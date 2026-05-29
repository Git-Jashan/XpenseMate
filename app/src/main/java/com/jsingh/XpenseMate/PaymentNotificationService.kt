package com.jsingh.XpenseMate

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PaymentNotificationService : NotificationListenerService() {

    // 🚀 THE FULL MEGA-MAP (Every single app is preserved and categorized)
    private val validFinanceApps = mapOf(
        // UPI & Major Wallets
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

        // Public Sector Banks (PSUs)
        "com.sbi.SBIFreedomPlus" to Pair("SBI YONO", "Net Banking"),
        "com.sbi.upi" to Pair("BHIM SBI Pay", "UPI"),
        "com.bankofbaroda.mconnect" to Pair("BoB World", "Net Banking"),
        "com.pnb.mBanking" to Pair("PNB One", "Net Banking"),
        "com.unionbank.in" to Pair("Union Bank", "Net Banking"),
        "com.canarabank.mobility" to Pair("Canara ai1", "Net Banking"),
        "com.indianbank.indpay" to Pair("Indian Bank", "Net Banking"),

        // Private Banks
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

        // Neo-banks, Credit Cards & BNPL
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

    override fun onCreate() {
        super.onCreate()
        ExpenseEngine.initialize(this) // Keep AI Engine ready
        Log.d("XpenseMate_Service", "✅ Smart Mega-Tracker active!")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // GATE 1: Check if it's in our map, and extract BOTH variables!
        val appData = validFinanceApps[packageName] ?: return
        val appName = appData.first       // e.g., "GPay" or "CRED"
        val paymentMode = appData.second  // e.g., "UPI" or "Credit Card"

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val body = extras.getCharSequence("android.text")?.toString() ?: ""

        val fullMessageLower = "$title $body".lowercase()

        // GATE 2: Junk Filter
        val junkKeywords = listOf(
            "otp", "verification code",
            "failed", "declined", "unsuccessful", "bounced", "reversed"
        )
        if (junkKeywords.any { fullMessageLower.contains(it) }) {
            Log.d("XpenseMate_Service", "🗑️ Dropped: Hard failure or OTP")
            return
        }

        // GATE 3: Money Filter
        val hasMoney = fullMessageLower.contains(Regex("(?i)((?:rs\\.?|inr|₹|rupees)\\s*[0-9,]+\\.?[0-9]*)|([0-9,]+\\.?[0-9]*\\s*(?:rs\\.?|inr|₹|rupees))"))
        if (!hasMoney) {
            Log.d("XpenseMate_Service", "🗑️ Dropped: No money detected")
            return
        }

        Log.d("XpenseMate_Service", "💸 PROBABLE SPEND from $appName via $paymentMode")

        val cleanPayload = "$title - $body"

        // 🚀 THE NEW HANDOFF: Sending the Text + App Name + Payment Mode
        ExpenseEngine.processTransaction(this, cleanPayload, appName, paymentMode, sbn.postTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("XpenseMate_Service", "🔴 Service destroyed.")
    }
}