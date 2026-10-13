package com.jsingh.XpenseMate.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.jsingh.XpenseMate.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    private const val PREFS_NAME = "XpenseMateExportPrefs"
    private const val KEY_LAST_EXPORT = "last_export_timestamp"

    // Get the last exported time to show in the UI
    fun getLastExportDate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val timestamp = prefs.getLong(KEY_LAST_EXPORT, 0L)
        if (timestamp == 0L) return "Never"
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    suspend fun exportTransactionsToCsv(context: Context, transactions: List<Transaction>) {
        withContext(Dispatchers.IO) {
            try {
                val csvBuilder = java.lang.StringBuilder()

                // 🚀 PRO TIP: Add UTF-8 BOM so Microsoft Excel reads symbols (like ₹) correctly!
                csvBuilder.append('\uFEFF')

                // --- 1. EXCEL HEADER & SUMMARY ---
                csvBuilder.append("XPENSEMATE FINANCIAL STATEMENT\n")
                csvBuilder.append("Generated On:,${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}\n")
                csvBuilder.append("Total Records:,${transactions.size}\n\n")

                val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
                val netBalance = totalIncome - totalExpense

                csvBuilder.append("--- SUMMARY ---\n")
                csvBuilder.append("Total Income (₹),Total Expense (₹),Net Balance (₹)\n")
                csvBuilder.append("$totalIncome,$totalExpense,$netBalance\n\n")

                // --- 2. TRANSACTIONS DATA ---
                csvBuilder.append("--- TRANSACTIONS ---\n")
                // Using raw numbers for amount so Excel formulas work properly!
                csvBuilder.append("Date,Time,Type,Category,Title,Amount (₹)\n")

                for (t in transactions) {
                    val type = if (t.isExpense) "Expense" else "Income"

                    // Escape commas so titles like "Domino's, Pizza" don't break Excel columns
                    val safeTitle = "\"${t.title.replace("\"", "\"\"")}\""
                    val safeCategory = "\"${t.category.replace("\"", "\"\"")}\""

                    csvBuilder.append("${t.date},${t.time},$type,$safeCategory,$safeTitle,${t.amount}\n")
                }

                val filename = "XpenseMate_Statement_${System.currentTimeMillis()}.csv"

                // --- 3. SAVE TO DOWNLOADS FOLDER ---
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(csvBuilder.toString().toByteArray())
                        }
                    }
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, filename)
                    file.writeText(csvBuilder.toString())
                }

                // Save the timestamp to history
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(KEY_LAST_EXPORT, System.currentTimeMillis())
                    .apply()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Saved to Downloads!", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}