package com.jsingh.XpenseMate.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.jsingh.XpenseMate.utils.UpiUtils

@Composable
fun UpiPaymentDialog(
    payeeName: String,
    payeeVpa: String,
    merchantCode: String,
    scannedAmount: String,
    onDismiss: () -> Unit,
    onPaymentAttempted: (Double, String, Boolean?) -> Unit
) {
    val context = LocalContext.current
    var amountInput by remember { mutableStateOf(scannedAmount) }
    var appsExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val rawInstalledApps = remember { UpiUtils.getInstalledUpiApps(context) }
    val sharedPreferences = remember { context.getSharedPreferences("XpenseMatePrefs", Context.MODE_PRIVATE) }
    val lastUsedPackage = remember { sharedPreferences.getString("LAST_UPI_APP", null) }

    // Set default recommended package (GPay)
    val recommendedPackage = "com.google.android.apps.nbu.paisa.user"

    // Weighted Multi-Tier Sorting Matrix
    val sortedApps = remember(rawInstalledApps, lastUsedPackage) {
        rawInstalledApps.sortedWith { app1, app2 ->
            val score1 = when {
                app1.packageName == recommendedPackage -> 3
                app1.packageName == lastUsedPackage -> 2
                else -> 1
            }
            val score2 = when {
                app2.packageName == recommendedPackage -> 3
                app2.packageName == lastUsedPackage -> 2
                else -> 1
            }
            score2.compareTo(score1) // Descending sequence evaluation
        }
    }

    // Default selection is the highest scored app
    var selectedApp by remember { mutableStateOf(sortedApps.firstOrNull()) }

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isValidAmount = parsedAmount >= 1.0

    LaunchedEffect(Unit) {
        if (scannedAmount.isEmpty()) focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF0A0A0A))
                    .border(1.dp, Color(0xFF1F1F1F), RoundedCornerShape(28.dp))
                    .clickable(enabled = false) {}
                    .padding(24.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Payee Header Identity Block
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF141414), CircleShape)
                            .border(1.dp, Color(0xFF262626), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = payeeName.take(1).uppercase().ifEmpty { "E" },
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(payeeName.ifEmpty { "External Payee" }, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(payeeVpa, color = Color(0xFF8E8E93), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        val sanitized = if (input.startsWith(".")) "0$input" else input
                        if (sanitized.isEmpty() || sanitized.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountInput = sanitized
                        }
                    },
                    prefix = { Text("₹", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold) },
                    textStyle = TextStyle(color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFF0A84FF)
                    )
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1F1F1F)))
                Spacer(modifier = Modifier.height(24.dp))

                // Smart Selector Component
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF121212))
                        .border(1.dp, Color(0xFF1F1F1F), RoundedCornerShape(18.dp))
                ) {
                    // Closed State Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { appsExpanded = !appsExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        selectedApp?.let { app ->
                            val isRecommended = app.packageName == recommendedPackage
                            val isRecent = app.packageName == lastUsedPackage

                            Image(painter = rememberDrawablePainter(app.icon), contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.appName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isRecommended && isRecent) {
                                        Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFCC00), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Recommended • Last Used", color = Color(0xFFFFCC00), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    } else if (isRecommended) {
                                        Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFCC00), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Recommended (Best Rewards)", color = Color(0xFFFFCC00), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    } else if (isRecent) {
                                        Text("Recently Used", color = Color(0xFF0A84FF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    } else {
                                        Text("Installed Provider", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        Icon(
                            imageVector = if (appsExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF8E8E93)
                        )
                    }

                    // Expanded List
                    AnimatedVisibility(
                        visible = appsExpanded,
                        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(animationSpec = tween(150))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF161616))) {
                            sortedApps.forEach { app ->
                                val isRecommended = app.packageName == recommendedPackage
                                val isRecent = app.packageName == lastUsedPackage

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedApp = app
                                            appsExpanded = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(painter = rememberDrawablePainter(app.icon), contentDescription = null, modifier = Modifier.size(26.dp))
                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.appName, color = Color.White, fontSize = 14.sp)
                                        if (isRecommended && isRecent) {
                                            Text("Best Reward Yield • Last Used", color = Color(0xFFFFCC00), fontSize = 10.sp)
                                        } else if (isRecommended) {
                                            Text("Best Reward Yield", color = Color(0xFFFFCC00), fontSize = 10.sp)
                                        } else if (isRecent) {
                                            Text("Last Used Choice", color = Color(0xFF0A84FF), fontSize = 10.sp)
                                        }
                                    }

                                    if (selectedApp?.packageName == app.packageName) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF0A84FF), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Intent Execution Button
                Button(
                    onClick = {
                        if (isValidAmount && selectedApp != null) {
                            // 1. Save Last Used App
                            sharedPreferences.edit().putString("LAST_UPI_APP", selectedApp!!.packageName).apply()

                            // 2. Build Safe URI manually to prevent Uri.Builder from corrupting '@' into '%40'
                            val safeVpa = payeeVpa.trim()
                            val safeName = Uri.encode(payeeName.trim().ifBlank { "User Transfer" }) // Encodes spaces safely

                            var uriString = "upi://pay?pa=$safeVpa&pn=$safeName&cu=INR"

                            // Only append Merchant Code if it's a valid shop.
                            // DO NOT append 'am', 'tr', or 'tn' to bypass security blocks.
                            if (merchantCode.isNotBlank() && merchantCode != "0000") {
                                uriString += "&mc=${merchantCode.trim()}"
                            }

                            val paymentUri = Uri.parse(uriString)

                            // 3. The Safe Intent
                            val executionIntent = Intent(Intent.ACTION_VIEW, paymentUri).apply {
                                setPackage(selectedApp!!.packageName)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }

                            try {
                                // 4. Log intention locally (passing null so it waits for background engine to confirm via SMS)
                                onPaymentAttempted(parsedAmount, safeName, null)

                                // 5. Fire the Intent
                                context.startActivity(executionIntent)
                            } catch (err: Exception) {
                                Toast.makeText(context, "Execution halted: Cannot initialize target channel.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = isValidAmount && selectedApp != null,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF), disabledContainerColor = Color(0xFF1C1C1E))
                ) {
                    Text(
                        text = if (!isValidAmount) "Configure Amount" else "Pay ₹$amountInput via ${selectedApp?.appName}",
                        color = if (!isValidAmount) Color(0xFF48484A) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}