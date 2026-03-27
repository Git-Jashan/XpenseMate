package com.jsingh.XpenseMate

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // 1. THE SAVE COMMAND
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    // 2. THE READ COMMAND (Newest first)
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    // 3. THE DELETE COMMAND
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: String)


}