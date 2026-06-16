package com.jsingh.XpenseMate.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val isExpense: Boolean,
    val category: String = "General",
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val merchantName: String = "Unknown",
    val transactionType: String = "UNKNOWN",
    val account: String = "---",
    val source: String = "MANUAL",
    val phoneNumber: String = "---"
)
@Serializable
data class TransactionDto(
    val id: String? = null, 
    val user_id: String? = null,
    @SerialName("merchant") val merchantName: String? = "Unknown",
    val amount: Double? = 0.0,
    @SerialName("is_expense") val isExpense: Boolean? = true,
    val category: String? = "Uncategorized",
    val source: String? = "Manual",
    val created_at: String? = null
)