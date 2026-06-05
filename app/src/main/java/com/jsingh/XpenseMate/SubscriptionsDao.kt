package com.jsingh.XpenseMate

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionsDao {

    // For the UI: Show all active subscriptions sorted by what is due next
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveSubscriptions(): Flow<List<Subscriptions>>

    // For the AI Engine: Check if a merchant is already a known subscription
    @Query("SELECT * FROM subscriptions WHERE merchant LIKE '%' || :merchantName || '%' AND isActive = 1 LIMIT 1")
    suspend fun getSubscriptionByMerchant(merchantName: String): Subscriptions?

    // Add a new subscription (from your UI Popup)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscriptions)

    // For the AI Engine: When a payment hits, push the due date to the next month
    @Query("UPDATE subscriptions SET nextDueDate = :newDate WHERE id = :subId")
    suspend fun updateNextDueDate(subId: Int, newDate: Long)

    // When a user cancels a subscription in the UI
    @Query("UPDATE subscriptions SET isActive = 0 WHERE id = :subId")
    suspend fun cancelSubscription(subId: Int)
}