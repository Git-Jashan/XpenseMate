package com.jsingh.XpenseMate

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_transactions")
data class PendingTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rawText: String,
    val appName: String,
    val paymentMode: String,
    val timestamp: Long
)