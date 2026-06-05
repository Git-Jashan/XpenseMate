package com.jsingh.XpenseMate.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DevDashboard(
    context: Context,
    onInject: (String) -> Unit
) {
    // 🛠️ DEV DASHBOARD HIDDEN
    var testSms by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                Color(0xFFEEEEEE),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            "🛠️ Developer Testing Panel",
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        OutlinedTextField(
            value = testSms,
            onValueChange = { testSms = it },
            label = { Text("Paste Fake Bank SMS Here") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // BUTTON 1: Inject straight to Engine
            Button(onClick = {
                onInject(testSms) // 👈 THIS IS THE FIX. It hands the text back to MainActivity!
                testSms = ""
            }) {
                Text("Inject to AI")
            }

            // BUTTON 2: Test your exact SmsReceiver Gates!
            Button(
                onClick = {
                    val lowerBody = testSms.lowercase()

                    // GATE 2: Currency Check
                    val hasMoneyAmount = lowerBody.contains(Regex("(?i)(rs\\.?|inr|₹)\\s*[0-9,]+\\.?[0-9]*"))
                    if (!hasMoneyAmount) {
                        Log.d("XpenseMate_GATES", "🗑️ Dropped [Gate 2]: No currency format found")
                    } else {
                        // GATE 3: Junk Check
                        val junkKeywords = listOf(
                            "otp", "verification", "code is", "loan", "offer", "win",
                            "apply now", "reward", "within", "statement", "due date",
                            "bill generated", "minimum due", "outstanding", "failed",
                            "declined", "unsuccessful", "rejected", "bounced"
                        )
                        if (junkKeywords.any { lowerBody.contains(it) }) {
                            Log.d("XpenseMate_GATES", "🗑️ Dropped [Gate 3]: Contains Spam/Bill/Failure keywords")
                        } else {
                            // GATE 4: Bank Check
                            val bankingSignature = listOf(
                                "a/c", "acct", "account", "card", "upi", "vpa",
                                "debited", "credited", "deducted", "refund", "reversed"
                            )
                            if (!bankingSignature.any { lowerBody.contains(it) }) {
                                Log.d("XpenseMate_GATES", "🗑️ Dropped [Gate 4]: Lacks banking signature")
                            } else {
                                Log.d("XpenseMate_GATES", "✅ VIP APPROVED: Valid Transaction")
                            }
                        }
                    }
                    testSms = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Test Gates")
            }
        }
    }
}