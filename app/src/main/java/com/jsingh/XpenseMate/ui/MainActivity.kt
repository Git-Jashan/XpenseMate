package com.jsingh.XpenseMate.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.jsingh.XpenseMate.SidebarScreen
import com.jsingh.XpenseMate.data.AppDatabase
import com.jsingh.XpenseMate.data.Transaction
import com.jsingh.XpenseMate.network.SupabaseNetwork
import com.jsingh.XpenseMate.service.ExpenseEngine
import com.jsingh.XpenseMate.service.ParseResult
import com.jsingh.XpenseMate.service.TransactionRequest
import com.jsingh.XpenseMate.service.TransactionSource
import com.jsingh.XpenseMate.ui.components.*
import com.jsingh.XpenseMate.ui.sidebar.*
import com.jsingh.XpenseMate.ui.tabs.*
import com.jsingh.XpenseMate.ui.theme.XpenseMateTheme
import com.jsingh.XpenseMate.utils.PermissionRouter
import com.jsingh.XpenseMate.utils.SecurityManager
import com.jsingh.XpenseMate.utils.ThemeManager
import com.jsingh.XpenseMate.viewmodel.HomeViewModel
import com.jsingh.XpenseMate.viewmodel.HomeViewModelFactory
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import com.jsingh.XpenseMate.utils.SubscriptionDetector

class MainActivity : FragmentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext) }

    private var pendingSharedTransaction by mutableStateOf<Transaction?>(null)
    private var pendingPdfUri by mutableStateOf<Uri?>(null)
    private var showPdfPasswordDialog by mutableStateOf(false)

    private var showPostPurchaseDialog by mutableStateOf(false)
    private var recentAppName by mutableStateOf("")
    private var wentToUpi by mutableStateOf(false)
    private var shouldOpenAddDialog by mutableStateOf(false)

    private var prefillTitle by mutableStateOf("")
    private var prefillCategory by mutableStateOf("General")

    private fun handleWatchDogIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("SHOW_POST_PURCHASE_DIALOG", false)) {
                recentAppName = it.getStringExtra("RECENT_APP_NAME") ?: "an app"
                wentToUpi = it.getBooleanExtra("WENT_TO_UPI", false)
                showPostPurchaseDialog = true
                it.removeExtra("SHOW_POST_PURCHASE_DIALOG")
            }
            if (it.getBooleanExtra("SHOW_ADD_TRANSACTION", false)) {
                prefillTitle = it.getStringExtra("PREFILL_TITLE") ?: ""
                prefillCategory = it.getStringExtra("PREFILL_CATEGORY") ?: "General"
                shouldOpenAddDialog = true
                it.removeExtra("SHOW_ADD_TRANSACTION")
                it.removeExtra("PREFILL_TITLE")
                it.removeExtra("PREFILL_CATEGORY")
            }
        }
    }

    private fun handleShareIntent(currentIntent: Intent?) {
        if (currentIntent?.action == Intent.ACTION_SEND) {
            if (currentIntent.type?.startsWith("image/") == true) {
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    currentIntent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    currentIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                }
                if (uri != null) processSharedImage(this, uri)
            } else if (currentIntent.type == "application/pdf") {
                val pdfUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    currentIntent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    currentIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                }
                if (pdfUri != null) {
                    pendingPdfUri = pdfUri
                    showPdfPasswordDialog = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val securityPrefs = getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)
        if (securityPrefs.getBoolean("secure_display", false)) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        PDFBoxResourceLoader.init(applicationContext)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), 101)
        }

        handleShareIntent(intent)
        handleWatchDogIntent(intent)

        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as android.app.Application
            val appDatabase = AppDatabase.getDatabase(app)
            val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = HomeViewModelFactory(
                    application = app,
                    transactionDao = appDatabase.transactionDao(),
                    subscriptionDao = appDatabase.subscriptionDao()
                )
            )

            LaunchedEffect(shouldOpenAddDialog) {
                if (shouldOpenAddDialog) {
                    homeViewModel.updateAddDialogState(true)
                    shouldOpenAddDialog = false
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

            var hasNotifPermission by remember { mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)) }
            var hasBatteryExemption by remember { mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName)) }
            var hasUsageAccess by remember { mutableStateOf(PermissionRouter.hasUsageStatsPermission(context)) }
            var hasOverlayAccess by remember { mutableStateOf(PermissionRouter.hasOverlayPermission(context)) }
            var userFinishedSetup by rememberSaveable { mutableStateOf(false) }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        hasNotifPermission = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
                        hasBatteryExemption = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                        hasUsageAccess = PermissionRouter.hasUsageStatsPermission(context)
                        hasOverlayAccess = PermissionRouter.hasOverlayPermission(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode(context)) }

            XpenseMateTheme(darkTheme = isDarkMode) {

                val needsPermissions = !hasNotifPermission || !hasBatteryExemption || !hasUsageAccess || !hasOverlayAccess
                val sessionStatus by SupabaseNetwork.client.auth.sessionStatus.collectAsState()

                var isUnlocked by remember { mutableStateOf(!SecurityManager.isAppLockEnabled(context)) }
                var showQuickAddWhileLocked by rememberSaveable { mutableStateOf(false) }

                var pendingSuggestedMerchant by remember { mutableStateOf("") }
                var pendingSuggestedAmount by remember { mutableDoubleStateOf(0.0) }
                var showRecurringSuggestion by remember { mutableStateOf(false) }

                var showAddSubscriptionDialog by rememberSaveable { mutableStateOf(false) }
                var prefillSubMerchant by rememberSaveable { mutableStateOf("") }
                var prefillSubAmount by rememberSaveable { mutableDoubleStateOf(0.0) }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_STOP) {
                            SecurityManager.saveBackgroundTime(context)
                        } else if (event == Lifecycle.Event.ON_START) {
                            if (SecurityManager.shouldLockOnReturn(context)) {
                                isUnlocked = false
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                if (!isUnlocked) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LockScreenView(
                            onUnlockSuccess = { isUnlocked = true },
                            onQuickAddClick = { showQuickAddWhileLocked = true }
                        )

                        if (showQuickAddWhileLocked) {
                            TransactionPopup(
                                initialTitle = "",
                                initialCategory = "General",
                                categories = homeViewModel.categories.collectAsState().value,
                                onDismiss = { showQuickAddWhileLocked = false },
                                onSave = { savedTitle, savedAmount, savedIsExpense, savedCategory ->
                                    val now = java.time.LocalDateTime.now()
                                    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                                    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                                    val amountDouble = savedAmount.toDoubleOrNull() ?: 0.0

                                    homeViewModel.saveTransaction(savedTitle, amountDouble, savedIsExpense, savedCategory, now.format(dateFormatter), now.format(timeFormatter))
                                    showQuickAddWhileLocked = false

                                    val currentTxns = homeViewModel.transactions.value
                                    if (SubscriptionDetector.isLikelyRecurring(savedTitle, amountDouble, currentTxns)) {
                                        pendingSuggestedMerchant = savedTitle
                                        pendingSuggestedAmount = amountDouble
                                        showRecurringSuggestion = true
                                    } else {
                                        Toast.makeText(context, "Expense logged securely! 🔒", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                } else {
                    when (sessionStatus) {
                        is SessionStatus.Initializing -> {
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        is SessionStatus.Authenticated -> {
                            if (needsPermissions && !userFinishedSetup) {
                                PermissionScreen(
                                    hasNotifPermission = hasNotifPermission,
                                    hasBatteryExemption = hasBatteryExemption,
                                    onFinish = { userFinishedSetup = true }
                                )
                            } else {
                                LaunchedEffect(Unit) {
                                    try {
                                        val serviceIntent = Intent(context, com.jsingh.XpenseMate.service.BudgetWatchdogService::class.java)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(serviceIntent)
                                        } else {
                                            context.startService(serviceIntent)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("XpenseMate", "Failed to auto-start WatchDog", e)
                                    }
                                }

                                var showQrScanner by rememberSaveable { mutableStateOf(false) }
                                var selectedItem by rememberSaveable { mutableIntStateOf(0) }
                                var activeSidebarScreen by rememberSaveable { mutableStateOf<SidebarScreen?>(null) }

                                val showAddDialog by homeViewModel.showAddDialog.collectAsState()
                                val dailyLimit by homeViewModel.dailyLimit.collectAsState()
                                val monthlyLimit by homeViewModel.monthlyLimit.collectAsState()
                                val categories by homeViewModel.categories.collectAsState()
                                val categoryLimits by homeViewModel.categoryLimits.collectAsState()
                                val mySubscriptions by homeViewModel.subscriptions.collectAsState()
                                val transactions by homeViewModel.transactions.collectAsState()
                                val coroutineScope = rememberCoroutineScope()

                                var showPaymentSheet by rememberSaveable { mutableStateOf(false) }
                                var showConfirmationDialog by remember { mutableStateOf(false) }
                                var pendingAmount by remember { mutableDoubleStateOf(0.0) }
                                var pendingName by remember { mutableStateOf("") }
                                var lastAttemptedVpa by remember { mutableStateOf("") }
                                var lastAttemptedAmount by remember { mutableDoubleStateOf(0.0) }

                                var scannedMerchantName by rememberSaveable { mutableStateOf("") }
                                var scannedMerchantVpa by rememberSaveable { mutableStateOf("") }
                                var scannedAmount by rememberSaveable { mutableStateOf("") }
                                var showManualUpiInput by remember { mutableStateOf(false) }
                                var scannedMerchantCode by remember { mutableStateOf("") }

                                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                                val userEmail by homeViewModel.currentUserEmail.collectAsState()
                                val showAccountDialog by homeViewModel.showAccountDialog.collectAsState()

                                androidx.activity.compose.BackHandler(enabled = activeSidebarScreen != null || drawerState.isOpen) {
                                    if (drawerState.isOpen) {
                                        coroutineScope.launch { drawerState.close() }
                                    } else if (activeSidebarScreen != null) {
                                        activeSidebarScreen = null
                                    }
                                }

                                if (showAccountDialog) {
                                    AccountDialog(
                                        userEmail = userEmail,
                                        transactionCount = transactions.size,
                                        onDismiss = { homeViewModel.updateAccountDialogState(false) },
                                        onLogoutClick = { homeViewModel.logOut { } }
                                    )
                                }

                                ModalNavigationDrawer(
                                    drawerState = drawerState,
                                    drawerContent = {
                                        AppDrawer(
                                            activeRoute = activeSidebarScreen,
                                            userEmail = userEmail,
                                            onRouteSelected = { route -> activeSidebarScreen = route },
                                            onAccountClick = { homeViewModel.updateAccountDialogState(true) },
                                            closeDrawer = { coroutineScope.launch { drawerState.close() } }
                                        )
                                    }
                                ){
                                    @OptIn(ExperimentalMaterial3Api::class)
                                    Scaffold(
                                        modifier = Modifier.fillMaxSize(),
                                        topBar = {
                                            TopAppBar(
                                                colors = TopAppBarDefaults.topAppBarColors(
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                    titleContentColor = MaterialTheme.colorScheme.primary,
                                                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                                                    actionIconContentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                title = { Text("XpenseMate", fontSize = 22.sp, fontWeight = FontWeight.SemiBold) },
                                                navigationIcon = {
                                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                                        Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                                                    }
                                                }
                                            )
                                        },
                                        bottomBar = {
                                            AppBottomBar(
                                                selectedItem = selectedItem,
                                                onItemSelected = { selectedItem = it; activeSidebarScreen = null },
                                                onAddManualClick = { homeViewModel.updateAddDialogState(true) }
                                            )
                                        }
                                    ) { innerPadding ->
                                        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                                            Box(modifier = Modifier.weight(1f)) {

                                                when (selectedItem) {
                                                    0 -> HomeTab(
                                                        transactions = transactions,
                                                        subscriptions = mySubscriptions,
                                                        dailyLimit = dailyLimit,
                                                        monthlyLimit = monthlyLimit,
                                                        isIncognito = false,
                                                        onAddManualClick = { homeViewModel.updateAddDialogState(true) },
                                                        onPayAnyoneClick = { showManualUpiInput = true },
                                                        onMarkSubscriptionPaid = { sub -> homeViewModel.markSubscriptionPaid(sub) }
                                                    )
                                                    1 -> HistoryTab(
                                                        transactions = transactions,
                                                        categories = categories,
                                                        onUpdateTransaction = { updatedTx -> homeViewModel.updateTransaction(updatedTx) }
                                                    )
                                                    2 -> AnalyticsTab(
                                                        transactions = transactions,
                                                        categoryBudgets = categoryLimits,
                                                        dailyLimit = dailyLimit.toFloat()
                                                    )
                                                    3 -> AccountsTab()
                                                }

                                                if (showPostPurchaseDialog) {
                                                    PostPurchaseDialog(
                                                        appName = recentAppName,
                                                        wentToUpi = wentToUpi,
                                                        onDismiss = { showPostPurchaseDialog = false },
                                                        onConfirm = {
                                                            showPostPurchaseDialog = false
                                                            homeViewModel.updateAddDialogState(true)
                                                        }
                                                    )
                                                }

                                                if (showQrScanner) {
                                                    QrScannerDialog(
                                                        onDismiss = { showQrScanner = false },
                                                        onQrScanned = { name, vpa, mc, amount ->
                                                            scannedMerchantName = name
                                                            scannedMerchantVpa = vpa
                                                            scannedMerchantCode = mc
                                                            scannedAmount = amount
                                                            showQrScanner = false
                                                            showPaymentSheet = true
                                                        }
                                                    )
                                                }

                                                if (showPaymentSheet) {
                                                    UpiPaymentDialog(
                                                        payeeName = scannedMerchantName,
                                                        payeeVpa = scannedMerchantVpa,
                                                        scannedAmount = scannedAmount,
                                                        merchantCode = scannedMerchantCode,
                                                        onDismiss = { showPaymentSheet = false },
                                                        onPaymentAttempted = { finalAmount, finalName, isExplicitSuccess ->
                                                            showPaymentSheet = false
                                                            when (isExplicitSuccess) {
                                                                true -> {
                                                                    val calendar = java.util.Calendar.getInstance()
                                                                    val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
                                                                    val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(calendar.time)
                                                                    homeViewModel.saveTransaction(finalName, finalAmount, true, "UPI Scan", currentDate, currentTime)
                                                                    Toast.makeText(context, "Logged perfectly!", Toast.LENGTH_SHORT).show()
                                                                }
                                                                false -> Toast.makeText(context, "Payment failed in GPay.", Toast.LENGTH_SHORT).show()
                                                                null -> {
                                                                    if (scannedMerchantVpa == lastAttemptedVpa && finalAmount == lastAttemptedAmount) {
                                                                        pendingAmount = finalAmount
                                                                        pendingName = finalName
                                                                        showConfirmationDialog = true
                                                                    } else {
                                                                        lastAttemptedVpa = scannedMerchantVpa
                                                                        lastAttemptedAmount = finalAmount
                                                                        val calendar = java.util.Calendar.getInstance()
                                                                        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
                                                                        val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(calendar.time)
                                                                        homeViewModel.saveTransaction(finalName, finalAmount, true, "UPI Scan", currentDate, currentTime)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    )
                                                }

                                                if (showManualUpiInput) {
                                                    val upiExpenses = transactions.filter { it.isExpense }
                                                    val recentList = upiExpenses.distinctBy { it.title.lowercase().trim() }.take(15).map { AppContact(it.title, it.title) }
                                                    val frequentList = upiExpenses.groupingBy { it.title.trim() }.eachCount().toList().sortedByDescending { it.second }.take(15).map { AppContact(it.first, it.first) }

                                                    ManualUpiInputDialog(
                                                        recentContacts = recentList,
                                                        frequentContacts = frequentList,
                                                        onDismiss = { showManualUpiInput = false },
                                                        onContinue = { enteredUpiId ->
                                                            showManualUpiInput = false
                                                            scannedMerchantName = "Verify name in UPI App"
                                                            scannedMerchantVpa = enteredUpiId
                                                            scannedAmount = ""
                                                            showPaymentSheet = true
                                                        }
                                                    )
                                                }

                                                if (showAddDialog) {
                                                    TransactionPopup(
                                                        initialTitle = prefillTitle,
                                                        initialCategory = prefillCategory,
                                                        onDismiss = {
                                                            homeViewModel.updateAddDialogState(false)
                                                            prefillTitle = ""
                                                            prefillCategory = "General"
                                                        },
                                                        categories = categories,
                                                        onSave = { savedTitle, savedAmount, savedIsExpense, savedCategory ->
                                                            val now = java.time.LocalDateTime.now()
                                                            val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                                                            val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                                                            val amountDouble = savedAmount.toDoubleOrNull() ?: 0.0
                                                            homeViewModel.saveTransaction(savedTitle, amountDouble, savedIsExpense, savedCategory, now.format(dateFormatter), now.format(timeFormatter))

                                                            if (SubscriptionDetector.isLikelyRecurring(savedTitle, amountDouble, transactions)) {
                                                                pendingSuggestedMerchant = savedTitle
                                                                pendingSuggestedAmount = amountDouble
                                                                showRecurringSuggestion = true
                                                            } else {
                                                                Toast.makeText(context, "Expense Logged", Toast.LENGTH_SHORT).show()
                                                            }

                                                            prefillTitle = ""
                                                            prefillCategory = "General"
                                                        }
                                                    )
                                                }

                                                if (showPdfPasswordDialog && pendingPdfUri != null) {
                                                    PdfPasswordDialog(
                                                        context = context,
                                                        pdfUri = pendingPdfUri!!,
                                                        onDismiss = { showPdfPasswordDialog = false }
                                                    )
                                                }

                                                when (activeSidebarScreen) {
                                                    SidebarScreen.BUDGET -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                        BudgetSettingsView(
                                                            currentDaily = dailyLimit.toFloat(),
                                                            currentMonthly = monthlyLimit.toFloat(),
                                                            categories = categories,
                                                            currentCategoryLimits = categoryLimits,
                                                            onBackClick = { activeSidebarScreen = null },
                                                            onCategoriesUpdated = { newList -> homeViewModel.saveCategories(newList) },
                                                            onSaveBudgets = { newD, newM, newLimits ->
                                                                homeViewModel.saveBudgets(newD.toDouble(), newM.toDouble(), newLimits)
                                                                activeSidebarScreen = null
                                                            }
                                                        )
                                                    }
                                                    SidebarScreen.DESIGN -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                        DesignSettingsView(isDarkMode, onBackClick = { activeSidebarScreen = null }) { newValue ->
                                                            ThemeManager.saveTheme(context, newValue)
                                                            isDarkMode = newValue
                                                        }
                                                    }
                                                    SidebarScreen.BUDGET_ALERTS -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                        BudgetAlertsSettingsView(onBackClick = { activeSidebarScreen = null })
                                                    }
                                                    SidebarScreen.RECURRING -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                        RecurringPaymentsView(mySubscriptions, onBackClick = { activeSidebarScreen = null }, onSaveNew = { m, a, f, b, c -> homeViewModel.addManualSubscription(m, a, f, b, c) }) { subToDelete -> homeViewModel.deleteSubscription(subToDelete.id) }
                                                    }
                                                    SidebarScreen.EXPORT -> {
                                                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                            ExportDataView(transactions = transactions, onBackClick = { activeSidebarScreen = null })
                                                        }
                                                    }
                                                    SidebarScreen.PRIVACY -> {
                                                        PrivacySecurityView(
                                                            onBackClick = { activeSidebarScreen = null },
                                                            onWipeData = {} // 🚀 SAFE EMPTY HANDLER
                                                        )
                                                    }
                                                    null -> {}
                                                }

                                                pendingSharedTransaction?.let { txn ->
                                                    TransactionDetailsCard(
                                                        transaction = txn,
                                                        categories = categories,
                                                        isNew = true,
                                                        onDismiss = { pendingSharedTransaction = null },
                                                        onCategoryChange = { finalTxn ->
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                val engine = ExpenseEngine(context)
                                                                engine.saveTransaction(finalTxn)
                                                                withContext(Dispatchers.Main) {
                                                                    pendingSharedTransaction = null
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            LoginScreen(
                                viewModel = homeViewModel,
                                onLoginSuccess = { homeViewModel.verifyUserAndCache(context) }
                            )
                        }
                    }
                } // 🛑 END OF APP WRAPPER

                if (showRecurringSuggestion) {
                    RecurringSuggestionDialog(
                        merchant = pendingSuggestedMerchant,
                        amount = pendingSuggestedAmount,
                        onDismiss = { showRecurringSuggestion = false },
                        onConfirm = { _ ->
                            showRecurringSuggestion = false
                            prefillSubMerchant = pendingSuggestedMerchant
                            prefillSubAmount = pendingSuggestedAmount
                            showAddSubscriptionDialog = true
                        }
                    )
                }

                if (showAddSubscriptionDialog) {
                    AddSubscriptionDialog(
                        initialMerchant = prefillSubMerchant,
                        initialAmount = if (prefillSubAmount > 0) prefillSubAmount.toString() else "",
                        onDismiss = { showAddSubscriptionDialog = false },
                        onSave = { merchant, amount, frequency, billingDay, category ->
                            homeViewModel.addManualSubscription(merchant, amount, frequency, billingDay, category)
                            showAddSubscriptionDialog = false
                            Toast.makeText(context, "$merchant added to Recurring!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    private fun processSharedImage(context: Context, imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                }

                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, originalBitmap.width * 3, originalBitmap.height * 3, true)
                val invertedBitmap = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, scaledBitmap.config ?: Bitmap.Config.ARGB_8888)
                val canvas = Canvas(invertedBitmap)
                val paint = Paint()

                val colorMatrix = ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
                canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

                val image = InputImage.fromBitmap(invertedBitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val visionText: com.google.mlkit.vision.text.Text = recognizer.process(image).await()

                val engine = ExpenseEngine(context)
                val request = TransactionRequest(
                    rawText = visionText.text,
                    source = TransactionSource.SCREENSHOT,
                    appName = "SCREENSHOT",
                    paymentMode = "UPI",
                    timestamp = System.currentTimeMillis()
                )

                val result = engine.processTransaction(request)

                withContext(Dispatchers.Main) {
                    when (result) {
                        is ParseResult.NeedsReview -> {
                            pendingSharedTransaction = result.transactions.firstOrNull()
                            Toast.makeText(context, "Review transaction details", Toast.LENGTH_SHORT).show()
                        }
                        is ParseResult.Saved -> {
                            Toast.makeText(context, "Screenshot processed & saved!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(context, "Could not extract transaction data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("XpenseMate_OCR", "Failed to process image", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error processing screenshot: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
        handleWatchDogIntent(intent)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF3B30).copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = titleText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFFF3B30)
                )
            }
        },
        text = {
            Text(messageText, fontSize = 15.sp)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF3B30),
                    contentColor = Color.White
                )
            ) {
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