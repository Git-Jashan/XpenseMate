package com.jsingh.XpenseMate

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions") // Note: Make sure the 'indices' bouncer is removed if you still want edits to work!
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