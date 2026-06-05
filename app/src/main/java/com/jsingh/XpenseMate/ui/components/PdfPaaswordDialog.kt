package com.jsingh.XpenseMate.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jsingh.XpenseMate.service.ExpenseEngine
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PdfPasswordDialog(
    context: Context,
    pdfUri: Uri,
    onDismiss: () -> Unit,
    onUnlock: (String, Boolean) -> Unit
) {
    // 1. Hook into Android's lightweight local storage
    val sharedPrefs = context.getSharedPreferences("XpenseMatePrefs", Context.MODE_PRIVATE)
    val savedPass = sharedPrefs.getString("SAVED_BANK_PASSWORD", "") ?: ""

    // 2. New States for our upgraded UI
    var passwordInput by remember { mutableStateOf(savedPass) }
    var isError by remember { mutableStateOf(false) }
    var isExtracting by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberPassword by remember { mutableStateOf(savedPass.isNotEmpty()) }

    AlertDialog(
        onDismissRequest = {
            if (!isExtracting) {
                onDismiss() // 👈 FIX: Closes the dialog correctly
            }
        },
        title = { Text("Encrypted Statement", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter the password to unlock this bank statement.", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        isError = false
                    },
                    label = { Text("Password") },
                    isError = isError,
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle password visibility")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text("Incorrect Password. Try again.", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { rememberPassword = !rememberPassword }
                ) {
                    Checkbox(checked = rememberPassword, onCheckedChange = { rememberPassword = it })
                    Text("Remember password", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                if (isExtracting) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = passwordInput.isNotEmpty() && !isExtracting,
                onClick = {
                    isExtracting = true
                    CoroutineScope(Dispatchers.Main).launch {
                        val resultText = extractTextFromPdf(context, pdfUri, passwordInput)

                        if (resultText == "ERROR_WRONG_PASSWORD") {
                            isError = true
                            isExtracting = false
                        } else if (resultText.startsWith("ERROR")) {
                            Toast.makeText(context, "Failed to read PDF", Toast.LENGTH_SHORT).show()
                            isExtracting = false
                        } else {
                            Log.d("XpenseMate_PDF", "✅ UNLOCKED! Extracted ${resultText.length} characters.")

                            if (rememberPassword) {
                                sharedPrefs.edit().putString("SAVED_BANK_PASSWORD", passwordInput).apply()
                            } else {
                                sharedPrefs.edit().remove("SAVED_BANK_PASSWORD").apply()
                            }

                            ExpenseEngine.processTransaction(
                                context = context,
                                rawText = resultText,
                                appName = "STATEMENT",
                                paymentMode = "Bank Statement",
                                timestamp = System.currentTimeMillis()
                            ) { parsedTxn ->
                                Log.d("XpenseMate_PDF", "🎉 AI FINISHED PARSING STATEMENT!")
                                CoroutineScope(Dispatchers.Main).launch {
                                    onDismiss() // 👈 FIX: Closes the dialog correctly
                                }
                            }
                        }
                    }
                }
            ) { Text("Unlock") }
        },
        dismissButton = {
            TextButton(
                enabled = !isExtracting,
                onClick = {
                    onDismiss() // 👈 FIX: Closes the dialog correctly
                }) { Text("Cancel") }
        }
    )
}

private suspend fun extractTextFromPdf(context: Context, uri: Uri, password: String): String {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, password)
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                document.close()
                return@withContext text
            } ?: "ERROR_FILE_NOT_FOUND"
        } catch (e: InvalidPasswordException) {
            return@withContext "ERROR_WRONG_PASSWORD"
        } catch (e: Exception) {
            Log.e("XpenseMate_PDF", "PDF Parse Error", e)
            return@withContext "ERROR_UNKNOWN"
        }
    }
}
