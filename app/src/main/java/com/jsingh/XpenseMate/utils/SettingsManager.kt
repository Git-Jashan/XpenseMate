package com.jsingh.XpenseMate.utils

import android.content.Context

object SettingsManager {
    private const val PREFS_NAME = "xpensemate_settings"
    private const val KEY_LAST_USER = "LAST_USER_ID"

    fun saveBudgets(context: Context, daily: Float, monthly: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat("daily_limit", daily).putFloat("monthly_limit", monthly).apply()
    }

    fun getBudgets(context: Context): Pair<Float, Float> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val daily = prefs.getFloat("daily_limit", 1000f)
        val monthly = prefs.getFloat("monthly_limit", 30000f)
        return Pair(daily, monthly)
    }

    fun saveCategories(context: Context, categories: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet("custom_categories", categories).apply()
    }

    // 2. Load the custom list (with defaults if it's their first time)
    fun getCategories(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultCategories = setOf("Food", "Travel", "Uni", "Shopping", "Rent", "General")
        return prefs.getStringSet("custom_categories", defaultCategories) ?: defaultCategories
    }

    // --- CATEGORY BUDGET STORAGE ---
    // Saves a map like: { "Food" -> 5000f, "Transport" -> 2000f }
    fun saveCategoryBudgets(context: Context, budgets: Map<String, Float>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Flattens the map into a single string: "Food|5000.0,Transport|2000.0"
        val flattenedString = budgets.entries.joinToString(",") { "${it.key}|${it.value}" }
        prefs.edit().putString("category_budgets", flattenedString).apply()
    }

    // Loads the map back out
    fun getCategoryBudgets(context: Context): Map<String, Float> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stringData = prefs.getString("category_budgets", "") ?: ""
        if (stringData.isEmpty()) return emptyMap()

        // Unpacks the string back into a Map
        return stringData.split(",").mapNotNull {
            val parts = it.split("|")
            if (parts.size == 2) parts[0] to (parts[1].toFloatOrNull() ?: 0f) else null
        }.toMap()
    }

    fun saveLastUserId(context: Context, userId: String) {
        val prefs = context.getSharedPreferences("xpense_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("LAST_USER_ID", userId).apply()
    }

    fun getLastUserId(context: Context): String? {
        val prefs = context.getSharedPreferences("xpense_prefs", Context.MODE_PRIVATE)
        return prefs.getString("LAST_USER_ID", null)
    }
}
