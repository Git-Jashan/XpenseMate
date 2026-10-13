package com.jsingh.XpenseMate.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.jsingh.XpenseMate.data.AppDatabase
import com.jsingh.XpenseMate.utils.AlertSettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class BudgetWatchdogService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var watchdogJob: Job? = null

    // --- SESSION STATE MACHINE ---
    private var lastObservedPackage = ""
    private var lastTrackedApp: String? = null
    private var isPaymentInProgress: Boolean = false
    private var isOverlayCurrentlyShowing: Boolean = false

    companion object {
        private var instance: BudgetWatchdogService? = null

        fun clearTracking() {
            instance?.let {
                it.lastTrackedApp = null
                it.isPaymentInProgress = false
            }
        }
    }

    // 🚀 FIX 1: Ignore keyboards and system menus so they don't falsely trigger the exit popup
    private val ignoredPackages = listOf(
        "com.android.systemui",
        "com.google.android.inputmethod.latin", // GBoard
        "com.sec.android.inputmethod",          // Samsung Keyboard
        "com.microsoft.swiftkey",               // SwiftKey
        "android",                              // Android App Chooser (Fixes the Paytm bug!)
        "com.google.android.intentresolver",    // Android 12+ App Chooser
        "com.miui.securitycenter"
    )

    // 🚀 EXACT Official Play Store Packages for UPI (For Transition Logic)
    private val upiApps = listOf(
        "com.google.android.apps.nbu.paisa.user", // GPay
        "com.phonepe.app",                        // PhonePe
        "net.one97.paytm",                        // Paytm
        "com.paytm.business",                     // Paytm Business
        "in.org.npci.upiapp",                     // BHIM
        "money.super.payments",                   // super.money
        "com.dreamplug.android.cred",             // CRED
        "com.mobikwik_new",                       // MobiKwik
        "com.freecharge.android"                  // Freecharge
    )

    // 🚀 EXACT Official Play Store Packages (For Overlay Data)
    private val targetApps = mapOf(
        // 💸 UPI & PAYMENTS (MUST be here so the WatchDog knows they are "General" category!)
        "com.google.android.apps.nbu.paisa.user" to AppTargetInfo("Google Pay", "General"),
        "com.phonepe.app" to AppTargetInfo("PhonePe", "General"),
        "net.one97.paytm" to AppTargetInfo("Paytm", "General"),
        "com.paytm.business" to AppTargetInfo("Paytm Business", "General"),
        "in.org.npci.upiapp" to AppTargetInfo("BHIM", "General"),
        "money.super.payments" to AppTargetInfo("super.money", "General"),
        "com.dreamplug.android.cred" to AppTargetInfo("CRED", "General"),
        "com.mobikwik_new" to AppTargetInfo("MobiKwik", "General"),
        "com.freecharge.android" to AppTargetInfo("Freecharge", "General"),

        // 🍔 FOOD & DINING
        "com.application.zomato" to AppTargetInfo("Zomato", "Food & Dining"),
        "in.swiggy.android" to AppTargetInfo("Swiggy", "Food & Dining"),
        "com.hungerbox.customer.common" to AppTargetInfo("HungerBox", "Food & Dining"),
        "services.pizzahut.app" to AppTargetInfo("Pizza Hut", "Food & Dining"),
        "com.mcdonalds.mobileapp" to AppTargetInfo("McDonald's", "Food & Dining"),
        "com.yum.kfc" to AppTargetInfo("KFC", "Food & Dining"),
        "in.burgerking.android" to AppTargetInfo("Burger King", "Food & Dining"),
        "com.dodinos.controller" to AppTargetInfo("Dominos", "Food & Dining"),
        "com.Dominos" to AppTargetInfo("Domino's Pizza", "Food & Dining"),
        "au.com.dominos.olo.android.app" to AppTargetInfo("Domino's", "Food & Dining"),
        "com.faasos.android" to AppTargetInfo("EatSure", "Food & Dining"),
        "com.starbucks.in" to AppTargetInfo("Starbucks", "Food & Dining"),

        // 🛒 GROCERIES & QUICK COMMERCE
        "com.grofers.customerapp" to AppTargetInfo("Blinkit", "Groceries"),
        "com.jpl.jiomart" to AppTargetInfo("JioMart", "Groceries"),
        "com.zeptoconsumerapp" to AppTargetInfo("Zepto", "Groceries"),
        "com.bigbasket.mobileapp" to AppTargetInfo("BigBasket", "Groceries"),
        "in.dmart" to AppTargetInfo("DMart Ready", "Groceries"),

        // 🛍️ SHOPPING & E-COMMERCE
        "in.amazon.mShop.android.shopping" to AppTargetInfo("Amazon", "Shopping"),
        "com.amazon.mShop.android.shopping" to AppTargetInfo("Amazon", "Shopping"),
        "com.flipkart.android" to AppTargetInfo("Flipkart", "Shopping"),
        "com.myntra.android" to AppTargetInfo("Myntra", "Shopping"),
        "com.ril.ajio" to AppTargetInfo("AJIO", "Shopping"),
        "com.fsn.nykaa" to AppTargetInfo("Nykaa", "Shopping"),
        "com.meesho.supply" to AppTargetInfo("Meesho", "Shopping"),
        "com.tul.tatacliq" to AppTargetInfo("Tata Cliq", "Shopping"),
        "com.tatadigital.tcp" to AppTargetInfo("Tata Neu", "Shopping"),
        "com.snapdeal.main" to AppTargetInfo("Snapdeal", "Shopping"),

        // 🍿 ENTERTAINMENT & EVENTS
        "in.bookmyshow" to AppTargetInfo("BookMyShow", "Entertainment"),
        "com.insider.app" to AppTargetInfo("Paytm Insider", "Entertainment"),
        "com.pvr" to AppTargetInfo("PVR INOX", "Entertainment"),

        // ✈️ TRAVEL & HOTELS
        "com.makemytrip" to AppTargetInfo("MakeMyTrip", "Entertainment"),
        "com.goibibo" to AppTargetInfo("Goibibo", "Entertainment"),
        "com.cleartrip.android" to AppTargetInfo("Cleartrip", "Entertainment"),
        "com.agoda.mobile.consumer" to AppTargetInfo("Agoda", "Entertainment"),
        "com.oyo.consumer" to AppTargetInfo("OYO", "Entertainment"),

        // 🚕 CABS & COMMUTE
        "com.ubercab" to AppTargetInfo("Uber", "General"),
        "com.olacabs.customer" to AppTargetInfo("Ola", "General"),
        "com.rapido.passenger" to AppTargetInfo("Rapido", "General"),
        "in.juspay.nammayatri" to AppTargetInfo("Namma Yatri", "General")
    )

    data class AppTargetInfo(val appDisplayName: String, val category: String)

    override fun onCreate() {
        super.onCreate()
        instance = this
        startForegroundServiceWithNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startWatchdogLoop()
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "watchdog_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Budget WatchDog Tracker", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("WatchDog Active 🐕")
            .setContentText("Guarding your budget in real-time.")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1001, notification)
    }

    private fun startWatchdogLoop() {
        watchdogJob?.cancel()

        watchdogJob = serviceScope.launch {
            while (isActive) {
                val currentApp = getForegroundPackage()

                if (currentApp == null || currentApp == lastObservedPackage) {
                    delay(800)
                    continue
                }

                // 🚀 Bypassing Keyboard and SystemUI false-positives
                if (ignoredPackages.contains(currentApp) || currentApp == packageName) {
                    delay(800)
                    continue
                }

                lastObservedPackage = currentApp
                val isUpi = upiApps.contains(currentApp)
                val isTarget = targetApps.containsKey(currentApp)

                if (isTarget || isUpi) {
                    // It's a tracked app!
                    val targetInfo = targetApps[currentApp]

                    if (isUpi) {
                        if (lastTrackedApp != null && !upiApps.contains(lastTrackedApp)) {
                            // JUMPED FROM SHOPPING -> UPI
                            isPaymentInProgress = true
                            if (isOverlayCurrentlyShowing) {
                                withContext(Dispatchers.Main) { BudgetOverlayManager.removeOverlay() }
                                isOverlayCurrentlyShowing = false
                            }
                        } else if (targetInfo != null) {
                            // Opened an app like Paytm directly
                            lastTrackedApp = currentApp
                            isPaymentInProgress = true
                            showWarningOverlayIfNeeded(targetInfo)
                        }
                    } else if (targetInfo != null) {
                        // Opened a Shopping/Food app
                        lastTrackedApp = currentApp
                        isPaymentInProgress = false
                        showWarningOverlayIfNeeded(targetInfo)
                    }
                } else {
                    // SCENARIO 3: User TRULY left the app (e.g. Home Screen or WhatsApp)
                    if (isOverlayCurrentlyShowing) {
                        withContext(Dispatchers.Main) { BudgetOverlayManager.removeOverlay() }
                        isOverlayCurrentlyShowing = false
                    }

                    if (lastTrackedApp != null) {
                        // 🚀 FIX 2: Only show prompt if Master Switch is ON
                        if (AlertSettingsManager.isAlertsEnabled(applicationContext)) {
                            val appName = targetApps[lastTrackedApp]?.appDisplayName ?: "an app"
                            val category = targetApps[lastTrackedApp]?.category ?: "General" // 👈 GRAB CATEGORY
                            triggerPostSessionPopup(appName, category, isPaymentInProgress) // 👈 PASS CATEGORY
                        }

                        // Reset memory
                        lastTrackedApp = null
                        isPaymentInProgress = false
                    }
                }

                delay(800)
            }
        }
    }

    private suspend fun showWarningOverlayIfNeeded(targetInfo: AppTargetInfo) {
        if (!AlertSettingsManager.isAlertsEnabled(applicationContext)) return

        val allLimits = com.jsingh.XpenseMate.utils.SettingsManager.getCategoryBudgets(applicationContext)
        val realBudgetLimit = allLimits[targetInfo.category]?.toDouble() ?: 0.0

        // Remember: General category must have a budget limit set > 0 in your app for this to trigger!
        if (realBudgetLimit > 0.0) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)

            val spentThisMonth = AppDatabase.getDatabase(applicationContext)
                .transactionDao()
                .getTotalSpentForCategoryThisMonth(targetInfo.category, calendar.timeInMillis) ?: 0.0

            val alertThresholdPercentage = AlertSettingsManager.getAlertThreshold(applicationContext)
            val thresholdAmount = realBudgetLimit * (alertThresholdPercentage / 100.0)

            if (spentThisMonth >= thresholdAmount) {
                withContext(Dispatchers.Main) {
                    BudgetOverlayManager.showWarningOverlay(
                        context = applicationContext,
                        appName = targetInfo.appDisplayName,
                        category = targetInfo.category,
                        spentAmount = spentThisMonth,
                        budgetLimit = realBudgetLimit
                    )
                }
                isOverlayCurrentlyShowing = true
            }
        }
    }

    // 👈 Add "category: String" here
    private fun triggerPostSessionPopup(appName: String, category: String, wentToUpi: Boolean) {
        serviceScope.launch {
            delay(800)
            withContext(Dispatchers.Main) {
                // 👈 Pass "category" into the Overlay Manager
                BudgetOverlayManager.showPostPurchasePrompt(applicationContext, appName, category, wentToUpi)
                Log.d("XpenseMate_Watchdog", "Fired Post-Purchase Prompt Overlay for $appName")
            }
        }
    }

    private fun getForegroundPackage(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 10
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentForegroundApp: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentForegroundApp = event.packageName
            }
        }
        return currentForegroundApp
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }
}
@Composable
fun PostPurchaseDialog(
    appName: String,
    wentToUpi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val titleText = if (wentToUpi) "Payment Detected!" else "Session Ended"
    val messageText = if (wentToUpi) {
        "It looks like you just made a payment on $appName. Want to log this expense now to keep your budget accurate?"
    } else {
        "You were recently browsing $appName. Did you end up spending any money?"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, contentDescription = "Warning", tint = Color(0xFFFF3B30))
                Spacer(modifier = Modifier.width(8.dp))
                Text(titleText, fontWeight = FontWeight.Bold, color = Color(0xFFFF3B30))
            }
        },
        text = { Text(messageText, fontSize = 15.sp) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))) {
                Text("Yes, Log Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No, I didn't", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}