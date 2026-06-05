package com.jsingh.XpenseMate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscriptions(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchant: String,          // e.g., "Netflix", "Spotify"
    val amount: Double,            // e.g., 649.0
    val frequency: String,         // e.g., "MONTHLY", "YEARLY", "WEEKLY"
    val billingDay: Int,           // e.g., 15 (The day of the month it usually hits)
    val nextDueDate: Long,         // Timestamp of when it's supposed to hit next
    val category: String,          // e.g., "Entertainment"
    val isActive: Boolean = true   // If they cancel it, we flip this to false instead of deleting
)