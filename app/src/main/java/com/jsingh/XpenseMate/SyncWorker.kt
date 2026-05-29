package com.jsingh.XpenseMate

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("XpenseMate_Sync", "🔄 Internet restored! Checking offline queue...")
        val db = AppDatabase.getDatabase(applicationContext)
        val pendingItems = db.transactionDao().getAllPending()

        if (pendingItems.isEmpty()) {
            Log.d("XpenseMate_Sync", "✅ No offline transactions pending.")
            return Result.success()
        }

        // Send them to the engine one by one
        for (item in pendingItems) {
            Log.d("XpenseMate_Sync", "☁️ Syncing missed transaction from ${item.appName}")

            // Re-feed it into our main engine
            ExpenseEngine.processTransaction(
                context = applicationContext,
                rawText = item.rawText,
                appName = item.appName,
                paymentMode = item.paymentMode,
                timestamp = item.timestamp
            )

            // Delete it from the waiting room
            db.transactionDao().deletePending(item.id)
        }

        return Result.success()
    }
}