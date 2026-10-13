package com.jsingh.XpenseMate.utils

import android.content.Context

object AlertSettingsManager {
    private const val PREFS_NAME = "budget_alerts_prefs"

    fun isAlertsEnabled(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("alerts_enabled", true)
    fun setAlertsEnabled(context: Context, value: Boolean) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("alerts_enabled", value).apply()

    fun isSoundEnabled(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("sound_enabled", true)
    fun setSoundEnabled(context: Context, value: Boolean) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("sound_enabled", value).apply()

    fun isAutoDismissEnabled(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("auto_dismiss", true)
    fun setAutoDismissEnabled(context: Context, value: Boolean) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean("auto_dismiss", value).apply()

    fun getAutoDismissTime(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getFloat("auto_dismiss_time", 8f)
    fun setAutoDismissTime(context: Context, value: Float) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putFloat("auto_dismiss_time", value).apply()

    fun getAlertThreshold(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getFloat("alert_threshold", 80f)
    fun setAlertThreshold(context: Context, value: Float) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putFloat("alert_threshold", value).apply()
}