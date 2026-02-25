package com.jsingh.XpenseMate

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // This connects your SQL commands (DAO) to the physical database
    abstract fun transactionDao(): TransactionDao
}