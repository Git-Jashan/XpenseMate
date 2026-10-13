package com.jsingh.XpenseMate.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jsingh.XpenseMate.service.ExpenseEngine
import com.jsingh.XpenseMate.service.TransactionRequest
import com.jsingh.XpenseMate.service.TransactionSource
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
    onDismiss: () -> Unit
) {
    val sharedPrefs = context.getSharedPreferences("XpenseMatePrefs", Context.MODE_PRIVATE)
    val savedPass = sharedPrefs.getString("SAVED_BANK_PASSWORD", "") ?: ""

    var passwordInput by remember { mutableStateOf(savedPass) }
    var isError by remember { mutableStateOf(false) }
    var isExtracting by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberPassword by remember { mutableStateOf(savedPass.isNotEmpty()) }

    // Reusable function to trigger the extraction process
    val triggerExtraction = {
        if (passwordInput.isNotEmpty() && !isExtracting) {
            isExtracting = true
            val finalPassword = passwordInput.trim() // Prevent accidental space copy-pastes

            CoroutineScope(Dispatchers.Main).launch {
                val resultText = extractTextFromPdf(context, pdfUri, finalPassword)

                if (resultText == "ERROR_WRONG_PASSWORD") {
                    isError = true
                    isExtracting = false
                } else if (resultText.startsWith("ERROR")) {
                    Toast.makeText(context, "Failed to read PDF", Toast.LENGTH_SHORT).show()
                    isExtracting = false
                } else {
                    Log.d("XpenseMate_PDF", "✅ UNLOCKED!")

                    if (rememberPassword) {
                        sharedPrefs.edit().putString("SAVED_BANK_PASSWORD", finalPassword).apply()
                    } else {
                        sharedPrefs.edit().remove("SAVED_BANK_PASSWORD").apply()
                    }

                    Toast.makeText(context, "Extracting transactions in background...", Toast.LENGTH_SHORT).show()
                    onDismiss()

                    CoroutineScope(Dispatchers.IO).launch {
                        val engine = ExpenseEngine(context.applicationContext)
                        val request = TransactionRequest(
                            rawText = resultText,
                            source = TransactionSource.STATEMENT,
                            appName = "STATEMENT",
                            paymentMode = "Bank Statement",
                            timestamp = System.currentTimeMillis()
                        )

                        val result = engine.processTransaction(request)
                        Log.d("XpenseMate_PDF", "🎉 Engine finished: $result")
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isExtracting) onDismiss() },
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
                    // ✨ UX Boost: Allow user to hit "Done/Enter" on their keyboard to submit
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { triggerExtraction() }
                    ),
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
                onClick = { triggerExtraction() }
            ) { Text("Unlock") }
        },
        dismissButton = {
            TextButton(
                enabled = !isExtracting,
                onClick = { onDismiss() }
            ) { Text("Cancel") }
        }
    )
}

private suspend fun extractTextFromPdf(context: Context, uri: Uri, password: String): String {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream, password).use { document ->
                    val stripper = PDFTextStripper()
                    stripper.sortByPosition = true
                    return@withContext stripper.getText(document)
                }
            } ?: "ERROR_FILE_NOT_FOUND"
        } catch (e: InvalidPasswordException) {
            return@withContext "ERROR_WRONG_PASSWORD"
        } catch (e: Exception) {
            Log.e("XpenseMate_PDF", "PDF Parse Error", e)
            return@withContext "ERROR_UNKNOWN"
        }
    }
}