package com.jsingh.XpenseMate.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jsingh.XpenseMate.data.AppDatabase

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val engine = ExpenseEngine(applicationContext)

    override suspend fun doWork(): Result {
        Log.d("XpenseMate_Sync", "🔄 Internet restored! Checking offline queue...")
        val db = AppDatabase.getDatabase(applicationContext)
        val pendingItems = db.transactionDao().getAllPending()

        if (pendingItems.isEmpty()) {
            return Result.success()
        }

        var allSuccessful = true

        for (item in pendingItems) {
            Log.d("XpenseMate_Sync", "☁️ Syncing missed transaction: ${item.rawText.take(20)}...")

            val mappedSource = when (item.appName) {
                "SMS" -> TransactionSource.SMS
                "STATEMENT" -> TransactionSource.STATEMENT
                "SCREENSHOT" -> TransactionSource.SCREENSHOT
                else -> TransactionSource.NOTIFICATION
            }

            // 1. Build the new Request Object
            val request = TransactionRequest(
                rawText = item.rawText,
                source = mappedSource, // Or map this from item.appName
                appName = item.appName,
                paymentMode = item.paymentMode,
                timestamp = item.timestamp
            )

            // 2. Call the engine using the instance
            val result = engine.processTransaction(request)

            // 3. Check the sealed class result
            if (result is ParseResult.Saved || result is ParseResult.Ignored) {
                db.transactionDao().deletePending(item.id)
                Log.d("XpenseMate_Sync", "🗑️ Deleted pending item ${item.id}")
            } else {
                allSuccessful = false
                Log.e("XpenseMate_Sync", "❌ Failed to process item ${item.id}. Keeping for retry.")
            }
        }

        return if (allSuccessful) Result.success() else Result.retry()
    }
}