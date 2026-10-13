package com.jsingh.XpenseMate.utils

import com.jsingh.XpenseMate.data.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object SubscriptionDetector {
    private val recurringKeywords = listOf(
        "netflix", "spotify", "prime", "amazon", "youtube", "apple", "disney",
        "gym", "rent", "wifi", "internet", "broadband", "electricity", "water bill",
        "chatgpt", "canva", "adobe", "salary", "wage", "emi", "loan"
    )

    fun isLikelyRecurring(newTitle: String, newAmount: Double, history: List<Transaction>): Boolean {
        val lowerTitle = newTitle.lowercase().trim()

        if (recurringKeywords.any { lowerTitle.contains(it) }) return true

        val pastTxns = history.filter { it.isExpense && it.title.lowercase().trim() == lowerTitle }

        if (pastTxns.isNotEmpty()) {
            val sameAmountTxns = pastTxns.filter { it.amount == newAmount }
            if (sameAmountTxns.isNotEmpty()) return true

            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val today = LocalDate.now()

            for (txn in pastTxns) {
                try {
                    val pastDate = LocalDate.parse(txn.date, dateFormatter)
                    val daysBetween = ChronoUnit.DAYS.between(pastDate, today).toLong()

                    if (daysBetween in 28..32 || daysBetween in 6..8 || daysBetween in 360..370) {
                        return true
                    }
                } catch (e: Exception) {
                }
            }
        }

        return false
    }
}