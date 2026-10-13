package com.jsingh.XpenseMate.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.jsingh.XpenseMate.data.AppDatabase
import com.jsingh.XpenseMate.data.PendingTransaction
import com.jsingh.XpenseMate.data.Transaction
import com.jsingh.XpenseMate.data.TransactionDto
import com.jsingh.XpenseMate.network.SupabaseNetwork
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.R

// ─────────────────────────────────────────────────────────────────────────────
// Input / Output models
// ─────────────────────────────────────────────────────────────────────────────

enum class TransactionSource { SMS, NOTIFICATION, SCREENSHOT, STATEMENT }

data class TransactionRequest(
    val rawText: String,
    val source: TransactionSource,
    val appName: String,
    val paymentMode: String,
    val timestamp: Long,
    val sender: String = ""
)

sealed class ParseResult {
    data class NeedsReview(val transactions: List<Transaction>) : ParseResult()
    object Saved : ParseResult()
    object Ignored : ParseResult()
    data class Error(val cause: Throwable) : ParseResult()
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpenseEngine
// ─────────────────────────────────────────────────────────────────────────────

class ExpenseEngine(context: Context) {

    private val appContext: Context = context.applicationContext

    private val mlMutex = Mutex()
    private var mlExtractor: EntityExtractor? = null

    private suspend fun getMlExtractor(): EntityExtractor? = mlMutex.withLock {
        mlExtractor?.let { return@withLock it }
        return@withLock try {
            val client = EntityExtraction.getClient(
                EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
            )
            client.downloadModelIfNeeded().await()
            mlExtractor = client
            client
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit init failed", e)
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun processTransaction(request: TransactionRequest): ParseResult =
        withContext(Dispatchers.IO) {
            try {
                val session = SupabaseNetwork.client.auth.currentSessionOrNull()
                if (session == null) {
                    Log.d(TAG, "🚫 Dropped: No active user session found.")
                    return@withContext ParseResult.Ignored
                }

                val clean = if (request.source == TransactionSource.STATEMENT) {
                    request.rawText
                } else {
                    preprocessMessage(request.rawText)
                }

                analyzeMessage(clean, request)

            } catch (e: Exception) {
                Log.e(TAG, "Unhandled error in processTransaction", e)
                saveToOfflineQueue(request)
                ParseResult.Error(e)
            }
        }

    suspend fun saveTransaction(txn: Transaction): Boolean {
        return try {
            val dao = AppDatabase.getDatabase(appContext).transactionDao()
            // 🚀 FIX 2: Check for duplicates relative to the transaction's ACTUAL historical date
            if (dao.findDuplicate(txn.amount, txn.isExpense, txn.merchantName, duplicateWindowMs(txn.timestamp)) != null) {
                Log.d(TAG, "Duplicate transaction skipped silently: ${txn.merchantName}")
                return true
            }
            dao.insertTransaction(txn)
            uploadTransactionToSupabase(txn)
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveTransaction failed", e)
            false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core analysis pipeline
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun analyzeMessage(
        cleanMessage: String,
        request: TransactionRequest
    ): ParseResult {

        if (request.source == TransactionSource.STATEMENT) {
            return processStatement(cleanMessage, request.timestamp)
        }

        if (isSpamOrRequest(request.rawText)) {
            return ParseResult.Ignored
        }

        if (cleanMessage.length < 5 || !cleanMessage.contains(Regex("\\d"))) {
            return ParseResult.Ignored
        }

        val amount = extractAmount(cleanMessage)
        if (amount <= 0.0) {
            return ParseResult.Ignored
        }

        val explicitMerchant = extractMerchantFromSender(request.sender)
        val context = if (explicitMerchant != null) {
            TxnContext(explicitMerchant, isExpense = true)
        } else {
            extractSmartContext(cleanMessage, request.source)
        }

        val dateTime = Instant.ofEpochMilli(request.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val accountDigits = extractAccountDigits(cleanMessage)
        val phone = extractPhoneNumber(cleanMessage)
        val merchant = if (context.merchant == UNKNOWN_MERCHANT && phone != NO_VALUE) phone else context.merchant
        val sourceLabel = if (request.source == TransactionSource.NOTIFICATION) {
            "Notification: ${request.appName}"
        } else {
            request.source.name
        }

        val txn = Transaction(
            title = merchant,
            amount = amount,
            isExpense = context.isExpense,
            category = guessCategoryLocally(merchant),
            date = dateTime.format(DATE_FMT),
            time = dateTime.format(TIME_FMT),
            timestamp = request.timestamp,
            merchantName = merchant,
            transactionType = request.paymentMode.uppercase(),
            account = if (accountDigits != NO_VALUE) "Acct: $accountDigits" else NO_VALUE,
            phoneNumber = phone,
            source = sourceLabel
        )

        val confidence = calculateConfidence(txn, cleanMessage)

        if (request.source == TransactionSource.SCREENSHOT || !confidence.isHighConfidence) {
            return ParseResult.NeedsReview(listOf(txn))
        }

        // Use relative time window
        val dao = AppDatabase.getDatabase(appContext).transactionDao()
        if (dao.findDuplicate(txn.amount, txn.isExpense, txn.merchantName, duplicateWindowMs(txn.timestamp)) != null) {
            return ParseResult.Ignored
        }

        dao.insertTransaction(txn)
        uploadTransactionToSupabase(txn)
        handleSubscriptionLogic(txn)

        return ParseResult.Saved
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Statement processing (Tailored for SBI & Standard Indian Bank Tables)
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun processStatement(
        cleanMessage: String,
        timestamp: Long
    ): ParseResult {
        Log.d(TAG, "Processing bank statement lines...")
        var savedCount = 0

        val statementRowRegex = Regex("""^(\d{2}-\d{2}-\d{2})\s+(.*?)\s+(?:-|\w+)?\s*([\d,.]+)\s+([\d,.]+)\s+([\d,.]+)$""")
        val pdfDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy")

        val lines = cleanMessage.split("\n")
        Log.d(TAG, "Total lines to analyze in statement: ${lines.size}")

        for (line in lines) {
            val trimmed = line.trim()
            val match = statementRowRegex.find(trimmed) ?: continue

            // 🚀 FIX 1: Parse the Exact Historical Date
            val dateStr = match.groupValues[1]
            val localDate = LocalDate.parse(dateStr, pdfDateFormatter)
            val txnTimestamp = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val displayDate = localDate.format(DATE_FMT)

            val narration = match.groupValues[2].trim()
            val creditStr = match.groupValues[3].replace(",", "")
            val debitStr = match.groupValues[4].replace(",", "")

            val creditAmount = creditStr.toDoubleOrNull() ?: 0.0
            val debitAmount = debitStr.toDoubleOrNull() ?: 0.0

            if (creditAmount == 0.0 && debitAmount == 0.0) continue

            val isExpense = debitAmount > 0.0
            val finalAmount = if (isExpense) debitAmount else creditAmount

            var merchant = ""
            if (narration.startsWith("UPI/", ignoreCase = true)) {
                val parts = narration.split("/")
                if (parts.size >= 4 && parts[3].trim().isNotEmpty()) {
                    merchant = parts[3].trim()
                }
            } else if (narration.contains("INTEREST CREDIT", ignoreCase = true)) {
                merchant = "Bank Interest"
            }

            if (merchant.isEmpty()) {
                merchant = narration.take(25)
            }

            // 🚀 FIX 3: Beautify the Merchant Name
            merchant = cleanStatementMerchant(merchant)

            val txn = Transaction(
                title = merchant,
                amount = finalAmount,
                isExpense = isExpense,
                category = guessCategoryLocally(merchant),
                date = displayDate,
                time = "12:00 AM", // Bank statements omit exact time
                timestamp = txnTimestamp,
                merchantName = merchant,
                transactionType = "BANK",
                account = "Bank Statement",
                source = "STATEMENT",
                phoneNumber = NO_VALUE
            )

            // Will internally use the historical `txnTimestamp` to prevent dupes!
            if (saveTransaction(txn)) {
                savedCount++
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(appContext, "Saved $savedCount transactions from statement!", Toast.LENGTH_LONG).show()
        }

        return if (savedCount > 0) ParseResult.Saved else ParseResult.Ignored
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Merchant Cleaning Engine
    // ─────────────────────────────────────────────────────────────────────────

    private fun cleanStatementMerchant(raw: String): String {
        var m = raw.uppercase()

        // 1. Instantly recognize mapped names (ZOMATO, SWIGGY, ZEPTO)
        for ((key, value) in SENDER_MAP) {
            if (m.contains(key)) return value
        }

        // 2. Strip standard business suffixes (L, MA, LTD, PVT)
        m = m.replace(Regex("\\b(L|MA|LTD|PVT|PRIVATE|LIMITED|INC)\\b", RegexOption.IGNORE_CASE), "")

        // 3. Remove punctuation and numbers, keeping only letters
        m = m.replace(Regex("[^A-Z\\s]"), " ")

        // 4. Remove extra spaces and return as gorgeous Title Case
        m = m.replace(Regex("\\s+"), " ").trim()

        return toTitleCase(m)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spam / request filter
    // ─────────────────────────────────────────────────────────────────────────

    private fun isSpamOrRequest(text: String): Boolean {
        if (REGEX_REQUEST.containsMatchIn(text)) return true
        if (REGEX_PROMO.containsMatchIn(text)) return true
        if (REGEX_FAILED.containsMatchIn(text)) return true
        val isReminder = REGEX_REMINDER.containsMatchIn(text)
        val isConfirmed = REGEX_CONFIRMED.containsMatchIn(text)
        if (isReminder && !isConfirmed) return true
        return false
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Merchant extraction
    // ─────────────────────────────────────────────────────────────────────────

    private data class TxnContext(val merchant: String, val isExpense: Boolean)

    private fun extractMerchantFromSender(sender: String): String? {
        val s = sender.uppercase()
        return SENDER_MAP.entries.firstOrNull { (key, _) -> s.contains(key) }?.value
    }

    private fun extractSmartContext(text: String, source: TransactionSource): TxnContext {
        val t = " $text "

        REGEX_NPCI.find(t)?.let { m ->
            cleanCapturedMerchant(m.groupValues[1])?.let { merchant ->
                if (merchant.length > 2) return TxnContext(toTitleCase(merchant), isExpense = true)
            }
        }

        REGEX_PAID_YOU.find(t)?.let { m ->
            cleanCapturedMerchant(m.groupValues[1])?.let { return TxnContext(toTitleCase(it), isExpense = false) }
        }

        REGEX_RECEIVED_FROM.find(t)?.let { m ->
            cleanCapturedMerchant(m.groupValues[1])?.let { return TxnContext(toTitleCase(it), isExpense = false) }
        }

        REGEX_POS.find(t)?.let { m ->
            cleanCapturedMerchant(m.groupValues[1])?.let { return TxnContext(toTitleCase(it), isExpense = true) }
        }

        REGEX_TOWARDS.find(t)?.let { m ->
            cleanCapturedMerchant(m.groupValues[1])?.let { return TxnContext(toTitleCase(it), isExpense = true) }
        }

        REGEX_PAID_TO.find(t)?.let { m ->
            cleanCapturedMerchant(m.groupValues[1])?.let { return TxnContext(toTitleCase(it), isExpense = true) }
        }

        REGEX_VPA.find(t)?.let { m ->
            val domain = m.groupValues[2].lowercase()
            if (domain !in PERSONAL_VPA_DOMAINS) {
                val isExpense = source == TransactionSource.SCREENSHOT ||
                        !t.contains(REGEX_INCOMING_KEYWORDS)
                return TxnContext(m.groupValues[1], isExpense)
            }
        }

        val isExpense = source == TransactionSource.SCREENSHOT ||
                !t.contains(REGEX_INCOMING_KEYWORDS)
        return TxnContext(UNKNOWN_MERCHANT, isExpense)
    }

    private fun cleanCapturedMerchant(raw: String): String? {
        var m = raw.replace(REGEX_MERCHANT_PREFIX, "")
        m = m.replace(REGEX_MERCHANT_TRAILING_JUNK, "")
        m = m.replace(Regex("\\s+"), " ").trim().trim('.', ',', '-', ' ')
        if (m.length < 3) return null
        if (m.lowercase() in GENERIC_MERCHANT_WORDS) return null
        return m
    }

    private fun toTitleCase(s: String): String =
        s.lowercase().split(" ").joinToString(" ") { w ->
            if (w.isNotEmpty()) w.replaceFirstChar { it.uppercase() } else w
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Amount extraction
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun extractAmount(text: String): Double {
        val fast = extractAmountFallback(text)
        if (fast > 0.0) return fast
        return extractAmountWithMlKit(text)
    }

    private fun extractAmountFallback(text: String): Double {
        val safeText = text.replace(REGEX_BALANCE_STRIP, " ")
        val matches = REGEX_MONEY.findAll(safeText).toList()
        if (matches.isEmpty()) return 0.0

        if (matches.size == 1) {
            return parseMoneyMatch(matches[0])
        }

        val keywordRanges = REGEX_AMOUNT_KEYWORDS.findAll(safeText).map { it.range }.toList()
        val best = if (keywordRanges.isEmpty()) {
            matches.first()
        } else {
            matches.minByOrNull { match ->
                keywordRanges.minOf { kr ->
                    when {
                        match.range.first > kr.last -> match.range.first - kr.last
                        kr.first > match.range.last -> kr.first - match.range.last
                        else -> 0
                    }
                }
            } ?: matches.first()
        }
        return parseMoneyMatch(best)
    }

    private fun parseMoneyMatch(match: MatchResult): Double {
        val raw = match.groupValues[1].ifEmpty { match.groupValues[2] }
        return raw.replace(",", "").toDoubleOrNull() ?: 0.0
    }

    private suspend fun extractAmountWithMlKit(text: String): Double {
        return try {
            val extractor = getMlExtractor() ?: return 0.0
            val params = EntityExtractionParams.Builder(text).build()
            val annotations = extractor.annotate(params).await()
            var amount = 0.0
            for (annotation in annotations) {
                for (entity in annotation.entities) {
                    if (entity.type == Entity.TYPE_MONEY) {
                        val moneyEntity = entity.asMoneyEntity()
                        val intPart = moneyEntity?.integerPart ?: 0
                        val fracStr = moneyEntity?.fractionalPart?.toString()?.padStart(2, '0') ?: "00"
                        amount = "$intPart.$fracStr".toDoubleOrNull() ?: 0.0
                    }
                }
            }
            amount
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit extraction failed", e)
            0.0
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Category inference
    // ─────────────────────────────────────────────────────────────────────────

    private fun guessCategoryLocally(merchant: String): String {
        val lower = merchant.lowercase()
        return when {
            REGEX_CAT_FOOD.containsMatchIn(lower)      -> "Food & Dining"
            REGEX_CAT_SHOPPING.containsMatchIn(lower)  -> "Shopping"
            REGEX_CAT_TRANSPORT.containsMatchIn(lower) -> "Transport"
            REGEX_CAT_BILLS.containsMatchIn(lower)     -> "Bills & Utilities"
            REGEX_CAT_HEALTH.containsMatchIn(lower)    -> "Health"
            else                                        -> "General"
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Confidence scoring
    // ─────────────────────────────────────────────────────────────────────────

    private data class Confidence(val score: Double) {
        val isHighConfidence: Boolean get() = score >= HIGH_CONFIDENCE_THRESHOLD
    }

    private fun calculateConfidence(txn: Transaction, rawMessage: String): Confidence {
        var score = 100.0
        if (txn.merchantName == UNKNOWN_MERCHANT || txn.merchantName.length < 3) score -= 40.0
        if (txn.amount <= 0) score -= 50.0
        if (txn.account == NO_VALUE && !rawMessage.contains("UPI", ignoreCase = true)) score -= 10.0
        return Confidence(score)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Text preprocessing
    // ─────────────────────────────────────────────────────────────────────────

    private fun preprocessMessage(raw: String): String {
        var clean = raw.replace('\u00A0', ' ')
        for (phrase in BOILERPLATE_PHRASES) {
            clean = clean.replace(Regex("(?i)" + Regex.escape(phrase)), " ")
        }
        clean = clean.replace(Regex("\\s*-\\s*$"), "")
        return clean.replace(Regex("\\s+"), " ").trim()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Field extraction helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun extractAccountDigits(message: String): String =
        REGEX_ACCOUNT.find(message)?.groupValues?.get(1) ?: NO_VALUE

    private fun extractPhoneNumber(message: String): String {
        val refRanges = REGEX_REF_WORDS.findAll(message).map { it.range }.toList()
        for (match in REGEX_PHONE.findAll(message)) {
            val tooCloseToRef = refRanges.any { rr -> (match.range.first - rr.last) in 0..20 }
            if (!tooCloseToRef) return match.value
        }
        return NO_VALUE
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Supabase upload
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun uploadTransactionToSupabase(txn: Transaction) {
        try {
            var session = SupabaseNetwork.client.auth.currentSessionOrNull()
            if (session == null) {
                SupabaseNetwork.client.auth.awaitInitialization()
                session = SupabaseNetwork.client.auth.currentSessionOrNull()
            }
            if (session == null) {
                Log.w(TAG, "No active session — queuing upload for later")
                saveToOfflineQueue(txn)
                return
            }
            val dto = TransactionDto(
                user_id = session.user?.id,
                merchantName = txn.merchantName,
                amount = txn.amount,
                isExpense = txn.isExpense,
                category = txn.category,
                source = txn.source
            )
            SupabaseNetwork.client.postgrest["transactions"].insert(dto)
        } catch (e: Exception) {
            Log.e(TAG, "Supabase upload failed — queuing for retry", e)
            saveToOfflineQueue(txn)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subscription logic
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun handleSubscriptionLogic(txn: Transaction) {
        if (!txn.isExpense) return
        val subDao = AppDatabase.getDatabase(appContext).subscriptionDao()
        val txnDao = AppDatabase.getDatabase(appContext).transactionDao()

        val activeSub = subDao.getSubscriptionByMerchant(txn.merchantName)
        if (activeSub != null) {
            val nextDue = calculateNextDueDate(activeSub.nextDueDate, activeSub.frequency)
            subDao.updateNextDueDate(activeSub.id, nextDue)
        } else {
            val pastPayments = txnDao.getHistoricalPaymentCount(txn.merchantName, txn.amount)
            if (pastPayments >= 1) sendSubscriptionNotification(txn.merchantName, txn.amount)
        }
    }

    private fun sendSubscriptionNotification(merchant: String, amount: Double) {
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "subscription_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Subscription Alerts", NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Recurring Payment Detected")
            .setContentText("You paid ₹$amount to $merchant again. Tap to mark as subscription.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(merchant.hashCode(), notification)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Offline queue
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun saveToOfflineQueue(request: TransactionRequest) {
        try {
            AppDatabase.getDatabase(appContext).transactionDao().insertPending(
                PendingTransaction(0, request.rawText, request.appName, request.paymentMode, request.timestamp)
            )
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                "OfflineSyncWork",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enqueue offline transaction", e)
        }
    }

    private suspend fun saveToOfflineQueue(txn: Transaction) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                "OfflineSyncWork",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enqueue Supabase retry", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Date helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun calculateNextDueDate(currentDueDate: Long, frequency: String): Long {
        val calendar = Calendar.getInstance()
        val base = if (currentDueDate < System.currentTimeMillis()) System.currentTimeMillis() else currentDueDate
        calendar.timeInMillis = base
        when (frequency.uppercase()) {
            "MONTHLY"   -> calendar.add(Calendar.MONTH, 1)
            "YEARLY"    -> calendar.add(Calendar.YEAR, 1)
            "WEEKLY"    -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "QUARTERLY" -> calendar.add(Calendar.MONTH, 3)
            else        -> calendar.add(Calendar.MONTH, 1)
        }
        return calendar.timeInMillis
    }

    // 🚀 FIX 2: Creates a strict 2-minute duplicate window relative to any given timestamp
    private fun duplicateWindowMs(baseTime: Long) = baseTime - 120_000L

    // ─────────────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        private const val TAG = "XpenseMate_Engine"
        private const val UNKNOWN_MERCHANT = "Unknown Merchant"
        private const val NO_VALUE = "---"
        private const val HIGH_CONFIDENCE_THRESHOLD = 85.0

        private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

        private val REGEX_REQUEST = Regex(
            """(?i)\b(requested|has\s+requested|request(ed)?\s+money|asking\s+for|pay\s+now|approve\s+(the\s+)?request|collect\s+request)\b"""
        )
        private val REGEX_PROMO = Regex(
            """(?i)\b(win|jackpot|lucky\s+draw|pre[- ]?approved|apply\s+now|loan\s+offer|personal\s+loan|instant\s+loan|limited\s+period\s+offer|exclusive\s+offer|save\s+up\s+to|flat\s+\d+%\s*off|\d+%\s*off|discount\s+code|coupon|recharge\s+now|recharge\s+with|expires?\s+today|plan\s+expir|validity|scratch\s+card|earn\s+up\s+to|eligible\s+for|get\s+cashback|cashback\s+on)\b"""
        )
        private val REGEX_FAILED = Regex(
            """(?i)\b(failed|declined|rejected|unsuccessful|bounced|expired|insufficient\s+funds|could\s+not\s+be\s+processed)\b"""
        )
        private val REGEX_REMINDER = Regex(
            """(?i)\b(due\s+date|minimum\s+due|bill\s+generated|statement\s+generated|payment\s+due|is\s+due\s+on|recharge\s+due|plan\s+ending)\b"""
        )
        private val REGEX_CONFIRMED = Regex(
            """(?i)\b(debited|credited|paid|spent|received|sent)\b"""
        )

        private val REGEX_BALANCE_STRIP = Regex(
            """(?i)(?:avl|avbl|available|net|curr|current|wallet|total)?\s*(?:bal|balance|lim|limit)[:\s]*(?:rs\.?|inr|₹)?\s*[0-9,.]+"""
        )
        private val REGEX_MONEY = Regex(
            """(?i)(?:rs\.?|inr|₹)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)|([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s*(?:rs\.?|inr|₹)"""
        )
        private val REGEX_AMOUNT_KEYWORDS = Regex(
            """(?i)\b(debited|credited|spent|paid|sent|received|withdrawn|purchase|txn|transaction|deducted)\b"""
        )

        private val REGEX_NPCI = Regex(
            """(?i)UPI/(?:[A-Za-z0-9]+/)+([A-Za-z0-9&.\-\s]+?)(?:/|\s|\.|,|$)"""
        )
        private val REGEX_PAID_YOU = Regex(
            """(?i)\b([A-Za-z0-9&.\s]{2,40}?)\s+(?:paid\s+you|sent\s+you)\b"""
        )
        private val REGEX_RECEIVED_FROM = Regex(
            """(?i)\b(?:received|credited|transfer|cashback)\b.{0,20}?\b(?:from|by)\s+([A-Za-z0-9&.\s]{2,40}?)(?:\s+(?:on|ref|upi|via|vide|\-)|[.,]|$)"""
        )
        private val REGEX_POS = Regex(
            """(?i)\bat\s+([A-Za-z0-9&.\s]{2,40}?)\s+(?:on|card|txn|using|dated)\b"""
        )
        private val REGEX_TOWARDS = Regex(
            """(?i)\btowards\s+([A-Za-z0-9&.\s]{2,40}?)(?:\s+(?:on|ref|txn|dated|via|successful|vide)|[.,]|$)"""
        )
        private val REGEX_PAID_TO = Regex(
            """(?i)(?:\bto|\binfo[-:]?|\bpaid\s+to|\bsent\s+to)\s+([A-Za-z0-9&.\s]{2,40}?)(?:\s+(?:on|ref|upi|for|avl|bal|from|successful|successfully|via|towards|dated|vide|\-)|[.,]|$)"""
        )
        private val REGEX_VPA = Regex(
            """(?i)\b([a-zA-Z0-9.\-_]{2,})@([a-zA-Z]{2,})\b"""
        )
        private val REGEX_INCOMING_KEYWORDS = Regex(
            """(?i)credited|refund|received|cashback"""
        )
        private val REGEX_MERCHANT_PREFIX = Regex(
            """(?i)^(payee|beneficiary|name|merchant|vpa|to)\s*[:\-]?\s*"""
        )
        private val REGEX_MERCHANT_TRAILING_JUNK = Regex(
            """(?i)\s+(on|via|ref|txn|dated|using|card|successful|successfully|vide|\-).*$"""
        )

        private val REGEX_ACCOUNT = Regex(
            """(?i)(?:a/c|acct|account|card|ending|no\.?)\D{0,15}?(\d{4})(?!\d)"""
        )
        private val REGEX_PHONE = Regex("""\\b([6-9]\d{9})\b""")
        private val REGEX_REF_WORDS = Regex(
            """(?i)\b(ref|rrn|txn|transaction\s+id|order\s+id|utr)\b"""
        )

        private val REGEX_CAT_FOOD = Regex(
            """swiggy|zomato|mcdonalds|kfc|starbucks|dominos|cafe|restaurant|hotel|eatery|dine"""
        )
        private val REGEX_CAT_SHOPPING = Regex(
            """amazon|flipkart|myntra|blinkit|zepto|mart|supermarket|store|bazaar|ajio|nykaa"""
        )
        private val REGEX_CAT_TRANSPORT = Regex(
            """uber|ola|rapido|irctc|makemytrip|redbus|petrol|fuel|hpcl|bpcl|iocl|metro|parking"""
        )
        private val REGEX_CAT_BILLS = Regex(
            """airtel|jio|\bvi\b|vodafone|recharge|bill|electricity|bescom|broadband|netflix|spotify|hotstar|prime"""
        )
        private val REGEX_CAT_HEALTH = Regex(
            """apollo|pharmacy|hospital|clinic|medplus|diagnostic|netmeds|1mg"""
        )

        private val SENDER_MAP = linkedMapOf(
            "ZOMATO" to "Zomato",
            "SWIGGY" to "Swiggy",
            "ZEPTO"  to "Zepto",
            "BLINKI" to "Blinkit",
            "AMAZON" to "Amazon",
            "FLIPKA" to "Flipkart",
            "UBER"   to "Uber",
            "OLACAB" to "Ola",
            "MYNTRA" to "Myntra",
            "MAKE MY" to "MakeMyTrip",
            "MMT"    to "MakeMyTrip"
        )

        private val PERSONAL_VPA_DOMAINS = setOf(
            "gmail", "yahoo", "outlook", "hotmail", "icloud",
            "ybl", "okicici", "okhdfcbank", "oksbi", "paytm"
        )

        private val GENERIC_MERCHANT_WORDS = setOf(
            "your account", "your a/c", "account", "bank account",
            "wallet", "upi", "view", "details", "payee", "beneficiary",
            "name", "merchant"
        )

        private val BOILERPLATE_PHRASES = listOf(
            "tap to view details", "tap here to view", "tap to view", "swipe to view",
            "click here to view", "click to view", "view details", "view transaction",
            "see details", "for details tap here", "double tap", "check balance",
            "know more", "report dispute if not you", "if not you call",
            "ignore if already paid", "this is an auto-generated message",
            "this is a system generated message", "do not share your",
            "never share otp"
        )
    }
}