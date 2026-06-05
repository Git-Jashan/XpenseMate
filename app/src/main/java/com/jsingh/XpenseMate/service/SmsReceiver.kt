package com.jsingh.XpenseMate.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0]?.originatingAddress?.lowercase() ?: ""
        val timestamp = messages[0]?.timestampMillis ?: System.currentTimeMillis()

        val originalBody = messages.joinToString("") { it?.messageBody ?: "" }
        val lowerBody = originalBody.lowercase()

        val cleanSender = sender.replace(Regex("[^0-9a-z+]"), "")
        val isPersonalNumber = cleanSender.matches(Regex("^(?:\\+91|91)?[6-9]\\d{9}$")) || cleanSender.startsWith("+")

        if (isPersonalNumber) {
            Log.d("XpenseMate_SMS", "🚫 Dropped [Gate 1]: Personal mobile number")
            return
        }

        val hasMoneyAmount = lowerBody.contains(
            Regex("(?i)((?:rs\\.?|inr|₹|rupees)\\s*[0-9,]+\\.?[0-9]*)|([0-9,]+\\.?[0-9]*\\s*(?:rs\\.?|inr|₹|rupees))")
        )

        if (!hasMoneyAmount) {
            Log.d("XpenseMate_SMS", "🗑️ Dropped [Gate 2]: No currency format found")
            return
        }

        val junkKeywords = listOf(
            "otp", "verification", "code is", "loan", "offer", "win", "apply now", "reward","within",
            "statement", "due date", "bill generated", "minimum due",
            "failed", "declined", "unsuccessful", "rejected", "bounced",
        )

        if (junkKeywords.any { lowerBody.contains(it) }) {
            Log.d("XpenseMate_SMS", "🗑️ Dropped [Gate 3]: Contains Spam/Bill/Failure keywords")
            return
        }

        val bankingSignature = listOf(
            "a/c","acc", "acct", "account", "card", "upi", "vpa","paid",
            "debited", "credited", "deducted", "refund", "reversed"
        )

        if (!bankingSignature.any { lowerBody.contains(it) }) {
            Log.d("XpenseMate_SMS", "🗑️ Dropped [Gate 4]: Lacks banking signature")
            return
        }

        Log.d("XpenseMate_SMS", "✅ ULTIMATE SENTRY PASSED: Valid Transaction from $sender")

        ExpenseEngine.processTransaction(context, originalBody, "SMS", "Bank", timestamp)}
}