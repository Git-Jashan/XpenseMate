package com.jsingh.XpenseMate.data

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AiTransactionResult(
    val isValid: Boolean = false,
    val raw_amount_string: String? = null,
    val amount: Double? = null,
    val type: String? = null,
    val merchant: String? = null,
    val category: String? = null,
    val paymentMode: String? = null,
    val confidence: Double? = null,
    val date: String? = null // 🚀 Added Date for historical PDF syncing
)

// 🚀 Parent class to hold the massive array of PDF transactions
@Keep
@Serializable
data class AiStatementResult(
    val transactions: List<AiTransactionResult>
)