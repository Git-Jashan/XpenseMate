package com.jsingh.XpenseMate.service

import com.jsingh.XpenseMate.data.*
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jsingh.XpenseMate.BuildConfig
import com.jsingh.XpenseMate.network.SupabaseNetwork
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.jsingh.XpenseMate.utils.*

object ExpenseEngine {

    private var appContext: Context? = null

    private const val WORKER_URL = "https://xpensemate-parser.jashanjeetsingh5050.workers.dev/"
    private const val WORKER_SECRET = BuildConfig.WORKER_SECRET

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    fun initialize(context: Context) {
        appContext = context.applicationContext
        Log.d("XpenseMate_Engine", "✅ Cloud AI Engine Ready!")
    }

    fun processTransaction(
        context: Context,
        rawText: String,
        appName: String,
        paymentMode: String,
        timestamp: Long,
        onResult: ((List<Transaction>?) -> Unit)? = null
    ) {
        if (appContext == null) appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            val cleanMessage = sanitizeSmsForCloud(rawText)
            analyzeWithAI(cleanMessage, appName, paymentMode, timestamp, rawText, onResult)
        }
    }

    private suspend fun analyzeWithAI(cleanMessage: String, appName: String, paymentMode: String, timestamp: Long, originalRawMessage: String, onResult: ((List<Transaction>?) -> Unit)?) {
        val context = appContext ?: return

        try {
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

            val userCategories = SettingsManager.getCategories(context).toList()

            val source = when (appName) {
                "SCREENSHOT" -> "SCREENSHOT"
                "SMS" -> "SMS"
                "STATEMENT" -> "STATEMENT"
                else -> "NOTIFICATION"
            }

            val contextString = when (source) {
                "SCREENSHOT" -> "UPI App Screenshot"
                "NOTIFICATION" -> "Android Payment Notification from $appName"
                "SMS" -> "SMS from ${extractAccountDigits(originalRawMessage)}"
                "STATEMENT" -> "PDF Bank Statement"
                else -> "Unknown Source"
            }

            val currentDateForAI = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val jsonBody = JSONObject().apply {
                put("sms_text", cleanMessage)
                put("source", source)
                put("app_name", appName)
                put("payment_mode", paymentMode)
                put("categories", JSONArray(userCategories))
                put("app_context", contextString)
                put("current_device_date", currentDateForAI)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(WORKER_URL)
                .addHeader("X-Worker-Secret", WORKER_SECRET)
                .addHeader("X-Device-ID", deviceId)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            // 🚀 OFFLINE RESCUE 1: API Failed
            if (!response.isSuccessful) {
                saveToOfflineQueue(context, cleanMessage, appName, paymentMode, timestamp)
                withContext(Dispatchers.Main) { onResult?.invoke(null) }
                return
            }

            val responseText = response.body?.string() ?: return
            Log.d("XpenseMate_Engine", "🧠 AI Response: $responseText")

            if (source == "STATEMENT") {
                val statementData = jsonParser.decodeFromString<AiStatementResult>(responseText)
                var savedCount = 0

                statementData.transactions.forEach { aiTxn ->
                    val safeAmount = aiTxn.amount ?: 0.0
                    val safeMerchant = aiTxn.merchant ?: "Unknown"
                    val safeIsExpense = if (aiTxn.type != null) (aiTxn.type.uppercase() == "DEBIT") else true

                    if (aiTxn.isValid && safeAmount > 0) {
                        val parsedTimestamp = try {
                            if (!aiTxn.date.isNullOrEmpty()) {
                                LocalDate.parse(aiTxn.date)
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant().toEpochMilli()
                            } else timestamp
                        } catch (e: Exception) { timestamp }

                        val trueDate = Instant.ofEpochMilli(parsedTimestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()

                        val newTxn = Transaction(
                            title = safeMerchant,
                            amount = safeAmount,
                            isExpense = safeIsExpense,
                            category = aiTxn.category ?: "General",
                            date = trueDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                            time = trueDate.format(DateTimeFormatter.ofPattern("hh:mm a")),
                            timestamp = parsedTimestamp,
                            merchantName = safeMerchant,
                            transactionType = aiTxn.type ?: "UNKNOWN",
                            account = aiTxn.paymentMode ?: "Bank Statement",
                            source = "STATEMENT",
                            phoneNumber = "---"
                        )
                        saveTransaction(newTxn) // 👈 This calls our updated save function which includes Supabase!
                        savedCount++
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Saved $savedCount transactions to history!", Toast.LENGTH_LONG).show()
                    onResult?.invoke(null)
                }

            } else {
                 val aiResult = jsonParser.decodeFromString<AiTransactionResult>(responseText)

                val safeAmount = aiResult.amount ?: 0.0
                val safeMerchant = aiResult.merchant ?: "Unknown"
                val safeIsExpense = if (source == "SCREENSHOT") true else {
                    if (aiResult.type != null) (aiResult.type.uppercase() == "DEBIT") else true
                }

                if (aiResult.isValid && safeAmount > 0) {
                    val trueDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()

                    val localAcct = extractAccountDigits(originalRawMessage)
                    val localPhone = try { extractPhoneNumber(originalRawMessage) } catch (e: Exception) { "---" }
                    val finalAccount = if (localAcct != "BANK" && localAcct != "---") "Acct: $localAcct" else "---"
                    val finalPaymentMode = if (paymentMode != "Bank" && paymentMode != "Unknown") paymentMode.uppercase() else (aiResult.paymentMode?.uppercase() ?: "UNKNOWN")
                    val finalMerchant = if (safeMerchant == "Unknown" && localPhone != "---") localPhone else safeMerchant

                    val newTxn = Transaction(
                        title = finalMerchant,
                        amount = safeAmount,
                        isExpense = safeIsExpense,
                        category = aiResult.category ?: "General",
                        date = trueDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        time = trueDate.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        timestamp = timestamp,
                        merchantName = finalMerchant,
                        transactionType = finalPaymentMode,
                        account = finalAccount,
                        phoneNumber = localPhone,
                        source = if (source == "NOTIFICATION") "Notification: $appName" else source
                    )

                    val aiConfidence = aiResult.confidence ?: 100.0

                    if (source == "SCREENSHOT" || aiConfidence < 85.0) {
                        Log.d("XpenseMate_Engine", "⚠️ AI Confidence low or Screenshot. Requesting UI Review.")
                        withContext(Dispatchers.Main) {
                            onResult?.invoke(listOf(newTxn))
                        }
                    } else {
                       CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val db = AppDatabase.getDatabase(context)
                                val dao = db.transactionDao()
                                val subDao = db.subscriptionDao()

                                val timeWindow = System.currentTimeMillis() - 120_000
                                if (dao.findDuplicate(newTxn.amount, newTxn.isExpense, newTxn.merchantName, timeWindow) != null) {
                                    return@launch
                                }

                               dao.insertTransaction(newTxn)
                                Log.d("XpenseMate_Engine", "💾 SAVED LOCAL SUCCESS → ₹${newTxn.amount}")

                               uploadTransactionToSupabase(newTxn)

                                // 3. Subscription Bridge
                                if (newTxn.isExpense) {
                                    val activeSub = subDao.getSubscriptionByMerchant(newTxn.merchantName)

                                    if (activeSub != null) {
                                        val newDueDate = calculateNextDueDate(activeSub.nextDueDate, activeSub.frequency)
                                        subDao.updateNextDueDate(activeSub.id, newDueDate)
                                    } else {
                                        val pastPayments = dao.getHistoricalPaymentCount(newTxn.merchantName, newTxn.amount)
                                        if (pastPayments >= 1) {
                                            sendSubscriptionNotification(context, newTxn.merchantName, newTxn.amount)
                                        }
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    onResult?.invoke(null)
                                }
                            } catch (e: Exception) {
                                Log.e("XpenseMate_Engine", "🚨 DB Error: ${e.message}")
                            }
                        }
                    }
                } else {
                    Log.d("XpenseMate_Engine", "🗑️ AI ruled this invalid.")
                    withContext(Dispatchers.Main) {
                        onResult?.invoke(null)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("XpenseMate_Engine", "❌ Network/Parsing Exception: ${e.message}")
            val contextToUse = appContext
            if (contextToUse != null) {
                saveToOfflineQueue(contextToUse, cleanMessage, appName, paymentMode, timestamp)
            }
            withContext(Dispatchers.Main) { onResult?.invoke(null) }
        }
    }

    fun saveTransaction(txn: Transaction) {
        val context = appContext ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.transactionDao()

                val timeWindow = System.currentTimeMillis() - 120_000
                if (dao.findDuplicate(txn.amount, txn.isExpense, txn.merchantName, timeWindow) != null) {
                    return@launch
                }

                dao.insertTransaction(txn)
                Log.d("XpenseMate_Engine", "💾 SAVED LOCAL SUCCESS → ₹${txn.amount}")

               uploadTransactionToSupabase(txn)

            } catch (e: Exception) {
                Log.e("XpenseMate_Engine", "🚨 DATABASE WRITE FAILED: ${e.message}")
            }
        }
    }

    private suspend fun uploadTransactionToSupabase(txn: Transaction) {
        try {
           var session = SupabaseNetwork.client.auth.currentSessionOrNull()

           if (session == null) {
                SupabaseNetwork.client.auth.awaitInitialization()
                session = SupabaseNetwork.client.auth.currentSessionOrNull()
            }

            if (session == null) {
                Log.e("XpenseMate_Engine", "☁️ Could not upload: Token missing.")
                return
            }

            val uploadDto = TransactionDto(
                user_id = session.user?.id,
                merchantName = txn.merchantName,
                amount = txn.amount,
                isExpense = txn.isExpense,
                category = txn.category,
                source = txn.source
            )

            SupabaseNetwork.client.postgrest["transactions"].insert(uploadDto)
            Log.d("XpenseMate_Engine", "☁️ CLOUD SYNC SUCCESS → ₹${txn.amount}")

        } catch (e: Exception) {
            Log.e("XpenseMate_Engine", "🚨 CLOUD SYNC FAILED: ${e.message}")
        }
    }

    private fun sanitizeSmsForCloud(raw: String): String {
        var scrubbed = raw
        scrubbed = scrubbed.replace(Regex("\\b\\d{10}\\b"), "[PHONE]")
        scrubbed = scrubbed.replace(Regex("(?i)(?:a/c|acct|ending in)\\s*\\d{3,6}"), "[ACCOUNT]")
        return scrubbed
    }

    private fun extractAccountDigits(message: String): String {
        return Regex("(?i)(?:a/c|acct|account|card|ending).*?(\\d{4})(?!\\d)")
            .find(message)?.groupValues?.get(1) ?: "---"
    }

    private fun extractPhoneNumber(message: String): String {
        return Regex("\\b\\d{10}\\b").find(message)?.value ?: "---"
    }

    private fun sendSubscriptionNotification(context: Context, merchant: String, amount: Double) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "subscription_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Subscription Alerts", NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Recurring Payment Detected")
            .setContentText("You paid ₹$amount to $merchant again. Check the app to mark as subscription.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(merchant.hashCode(), notification)
    }

    private suspend fun saveToOfflineQueue(context: Context, text: String, appName: String, mode: String, time: Long) {
        Log.d("XpenseMate_Engine", "⚠️ No Internet! Saving transaction to Offline Queue.")
        val db = AppDatabase.getDatabase(context)
        db.transactionDao().insertPending(PendingTransaction(0, text, appName, mode, time))

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    private fun calculateNextDueDate(currentDueDate: Long, frequency: String): Long {
        val calendar = Calendar.getInstance()
        val baseDate = if (currentDueDate < System.currentTimeMillis()) System.currentTimeMillis() else currentDueDate
        calendar.timeInMillis = baseDate

        when (frequency.uppercase()) {
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "YEARLY" -> calendar.add(Calendar.YEAR, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "QUARTERLY" -> calendar.add(Calendar.MONTH, 3)
            else -> calendar.add(Calendar.MONTH, 1)
        }
        return calendar.timeInMillis
    }
}