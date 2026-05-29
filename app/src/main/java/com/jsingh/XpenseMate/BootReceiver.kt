package com.jsingh.XpenseMate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("XpenseMate_Boot", "🚀 Phone rebooted. Waking up Expense Engine...")
            ExpenseEngine.initialize(context)

        }
    }
}