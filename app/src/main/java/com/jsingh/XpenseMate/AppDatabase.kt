package com.jsingh.XpenseMate

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 🚀 1. Added Subscription::class AND Bumped Version to 12
@Database(
    entities = [Transaction::class, PendingTransaction::class, Subscriptions::class],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    // 🚀 2. Added the new DAO access point
    abstract fun subscriptionDao(): SubscriptionsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP INDEX IF EXISTS index_transactions_amount_date_time")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN phoneNumber TEXT NOT NULL DEFAULT '---'")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pending_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `rawText` TEXT NOT NULL, 
                        `appName` TEXT NOT NULL, 
                        `paymentMode` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        // 🚀 3. THE NEW MIGRATION (11 to 12) for Recurring Subscriptions
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Note: In SQLite, Kotlin 'Double' is 'REAL' and 'Boolean/Long' are 'INTEGER'
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `subscriptions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `merchant` TEXT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `frequency` TEXT NOT NULL, 
                        `billingDay` INTEGER NOT NULL,
                        `nextDueDate` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xpense_db"
                )
                    // 🚀 4. Added MIGRATION_11_12 to the builder
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}