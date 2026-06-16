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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.jsingh.XpenseMate.data.AppDatabase
import com.jsingh.XpenseMate.data.Transaction
import com.jsingh.XpenseMate.service.ExpenseEngine
import com.jsingh.XpenseMate.ui.components.AppBottomBar
import com.jsingh.XpenseMate.ui.components.AppDrawer
import com.jsingh.XpenseMate.ui.components.LoginScreen
import com.jsingh.XpenseMate.ui.components.ManualUpiInputDialog
import com.jsingh.XpenseMate.ui.components.PermissionScreen
import com.jsingh.XpenseMate.ui.components.QrScannerDialog
import com.jsingh.XpenseMate.ui.components.TransactionDetailsCard
import com.jsingh.XpenseMate.ui.components.TransactionPopup
import com.jsingh.XpenseMate.ui.components.UpiPaymentDialog
import com.jsingh.XpenseMate.ui.sidebar.AccountDialog
import com.jsingh.XpenseMate.ui.sidebar.BudgetSettingsView
import com.jsingh.XpenseMate.ui.sidebar.CategorySettingsView
import com.jsingh.XpenseMate.ui.sidebar.DesignSettingsView
import com.jsingh.XpenseMate.ui.sidebar.RecurringPaymentsView
import com.jsingh.XpenseMate.ui.tabs.AccountsTab
import com.jsingh.XpenseMate.ui.tabs.AnalyticsTab
import com.jsingh.XpenseMate.ui.tabs.HistoryTab
import com.jsingh.XpenseMate.ui.tabs.HomeTab
import com.jsingh.XpenseMate.ui.theme.XpenseMateTheme
import com.jsingh.XpenseMate.utils.ThemeManager
import com.jsingh.XpenseMate.viewmodel.HomeViewModel
import com.jsingh.XpenseMate.viewmodel.HomeViewModelFactory
import com.jsingh.XpenseMate.network.SupabaseNetwork
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.jsingh.XpenseMate.SidebarScreen

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext) }
    private var pendingSharedTransaction by mutableStateOf<Transaction?>(null)
    private var pendingPdfUri by mutableStateOf<Uri?>(null)
    private var showPdfPasswordDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PDFBoxResourceLoader.init(applicationContext)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                ),
                101
            )
        }

        handleShareIntent(intent)

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

            val lifecycleOwner = LocalLifecycleOwner.current
            val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

            var hasNotifPermission by remember {
                mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName))
            }
            var hasBatteryExemption by remember {
                mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
            }
            var userFinishedSetup by rememberSaveable { mutableStateOf(false) }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        hasNotifPermission = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
                        hasBatteryExemption = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode(context)) }

            XpenseMateTheme(darkTheme = isDarkMode) {

                val needsPermissions = !hasNotifPermission || !hasBatteryExemption

                // 🚀 THE FIX: Listen directly to Supabase's live memory!
                val sessionStatus by SupabaseNetwork.client.auth.sessionStatus.collectAsState()

                when (sessionStatus) {

                    // STATE 1: Reading the Hard Drive
                    is SessionStatus.Initializing -> {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // STATE 2: Found the Saved Wristband!
                    is SessionStatus.Authenticated -> {

                        // Check if they still need to do the setup permissions
                        if (needsPermissions && !userFinishedSetup) {
                            PermissionScreen(
                                hasNotifPermission = hasNotifPermission,
                                hasBatteryExemption = hasBatteryExemption,
                                onFinish = { userFinishedSetup = true }
                            )
                        } else {
                            // --- THE MAIN DASHBOARD ---
                            var showQrScanner by rememberSaveable { mutableStateOf(false) }
                            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission()
                            ) { isGranted ->
                                if (isGranted) showQrScanner = true
                                else Toast.makeText(context, "Camera required for QR", Toast.LENGTH_SHORT).show()
                            }

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

                            if (showAccountDialog) {
                                AccountDialog(
                                    userEmail = userEmail,
                                    transactionCount = transactions.size,
                                    onDismiss = { homeViewModel.updateAccountDialogState(false) },
                                    onLogoutClick = {
                                        // 🚀 Logout triggers the listener to automatically flip back to LoginScreen
                                        homeViewModel.logOut { }
                                    }
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
                                                navigationIconContentColor = MaterialTheme.colorScheme.primary
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
                                            onItemSelected = { selectedItem = it },
                                            onAddClick = {
                                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                    showQrScanner = true
                                                } else {
                                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                }
                                            }
                                        )
                                    }
                                ) { innerPadding ->
                                    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                                        Box(modifier = Modifier.weight(1f)) {

                                            // Tab Routing
                                            when (selectedItem) {
                                                0 -> HomeTab(
                                                    transactions = transactions,
                                                    subscriptions = mySubscriptions,
                                                    dailyLimit = dailyLimit,
                                                    monthlyLimit = monthlyLimit,
                                                    onAddManualClick = { homeViewModel.updateAddDialogState(true) },
                                                    onPayAnyoneClick = { showManualUpiInput = true }
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

                                            // Dialogs & Sheets
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
                                                val recentList = upiExpenses.distinctBy { it.title.lowercase().trim() }.take(15).map { com.jsingh.XpenseMate.ui.components.AppContact(it.title, it.title) }
                                                val frequentList = upiExpenses.groupingBy { it.title.trim() }.eachCount().toList().sortedByDescending { it.second }.take(15).map { com.jsingh.XpenseMate.ui.components.AppContact(it.first, it.first) }

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
                                                    onDismiss = { homeViewModel.updateAddDialogState(false) },
                                                    categories = categories,
                                                    onSave = { savedTitle, savedAmount, savedIsExpense, savedCategory ->
                                                        val now = java.time.LocalDateTime.now()
                                                        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                                                        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                                                        val amountDouble = savedAmount.toDoubleOrNull() ?: 0.0
                                                        homeViewModel.saveTransaction(savedTitle, amountDouble, savedIsExpense, savedCategory, now.format(dateFormatter), now.format(timeFormatter))
                                                    }
                                                )
                                            }

                                            // Sidebar Routing
                                            when (activeSidebarScreen) {
                                                SidebarScreen.BUDGET -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                    BudgetSettingsView(dailyLimit.toFloat(), monthlyLimit.toFloat(), categories, categoryLimits, onBackClick = { activeSidebarScreen = null }) { newD, newM, newLimits ->
                                                        homeViewModel.saveBudgets(newD.toDouble(), newM.toDouble(), newLimits)
                                                        activeSidebarScreen = null
                                                    }
                                                }
                                                SidebarScreen.DESIGN -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                    DesignSettingsView(isDarkMode, onBackClick = { activeSidebarScreen = null }) { newValue ->
                                                        ThemeManager.saveTheme(context, newValue)
                                                        isDarkMode = newValue
                                                    }
                                                }
                                                SidebarScreen.CATEGORY -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                    CategorySettingsView(categories, onBackClick = { activeSidebarScreen = null }) { newList -> homeViewModel.saveCategories(newList) }
                                                }
                                                SidebarScreen.RECURRING -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                    RecurringPaymentsView(mySubscriptions, onBackClick = { activeSidebarScreen = null }, onSaveNew = { m, a, f, b, c -> homeViewModel.addManualSubscription(m, a, f, b, c) }) { subToDelete -> homeViewModel.deleteSubscription(subToDelete.id) }
                                                }
                                                null -> {}
                                            }

                                            // Shared Screenshot Parsing UI
                                            pendingSharedTransaction?.let { txn ->
                                                TransactionDetailsCard(
                                                    transaction = txn,
                                                    categories = categories,
                                                    isNew = true,
                                                    onDismiss = { pendingSharedTransaction = null },
                                                    onCategoryChange = { finalTxn ->
                                                        ExpenseEngine.saveTransaction(finalTxn)
                                                        pendingSharedTransaction = null
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
                            onLoginSuccess = {
                                homeViewModel.verifyUserAndCache(context)
                            }
                        )
                    }
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

                withContext(Dispatchers.Main) {
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            CoroutineScope(Dispatchers.IO).launch {
                                ExpenseEngine.processTransaction(
                                    context = context,
                                    rawText = visionText.text,
                                    appName = "SCREENSHOT",
                                    paymentMode = "UPI",
                                    timestamp = System.currentTimeMillis()
                                ) { parsedList ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if (parsedList != null && parsedList.isNotEmpty()) {
                                            pendingSharedTransaction = parsedList[0]
                                        } else {
                                            pendingSharedTransaction = null
                                            Toast.makeText(context, "Could not find a valid transaction.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e -> Log.e("XpenseMate_OCR", "Failed to read text", e) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
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
}