package com.jsingh.XpenseMate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface TransactionDao {

    // 1. THE SAVE COMMAND
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    // 2. THE READ COMMAND
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // Deletes a specific transaction from the database
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // --- OFFLINE QUEUE METHODS ---
    @Insert
    suspend fun insertPending(pending: PendingTransaction)

    @Query("SELECT * FROM pending_transactions")
    suspend fun getAllPending(): List<PendingTransaction>

    @Query("DELETE FROM pending_transactions WHERE id = :id")
    suspend fun deletePending(id: Int)

    // Checks if you've paid this exact amount to this merchant before
    @Query("SELECT COUNT(*) FROM transactions WHERE merchantName = :merchant AND amount = :amount")
    suspend fun getHistoricalPaymentCount(merchant: String, amount: Double): Int

    // 4. THE DEDUPLICATOR (Updated with Merchant Check)
    @Query("SELECT * FROM transactions WHERE amount = :amount AND isExpense = :isExpense AND merchantName = :merchant AND timestamp >= :timeWindow LIMIT 1")
    suspend fun findDuplicate(amount: Double, isExpense: Boolean, merchant: String, timeWindow: Long): Transaction?

    // 🚀 --- SMART CACHE WIPE COMMANDS --- 🚀
    // Completely obliterates all local transactions for user switching
    @Query("DELETE FROM transactions")
    suspend fun nukeTransactions()

    // Completely obliterates the offline queue so old users' data doesn't sync to the new user
    @Query("DELETE FROM pending_transactions")
    suspend fun nukePendingTransactions()

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE isExpense = 1 
        AND category = :category 
        AND timestamp >= :startOfMonthTimestamp
    """)
    suspend fun getTotalSpentForCategoryThisMonth(category: String, startOfMonthTimestamp: Long): Double?
}