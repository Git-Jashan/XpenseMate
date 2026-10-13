package com.jsingh.XpenseMate.service

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("XpenseMate_Boot", "🚀 Phone rebooted. Triggering offline sync & WatchDog...")

            // 1. Existing SyncWorker Logic
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)

            // 2. 👁️ NEW: WatchDog Resurrection Logic
            // We must check permissions first so it doesn't crash on boot if the user revoked them
            if (hasPermissions(context)) {
                val serviceIntent = Intent(context, BudgetWatchdogService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d("XpenseMate_Boot", "✅ WatchDog Foreground Service restarted!")
            } else {
                Log.d("XpenseMate_Boot", "❌ WatchDog permissions missing. Cannot start on boot.")
            }
        }
    }

    // Helper to check for the God Mode permissions
    private fun hasPermissions(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        val hasUsage = mode == AppOpsManager.MODE_ALLOWED
        val hasOverlay = Settings.canDrawOverlays(context)
        return hasUsage && hasOverlay
    }
}