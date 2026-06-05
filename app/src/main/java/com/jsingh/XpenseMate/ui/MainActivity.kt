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
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
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
import com.jsingh.XpenseMate.ui.components.PermissionScreen
import com.jsingh.XpenseMate.ui.components.TransactionDetailsCard
import com.jsingh.XpenseMate.ui.components.TransactionPopup
import com.jsingh.XpenseMate.ui.sidebar.BudgetSettingsView
import com.jsingh.XpenseMate.ui.sidebar.CategorySettingsView
import com.jsingh.XpenseMate.ui.sidebar.DesignSettingsView
import com.jsingh.XpenseMate.ui.sidebar.RecurringPaymentsView
import com.jsingh.XpenseMate.ui.tabs.AccountsTab
import com.jsingh.XpenseMate.ui.tabs.AnalyticsTab
import com.jsingh.XpenseMate.ui.tabs.HistoryTab
import com.jsingh.XpenseMate.ui.tabs.HomeTab
import com.jsingh.XpenseMate.ui.theme.XpenseMateTheme
import com.jsingh.XpenseMate.utils.SettingsManager
import com.jsingh.XpenseMate.utils.ThemeManager
import com.jsingh.XpenseMate.utils.addManualSubscription
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {


    private val db by lazy { AppDatabase.getDatabase(applicationContext) }
    private var pendingSharedTransaction by mutableStateOf<Transaction?>(null)
    private var pendingPdfUri by mutableStateOf<Uri?>(null)
    private var showPdfPasswordDialog by mutableStateOf(false)
    enum class SidebarScreen {
        BUDGET, DESIGN, CATEGORY, RECURRING
    }


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
            val mySubscriptions by db.subscriptionDao().getActiveSubscriptions()
                .collectAsState(initial = emptyList())
            var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode(context)) }
            XpenseMateTheme(darkTheme = isDarkMode) {
                //APP STATE
                var selectedItem by remember { mutableIntStateOf(0) }
                var showAddDialog by remember { mutableStateOf(false) }
                // 1. Grab the values from the phone's memory once on startup
                val initialLimits = remember { SettingsManager.getBudgets(context) }
                var dailyLimitState by remember { mutableStateOf(initialLimits.first.toDouble()) }
                var monthlyLimitState by remember { mutableStateOf(initialLimits.second.toDouble()) }
                var activeSidebarScreen by remember { mutableStateOf<SidebarScreen?>(null) }
                var masterCategoryList by remember {
                    mutableStateOf(
                        SettingsManager.getCategories(
                            context
                        ).toList()
                    )
                }
                var masterCategoryLimits by remember {
                    mutableStateOf(
                        SettingsManager.getCategoryBudgets(
                            context
                        )
                    )
                }

                val transactions by db.transactionDao().getAllTransactions()
                    .collectAsState(initial = emptyList())

                val coroutineScope = rememberCoroutineScope()

                val drawerState = rememberDrawerState(
                    initialValue = DrawerValue.Closed
                )

                val lifecycleOwner = LocalLifecycleOwner.current
                val powerManager =
                    remember { context.getSystemService(POWER_SERVICE) as PowerManager }

                var hasNotifPermission by remember {
                    mutableStateOf(
                        NotificationManagerCompat.getEnabledListenerPackages(context)
                            .contains(context.packageName)
                    )
                }
                var hasBatteryExemption by remember {
                    mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
                }
                var userFinishedSetup by remember { mutableStateOf(false) }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasNotifPermission =
                                NotificationManagerCompat.getEnabledListenerPackages(context)
                                    .contains(context.packageName)
                            hasBatteryExemption =
                                powerManager.isIgnoringBatteryOptimizations(context.packageName)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                if (hasNotifPermission && (hasBatteryExemption || userFinishedSetup)) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppDrawer(
                                activeRoute = activeSidebarScreen, // 👈 Matched to AppDrawer.kt
                                onRouteSelected = { route -> activeSidebarScreen = route }, // 👈 Matched to AppDrawer.kt
                                closeDrawer = { coroutineScope.launch { drawerState.close() } }
                            )
                        }
                    ) {
                        @OptIn(ExperimentalMaterial3Api::class)
                        (Scaffold(
                            modifier = Modifier.fillMaxSize(),

                            topBar = {
                                TopAppBar(
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        titleContentColor = MaterialTheme.colorScheme.primary,
                                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    title = {
                                        Text(
                                            "XpenseMate",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
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
                                    onAddClick = { showAddDialog = true }
                                )
                            }

                        ) { innerPadding ->

                            Column(modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()) {

                                Box(modifier = Modifier.weight(1f)) {

                                    when (selectedItem) {

                                        0 -> {
                                            HomeTab(
                                                transactions = transactions,
                                                subscriptions = mySubscriptions,
                                                dailyLimit = dailyLimitState,
                                                monthlyLimit = monthlyLimitState
                                            )
                                        }

                                        1 -> {
                                            HistoryTab(
                                                transactions = transactions,
                                                categories = masterCategoryList,
                                                onUpdateTransaction = { updatedTx ->
                                                    coroutineScope.launch {
                                                        db.transactionDao()
                                                            .updateTransaction(updatedTx)
                                                    }
                                                }
                                            )
                                        }

                                        2 -> {
                                            AnalyticsTab(
                                                transactions = transactions,
                                                categoryBudgets = masterCategoryLimits,
                                                dailyLimit = dailyLimitState.toFloat()
                                            )
                                        }

                                        3 -> {
                                            AccountsTab()
                                        }
                                    }


                                    if (showAddDialog) {
                                        TransactionPopup(
                                            onDismiss = { showAddDialog = false },
                                            categories = masterCategoryList,
                                            onSave = { savedTitle, savedAmount, savedIsExpense, savedCategory ->
                                                val now = LocalDateTime.now()
                                                val dateFormatter =
                                                    DateTimeFormatter.ofPattern("dd MMM yyyy")
                                                val timeFormatter =
                                                    DateTimeFormatter.ofPattern("hh:mm a")
                                                val amountDouble =
                                                    savedAmount.toDoubleOrNull() ?: 0.0

                                                coroutineScope.launch {
                                                    db.transactionDao().insertTransaction(
                                                        Transaction(
                                                            title = savedTitle,
                                                            amount = amountDouble,
                                                            isExpense = savedIsExpense,
                                                            category = savedCategory, // 👈 And save it to the DB here!
                                                            date = now.format(dateFormatter),
                                                            time = now.format(timeFormatter)
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                    }

                                    when (activeSidebarScreen) {
                                        SidebarScreen.BUDGET -> {
                                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                BudgetSettingsView(
                                                    currentDaily = dailyLimitState.toFloat(),
                                                    currentMonthly = monthlyLimitState.toFloat(),
                                                    categories = masterCategoryList,
                                                    currentCategoryLimits = masterCategoryLimits,
                                                    onBackClick = { activeSidebarScreen = null }, // 👈 Closes the screen
                                                    onSave = { newD, newM, newCategoryLimits ->
                                                        SettingsManager.saveBudgets(context, newD, newM)
                                                        SettingsManager.saveCategoryBudgets(context, newCategoryLimits)
                                                        dailyLimitState = newD.toDouble()
                                                        monthlyLimitState = newM.toDouble()
                                                        masterCategoryLimits = newCategoryLimits

                                                        activeSidebarScreen = null // 👈 Closes the screen
                                                    }
                                                )
                                            }
                                        }
                                        SidebarScreen.DESIGN -> {
                                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                DesignSettingsView(
                                                    isDarkMode = isDarkMode,
                                                    onBackClick = { activeSidebarScreen = null }, // 👈 Closes the screen
                                                    onThemeChange = { newValue ->
                                                        ThemeManager.saveTheme(context, newValue)
                                                        isDarkMode = newValue
                                                    }
                                                )
                                            }
                                        }
                                        SidebarScreen.CATEGORY -> {
                                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                CategorySettingsView(
                                                    currentCategories = masterCategoryList,
                                                    onBackClick = { activeSidebarScreen = null }, // 👈 Closes the screen
                                                    onCategoriesUpdated = { newList ->
                                                        SettingsManager.saveCategories(context, newList.toSet())
                                                        masterCategoryList = newList
                                                    }
                                                )
                                            }
                                        }
                                        SidebarScreen.RECURRING -> {
                                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                                RecurringPaymentsView(
                                                    subscriptions = mySubscriptions,
                                                    onBackClick = { activeSidebarScreen = null }, // 👈 Closes the screen
                                                    onSaveNew = { merchant, amount, frequency, billingDay, category ->
                                                        addManualSubscription(context, merchant, amount, frequency, billingDay, category)
                                                    },
                                                    onDelete = { subToDelete ->
                                                        coroutineScope.launch { db.subscriptionDao().cancelSubscription(subToDelete.id) }
                                                    }
                                                )
                                            }
                                        }
                                        null -> {}
                                    }

                                    pendingSharedTransaction?.let { txn ->
                                        TransactionDetailsCard(
                                            transaction = txn,
                                            categories = masterCategoryList,
                                            isNew = true,
                                            onDismiss = {
                                                pendingSharedTransaction = null
                                            },
                                            onCategoryChange = { finalTxn ->
                                                ExpenseEngine.saveTransaction(finalTxn)
                                                pendingSharedTransaction = null
                                            }
                                        )
                                    }

                                }
                            }
                        })
                    }
                } else {
                    PermissionScreen(
                        hasNotifPermission = hasNotifPermission,
                        onFinish = { userFinishedSetup = true })
                }
            }
        }
    }

    private fun processSharedImage(context: Context, imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Get the raw image
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

                // 2. 🚀 Blow it up 3x
                val scaledBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    originalBitmap.width * 3,
                    originalBitmap.height * 3,
                    true
                )

                // 3. 🚀 THE INVERT HACK (Dark Mode to Light Mode)
                val invertedBitmap = Bitmap.createBitmap(
                    scaledBitmap.width,
                    scaledBitmap.height,
                    scaledBitmap.config ?: Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(invertedBitmap)
                val paint = Paint()

                // This matrix mathematically flips all colors like a film negative
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

                // 4. Feed the bright, inverted image to the standard English scanner
                val image = InputImage.fromBitmap(invertedBitmap, 0)
                val recognizer = TextRecognition.getClient(
                    TextRecognizerOptions.DEFAULT_OPTIONS
                )

                // 5. Scan it on the Main Thread
                withContext(Dispatchers.Main) {
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            val extractedText = visionText.text

                            // Check Logcat! The decimal point will be there.
                            Log.d("XpenseMate_OCR", "Extracted Screen Text:\n$extractedText")

                            // Send to Cloudflare
                            CoroutineScope(Dispatchers.IO).launch {
                                ExpenseEngine.processTransaction(
                                    context = context,
                                    rawText = extractedText,
                                    appName = "SCREENSHOT",
                                    paymentMode = "UPI",
                                    timestamp = System.currentTimeMillis()
                                ) { parsedList ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if (parsedList != null && parsedList.isNotEmpty()) {
                                            pendingSharedTransaction = parsedList[0]
                                        } else {
                                            pendingSharedTransaction = null
                                            Toast.makeText(
                                                context,
                                                "Could not find a valid transaction.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("XpenseMate_OCR", "Failed to read text", e)
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }
    private fun handleShareIntent(currentIntent: Intent?) {
        if (currentIntent?.action == Intent.ACTION_SEND) {

            // 1. IMAGE HANDLING
            if (currentIntent.type?.startsWith("image/") == true) {
                val uri =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        currentIntent.getParcelableExtra(
                            Intent.EXTRA_STREAM,
                            Uri::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        currentIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    }
                if (uri != null) {
                    Log.d("XpenseMate_Share", "Caught a screenshot!")
                    processSharedImage(this, uri)
                }
            }

            // 2. 🚀 PDF HANDLING
            else if (currentIntent.type == "application/pdf") {
                val pdfUri =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        currentIntent.getParcelableExtra(
                            Intent.EXTRA_STREAM,
                            Uri::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        currentIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    }
                if (pdfUri != null) {
                    Log.d("XpenseMate_PDF", "📄 Caught a PDF Bank Statement!")
                    pendingPdfUri = pdfUri
                    showPdfPasswordDialog = true // Show the popup!
                }
            }
        }
    }
}
