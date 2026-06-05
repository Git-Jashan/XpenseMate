package com.jsingh.XpenseMate.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

fun requestBatteryExemption(context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = context.packageName

    // 1. Check if we are already whitelisted
    if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
        Log.d("XpenseMate_Setup", "✅ Already exempt from battery limits!")
        return
    }

    // 2. If not, trigger the OS Exemption Dialog
    try {
        Log.d("XpenseMate_Setup", "⚠️ Requesting battery exemption...")
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("XpenseMate_Setup", "Failed to open battery settings: ${e.message}")
    }
}
