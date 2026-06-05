package com.jsingh.XpenseMate.utils

import android.content.Context

object ThemeManager {
    private const val PREFS_NAME = "xpensemate_theme"
    private const val KEY_IS_DARK = "is_dark_mode"

    fun saveTheme(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK, isDark).apply()
    }

    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Default to 'true' since your app's identity is currently dark/silver
        return prefs.getBoolean(KEY_IS_DARK, true)
    }
}

