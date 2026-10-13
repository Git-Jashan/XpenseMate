package com.jsingh.XpenseMate.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PaymentNotificationService : NotificationListenerService() {

    companion object {
        private val validFinanceApps = mapOf(
            "com.google.android.apps.nbu.paisa.user" to Pair("GPay", "UPI"),
            "com.phonepe.app" to Pair("PhonePe", "UPI"),
            "net.one97.paytm" to Pair("Paytm", "UPI"),
            "in.amazon.mShop.android.shopping" to Pair("Amazon Pay", "UPI"),
            "in.org.npci.upiapp" to Pair("BHIM", "UPI"),
            "com.mobikwik_new" to Pair("MobiKwik", "UPI"),
            "com.freecharge.android" to Pair("Freecharge", "UPI"),
            "com.tatadigital.tcp" to Pair("Tata Neu", "UPI"),
            "com.myairtelapp" to Pair("Airtel Thanks", "UPI"),
            "com.samsung.android.spay" to Pair("Samsung Pay", "UPI"),

            "com.sbi.SBIFreedomPlus" to Pair("SBI YONO", "Net Banking"),
            "com.sbi.upi" to Pair("BHIM SBI Pay", "UPI"),
            "com.bankofbaroda.mconnect" to Pair("BoB World", "Net Banking"),
            "com.pnb.mBanking" to Pair("PNB One", "Net Banking"),
            "com.unionbank.in" to Pair("Union Bank", "Net Banking"),
            "com.canarabank.mobility" to Pair("Canara ai1", "Net Banking"),
            "com.indianbank.indpay" to Pair("Indian Bank", "Net Banking"),

            "com.snapwork.hdfc" to Pair("HDFC Bank", "Net Banking"),
            "com.hdfcbank.payzapp" to Pair("PayZapp", "UPI"),
            "com.csam.icici.bank.imobile" to Pair("ICICI iMobile", "Net Banking"),
            "com.icicibank.pockets" to Pair("ICICI Pockets", "UPI"),
            "com.axis.mobile" to Pair("Axis Bank", "Net Banking"),
            "com.msf.kbank.mobile" to Pair("Kotak Bank", "Net Banking"),
            "com.idfcfirstbank.dfa" to Pair("IDFC First", "Net Banking"),
            "com.fss.indus" to Pair("IndusInd Bank", "Net Banking"),
            "com.yesbank" to Pair("Yes Bank", "Net Banking"),
            "com.federalbank.fedmobile" to Pair("Federal Bank", "Net Banking"),

            "com.dreamplug.androidapp" to Pair("CRED", "Credit Card"),
            "tech.juspay.slice" to Pair("Slice", "Credit Card"),
            "com.fdc.onecard" to Pair("OneCard", "Credit Card"),
            "co.uni.app.play" to Pair("Uni Cards", "Credit Card"),
            "money.jupiter" to Pair("Jupiter", "Net Banking"),
            "com.epifi.paisa" to Pair("Fi Money", "Net Banking"),
            "com.naviapp" to Pair("Navi", "UPI"),
            "com.cheq.india" to Pair("Cheq", "Credit Card"),
            "com.lazypay.app" to Pair("LazyPay", "Pay Later"),
            "in.zestmoney.android" to Pair("ZestMoney", "Pay Later"),
            "com.postpe.app" to Pair("PostPe", "Pay Later")
        )
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)

     private var lastProcessedPayload = ""
    private var lastProcessedTime = 0L

    override fun onCreate() {
        super.onCreate()
        Log.d("XpenseMate_Service", "✅ Smart Mega-Tracker active!")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        val appData = validFinanceApps[packageName] ?: return

        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            return
        }

        val appName = appData.first
        val paymentMode = appData.second

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val body = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isEmpty() && body.isEmpty()) return

        val cleanPayload = "$title - $body"

        val now = System.currentTimeMillis()
        if (cleanPayload == lastProcessedPayload && (now - lastProcessedTime) < 3000) {
            Log.d("XpenseMate_Service", "🚫 Blocked duplicate notification update from $appName")
            return
        }
        lastProcessedPayload = cleanPayload
        lastProcessedTime = now

        Log.d("XpenseMate_Service", "📡 Notification captured from $appName. Handoff to background pipeline.")

        serviceScope.launch {
            try {
                val request = TransactionRequest(
                    rawText = cleanPayload,
                    source = TransactionSource.NOTIFICATION,
                    appName = appName,
                    paymentMode = paymentMode,
                    timestamp = sbn.postTime,
                    sender = appName
                )

                val engine = ExpenseEngine(applicationContext)
                val result = engine.processTransaction(request)

                Log.d("XpenseMate_Service", "Engine Result: $result")

            } catch (e: Exception) {
                Log.e("XpenseMate_Service", "❌ Background pipeline failed: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("XpenseMate_Service", "🔴 Service destroyed.")
    }
}