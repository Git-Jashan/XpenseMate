package com.jsingh.XpenseMate.data

import kotlinx.serialization.Serializable

@Serializable
data class TransactionInsert(
    val user_id: String,
    val merchant: String,
    val amount: Double,
    val category: String = "Miscellaneous",
    val source: String = "Manual",
    val is_expense: Boolean = true
)