package com.jsingh.XpenseMate

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jsingh.XpenseMate.ui.theme.XpenseMateTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import androidx.compose.foundation.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import androidx.room.Room.databaseBuilder
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.PowerManager
import android.net.Uri
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {

    //DATABASE CONNECTION
    private val db by lazy { AppDatabase.getDatabase(applicationContext) }

    private var pendingSharedTransaction by mutableStateOf<Transaction?>(null)

    private var pendingPdfUri by mutableStateOf<android.net.Uri?>(null)
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

            var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode(context)) }
            var showDesignView by remember { mutableStateOf(false) }
            XpenseMateTheme(darkTheme = isDarkMode) {
                //APP STATE
                var selectedItem by remember { mutableIntStateOf(0) }
                var showAddDialog by remember { mutableStateOf(false) }
                var showBudgetView by remember { mutableStateOf(false) }
                var showRecurringView by remember { mutableStateOf(false) }
                // 1. Grab the values from the phone's memory once on startup
                val initialLimits = remember { SettingsManager.getBudgets(context) }
                var dailyLimitState by remember { mutableStateOf(initialLimits.first.toDouble()) }
                var monthlyLimitState by remember { mutableStateOf(initialLimits.second.toDouble()) }

                var showCategoryView by remember { mutableStateOf(false) }
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

                // LEFT-SIDE NAVIGATION DRAWER
                val drawerState = rememberDrawerState(
                    initialValue = DrawerValue.Closed
                )

                val lifecycleOwner = LocalLifecycleOwner.current
                val powerManager =
                    remember { context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager }

// Track BOTH permissions independently
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

// Listen for changes when user comes back from Settings
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

// THE GATEKEEPER: Only show dashboard if Notifications are ON, AND (Battery is exempt OR they clicked Finish)
                if (hasNotifPermission && (hasBatteryExemption || userFinishedSetup)) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth(0.80f)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {

                                    // --- HEADER: ACCOUNT & PREMIUM CARD ---
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.3f
                                                )
                                            )
                                            .clickable { /* TODO: Open Account Info */ }
                                            .padding(20.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.size(56.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        "JS",
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "J. Singh",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    "jsingh@gmail.com",
                                                    fontSize = 14.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Icon(
                                                Icons.Rounded.ChevronRight,
                                                contentDescription = "Open Account",
                                                tint = Color.Gray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.7f
                                                )
                                            ) {
                                                Text(
                                                    text = "Free Plan",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 4.dp
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))

                                            Text(
                                                text = "Upgrade to Pro",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.1f
                                        )
                                    )

                                    // --- MIDDLE: SCROLLABLE FEATURE LIST ---
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Group 1
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.AccountBalance,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Manage Banks & Wallets",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = { },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.PieChart,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Budgets & Limits",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = {
                                                coroutineScope.launch { drawerState.close() }
                                                showBudgetView = true
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Label,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Tags & Categories",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = {
                                                coroutineScope.launch { drawerState.close() }
                                                showCategoryView = true
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Repeat,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Recurring Payments",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = { coroutineScope.launch { drawerState.close() }
                                                    showRecurringView = true },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.1f
                                            )
                                        )

                                        // Group 2
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Palette,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Theme & Design",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = {
                                                coroutineScope.launch { drawerState.close() }
                                                showDesignView = true
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Notifications,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Notifications",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = { },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.1f
                                            )
                                        )

                                        // Group 3
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Lock,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Privacy & Security",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = { },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Cloud,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Cloud Backup & Sync",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = { },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Download,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Export Data",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = { },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.1f
                                            )
                                        )

                                        // Group 4
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Help,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Contact & Support",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = { },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        NavigationDrawerItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.Description,
                                                    contentDescription = null
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Terms & Policy",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = false,
                                            onClick = { },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // --- FOOTER: VERSION ---
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.1f
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "XpenseMate v1.0",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    ) {
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
                                Column {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    )
                                    BottomAppBar(
                                        containerColor = MaterialTheme.colorScheme.background, // Pure Pitch Black
                                        contentPadding = PaddingValues(0.dp),
                                    ) {
                                        val navItemColors = NavigationBarItemDefaults.colors(
                                            // Active tabs get the bright Silver Accent
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.08f
                                            ),

                                            // THE FIX: All 4 unselected tabs are now a solid, crisp Light Grey
                                            unselectedIconColor = Color.LightGray,
                                            unselectedTextColor = Color.LightGray
                                        )

                                        NavigationBarItem(
                                            icon = { Icon(Icons.Rounded.Home, "Home") },
                                            label = {
                                                Text(
                                                    "Home",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = selectedItem == 0,
                                            onClick = { selectedItem = 0 },
                                            colors = navItemColors
                                        )

                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.history),
                                                    "Payments",
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "History",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = selectedItem == 1,
                                            onClick = { selectedItem = 1 },
                                            colors = navItemColors
                                        )

                                        // The Focal Point: Solid Silver "Add" Button
                                        NavigationBarItem(
                                            selected = false,
                                            onClick = { showAddDialog = true },
                                            icon = {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(56.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Rounded.Add,
                                                            "Add",
                                                            tint = MaterialTheme.colorScheme.background,
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            colors = navItemColors
                                        )

                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.analytics),
                                                    contentDescription = "Analytics",
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Analytics",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = selectedItem == 2,
                                            onClick = { selectedItem = 2 },
                                            colors = navItemColors
                                        )

                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    Icons.Rounded.AccountCircle,
                                                    "Accounts"
                                                )
                                            },
                                            label = {
                                                Text(
                                                    "Accounts",
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            selected = selectedItem == 3,
                                            onClick = { selectedItem = 3 },
                                            colors = navItemColors
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->

                            // 1. Changed Box to Column to stack the dashboard on top of the app
                            Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                                if (false) {
                                    // 🛠️ DEV DASHBOARD HIDDEN
                                    var testSms by remember { mutableStateOf("") }
                                    val currentContext = LocalContext.current

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .background(
                                                Color(0xFFEEEEEE),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            "🛠️ Developer Testing Panel",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )

                                        OutlinedTextField(
                                            value = testSms,
                                            onValueChange = { testSms = it },
                                            label = { Text("Paste Fake Bank SMS Here") },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // BUTTON 1: Inject straight to Engine
                                            Button(onClick = {
                                                ExpenseEngine.processTransaction(
                                                    currentContext,
                                                    testSms,
                                                    "DEV_DASHBOARD",
                                                    "Bank",
                                                    System.currentTimeMillis()
                                                )
                                                testSms = ""
                                            }) {
                                                Text("Inject to AI")
                                            }

                                            // BUTTON 2: Test your exact SmsReceiver Gates!
                                            Button(
                                                onClick = {
                                                    val lowerBody = testSms.lowercase()

                                                    // GATE 2: Currency Check (Using your exact Regex!)
                                                    val hasMoneyAmount =
                                                        lowerBody.contains(Regex("(?i)(rs\\.?|inr|₹)\\s*[0-9,]+\\.?[0-9]*"))
                                                    if (!hasMoneyAmount) {
                                                        android.util.Log.d(
                                                            "XpenseMate_GATES",
                                                            "🗑️ Dropped [Gate 2]: No currency format found"
                                                        )
                                                    } else {
                                                        // GATE 3: Junk Check
                                                        val junkKeywords = listOf(
                                                            "otp",
                                                            "verification",
                                                            "code is",
                                                            "loan",
                                                            "offer",
                                                            "win",
                                                            "apply now",
                                                            "reward",
                                                            "within",
                                                            "statement",
                                                            "due date",
                                                            "bill generated",
                                                            "minimum due",
                                                            "outstanding",
                                                            "failed",
                                                            "declined",
                                                            "unsuccessful",
                                                            "rejected",
                                                            "bounced"
                                                        )
                                                        if (junkKeywords.any { lowerBody.contains(it) }) {
                                                            android.util.Log.d(
                                                                "XpenseMate_GATES",
                                                                "🗑️ Dropped [Gate 3]: Contains Spam/Bill/Failure keywords"
                                                            )
                                                        } else {
                                                            // GATE 4: Bank Check
                                                            val bankingSignature = listOf(
                                                                "a/c",
                                                                "acct",
                                                                "account",
                                                                "card",
                                                                "upi",
                                                                "vpa",
                                                                "debited",
                                                                "credited",
                                                                "deducted",
                                                                "refund",
                                                                "reversed"
                                                            )
                                                            if (!bankingSignature.any {
                                                                    lowerBody.contains(
                                                                        it
                                                                    )
                                                                }) {
                                                                android.util.Log.d(
                                                                    "XpenseMate_GATES",
                                                                    "🗑️ Dropped [Gate 4]: Lacks banking signature"
                                                                )
                                                            } else {
                                                                android.util.Log.d(
                                                                    "XpenseMate_GATES",
                                                                    "✅ VIP APPROVED: Valid Transaction"
                                                                )
                                                            }
                                                        }
                                                    }
                                                    testSms = ""
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                            ) {
                                                Text("Test Gates")
                                            }
                                        }
                                    }
                                }
                                Box(modifier = Modifier.weight(1f)) {

                                    when (selectedItem) {

                                        0 -> {
                                            HomeTab(
                                                transactions = transactions,
                                                dailyLimit = dailyLimitState,
                                                monthlyLimit = monthlyLimitState
                                            )
                                        }

                                        1 -> {
                                            HistoryTab(
                                                transactions = transactions,
                                                categories = masterCategoryList, // Pass custom categories
                                                onUpdateTransaction = { updatedTx ->
                                                    // Tell the database to save the new category!
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

                                    if (showBudgetView) {
                                        Box(
                                            modifier = Modifier.fillMaxSize()
                                                .background(MaterialTheme.colorScheme.background)
                                        ) {
                                            BudgetSettingsView(
                                                currentDaily = dailyLimitState.toFloat(),
                                                currentMonthly = monthlyLimitState.toFloat(),
                                                categories = masterCategoryList, // 👈 Passing the live custom categories
                                                currentCategoryLimits = masterCategoryLimits, // 👈 Passing the current category limits
                                                onBackClick = { showBudgetView = false },
                                                onSave = { newD, newM, newCategoryLimits ->
                                                    // 1. Save all of it to the phone's memory
                                                    SettingsManager.saveBudgets(context, newD, newM)
                                                    SettingsManager.saveCategoryBudgets(
                                                        context,
                                                        newCategoryLimits
                                                    )

                                                    // 2. Update the live wires instantly
                                                    dailyLimitState = newD.toDouble()
                                                    monthlyLimitState = newM.toDouble()
                                                    masterCategoryLimits = newCategoryLimits

                                                    showBudgetView = false
                                                }
                                            )
                                        }
                                    }

                                    if (showDesignView) {
                                        Box(
                                            modifier = Modifier.fillMaxSize()
                                                .background(MaterialTheme.colorScheme.background)
                                        ) {
                                            DesignSettingsView(
                                                isDarkMode = isDarkMode,
                                                onBackClick = { showDesignView = false },
                                                onThemeChange = { newValue ->
                                                    ThemeManager.saveTheme(context, newValue)
                                                    isDarkMode = newValue
                                                }
                                            )
                                        }
                                    }

                                    if (showCategoryView) {
                                        Box(
                                            modifier = Modifier.fillMaxSize()
                                                .background(MaterialTheme.colorScheme.background)
                                        ) {
                                            CategorySettingsView(
                                                currentCategories = masterCategoryList,
                                                onBackClick = { showCategoryView = false },
                                                onCategoriesUpdated = { newList ->
                                                    SettingsManager.saveCategories(
                                                        context,
                                                        newList.toSet()
                                                    )
                                                    masterCategoryList = newList
                                                }
                                            )
                                        }
                                    }

                                    if (showRecurringView) {
                                        Box(
                                            modifier = Modifier.fillMaxSize()
                                                .background(MaterialTheme.colorScheme.background)
                                        ) {
                                            val context = LocalContext.current
                                            val mySubscriptions = transactions.filter { it.source == "SUBSCRIPTION" }

                                            RecurringPaymentsView(
                                                subscriptions = mySubscriptions,
                                                onBackClick = { showRecurringView = false },
                                                onSaveNew = { merchant, amount, category, mode ->
                                                    addManualSubscription(context, merchant, amount, category, mode)
                                                },
                                                onDelete = { txnToDelete ->
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        val db = AppDatabase.getDatabase(context)
                                                        db.transactionDao().deleteTransaction(txnToDelete)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    
                                    if (showPdfPasswordDialog && pendingPdfUri != null) {
                                        // 1. Hook into Android's lightweight local storage
                                        val sharedPrefs = context.getSharedPreferences("XpenseMatePrefs", Context.MODE_PRIVATE)
                                        val savedPass = sharedPrefs.getString("SAVED_BANK_PASSWORD", "") ?: ""

                                        // 2. New States for our upgraded UI
                                        var passwordInput by remember { mutableStateOf(savedPass) }
                                        var isError by remember { mutableStateOf(false) }
                                        var isExtracting by remember { mutableStateOf(false) }
                                        var isPasswordVisible by remember { mutableStateOf(false) } // The Eye Toggle
                                        var rememberPassword by remember { mutableStateOf(savedPass.isNotEmpty()) } // The Checkbox

                                        AlertDialog(
                                            onDismissRequest = {
                                                if (!isExtracting) {
                                                    showPdfPasswordDialog = false
                                                    pendingPdfUri = null
                                                }
                                            },
                                            title = { Text("Encrypted Statement", fontWeight = FontWeight.Bold) },
                                            text = {
                                                Column {
                                                    Text("Enter the password to unlock this bank statement.", fontSize = 14.sp, color = Color.Gray)
                                                    Spacer(modifier = Modifier.height(12.dp))

                                                    androidx.compose.material3.OutlinedTextField(
                                                        value = passwordInput,
                                                        onValueChange = {
                                                            passwordInput = it
                                                            isError = false
                                                        },
                                                        label = { Text("Password") },
                                                        isError = isError,
                                                        singleLine = true,
                                                        // 🚀 THE EYE TOGGLE LOGIC
                                                        visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                                        trailingIcon = {
                                                            val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )

                                                    if (isError) {
                                                        Text("Incorrect Password. Try again.", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                                    }

                                                    // 🚀 THE REMEMBER PASSWORD CHECKBOX
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 8.dp)
                                                            .clickable { rememberPassword = !rememberPassword }
                                                    ) {
                                                        Checkbox(
                                                            checked = rememberPassword,
                                                            onCheckedChange = { rememberPassword = it }
                                                        )
                                                        Text("Remember password", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                                    }

                                                    if (isExtracting) {
                                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
                                                    }
                                                }
                                            },
                                            confirmButton = {
                                                TextButton(
                                                    enabled = passwordInput.isNotEmpty() && !isExtracting,
                                                    onClick = {
                                                        isExtracting = true
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            // TRY TO UNLOCK IT
                                                            val resultText = extractTextFromPdf(context, pendingPdfUri!!, passwordInput)

                                                            if (resultText == "ERROR_WRONG_PASSWORD") {
                                                                isError = true
                                                                isExtracting = false
                                                            } else if (resultText.startsWith("ERROR")) {
                                                                android.widget.Toast.makeText(context, "Failed to read PDF", android.widget.Toast.LENGTH_SHORT).show()
                                                                isExtracting = false
                                                            } else {
                                                                // SUCCESS!
                                                                android.util.Log.d("XpenseMate_PDF", "✅ UNLOCKED! Extracted ${resultText.length} characters.")

                                                                // 🚀 SAVE THE PASSWORD IF CHECKED
                                                                if (rememberPassword) {
                                                                    sharedPrefs.edit().putString("SAVED_BANK_PASSWORD", passwordInput).apply()
                                                                } else {
                                                                    sharedPrefs.edit().remove("SAVED_BANK_PASSWORD").apply()
                                                                }

                                                                // 🚀 SEND IT TO THE CLOUD
                                                                ExpenseEngine.processTransaction(
                                                                    context = context,
                                                                    rawText = resultText,
                                                                    appName = "STATEMENT",
                                                                    paymentMode = "Bank Statement",
                                                                    timestamp = System.currentTimeMillis()
                                                                ) { parsedTxn ->
                                                                    android.util.Log.d("XpenseMate_PDF", "🎉 AI FINISHED PARSING STATEMENT!")

                                                                    CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                                                        showPdfPasswordDialog = false
                                                                        pendingPdfUri = null
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                ) { Text("Unlock") }
                                            },
                                            dismissButton = {
                                                TextButton(
                                                    enabled = !isExtracting,
                                                    onClick = {
                                                        showPdfPasswordDialog = false
                                                        pendingPdfUri = null
                                                    }) { Text("Cancel") }
                                            }
                                        )
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
                        }
                    }
                } else {
                    PermissionScreen(
                        hasNotifPermission = hasNotifPermission,
                        onFinish = { userFinishedSetup = true })
                }
            }
        }
    }

    private suspend fun extractTextFromPdf(context: Context, uri: android.net.Uri, password: String): String {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val document = PDDocument.load(inputStream, password)
                    val stripper = PDFTextStripper()
                    val text = stripper.getText(document)
                    document.close()
                    return@withContext text
                } ?: "ERROR_FILE_NOT_FOUND"
            } catch (e: InvalidPasswordException) {
                return@withContext "ERROR_WRONG_PASSWORD"
            } catch (e: Exception) {
                android.util.Log.e("XpenseMate_PDF", "PDF Parse Error", e)
                return@withContext "ERROR_UNKNOWN"
            }
        }
    }

    private fun processSharedImage(context: Context, imageUri: Uri) {
            CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Get the raw image
                val originalBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, imageUri)
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                }

                // 2. 🚀 Blow it up 3x
                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                    originalBitmap,
                    originalBitmap.width * 3,
                    originalBitmap.height * 3,
                    true
                )

                // 3. 🚀 THE INVERT HACK (Dark Mode to Light Mode)
                val invertedBitmap = android.graphics.Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, scaledBitmap.config ?: android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(invertedBitmap)
                val paint = android.graphics.Paint()

                // This matrix mathematically flips all colors like a film negative
                val colorMatrix = android.graphics.ColorMatrix(floatArrayOf(
                    -1f,  0f,  0f,  0f, 255f,
                    0f, -1f,  0f,  0f, 255f,
                    0f,  0f, -1f,  0f, 255f,
                    0f,  0f,  0f,  1f,   0f
                ))
                paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
                canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

                // 4. Feed the bright, inverted image to the standard English scanner
                val image = com.google.mlkit.vision.common.InputImage.fromBitmap(invertedBitmap, 0)
                val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                    com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)

                // 5. Scan it on the Main Thread
                withContext(Dispatchers.Main) {
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            val extractedText = visionText.text

                            // Check Logcat! The decimal point will be there.
                            android.util.Log.d("XpenseMate_OCR", "Extracted Screen Text:\n$extractedText")

                            // Send to Cloudflare
                            CoroutineScope(Dispatchers.IO).launch {
                                ExpenseEngine.processTransaction(
                                    context = context,
                                    rawText = extractedText,
                                    appName = "SCREENSHOT",
                                    paymentMode = "UPI",
                                    timestamp = System.currentTimeMillis()
                                ) { parsedList ->
                                     CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        if (parsedList != null && parsedList.isNotEmpty()) {
                                            pendingSharedTransaction = parsedList[0]
                                        } else {
                                            pendingSharedTransaction = null
                                            android.widget.Toast.makeText(context, "Could not find a valid transaction.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("XpenseMate_OCR", "Failed to read text", e)
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
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        currentIntent.getParcelableExtra(
                            Intent.EXTRA_STREAM,
                            android.net.Uri::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        currentIntent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                    }
                if (uri != null) {
                    android.util.Log.d("XpenseMate_Share", "Caught a screenshot!")
                    processSharedImage(this, uri)
                }
            }

            // 2. 🚀 PDF HANDLING
            else if (currentIntent.type == "application/pdf") {
                val pdfUri =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        currentIntent.getParcelableExtra(
                            Intent.EXTRA_STREAM,
                            android.net.Uri::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        currentIntent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                    }
                if (pdfUri != null) {
                    android.util.Log.d("XpenseMate_PDF", "📄 Caught a PDF Bank Statement!")
                    pendingPdfUri = pdfUri
                    showPdfPasswordDialog = true // Show the popup!
                }
            }
        }
    }
}
//TABS
@Composable
fun HomeTab(transactions: List<Transaction>,dailyLimit: Double,
            monthlyLimit: Double){

    var recurringSubTab by remember { mutableStateOf("Pending") }
    val todayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val todayString = LocalDateTime.now().format(todayFormatter)
    val currentMonthString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM yyyy"))

    val dailySpent = transactions.filter { it.isExpense && it.date == todayString }.sumOf { it.amount }
    val monthlySpent = transactions.filter { it.isExpense && it.date.contains(currentMonthString) }.sumOf { it.amount }

    val dailyRatio = (dailySpent / dailyLimit).toFloat().coerceIn(0f, 1f)
    val monthlyRatio = (monthlySpent / monthlyLimit).toFloat().coerceIn(0f, 1f)

    val safeColor = Color(0xFFA8D5BA)
    val warningColor = Color(0xFFFCD7A1)
    val dangerColor = Color(0xFFF2A6A6)

    val dailyColor = when {
        dailyRatio < 0.5f -> safeColor
        dailyRatio < 0.8f -> warningColor
        else -> dangerColor
    }
    val monthlyColor = when {
        monthlyRatio < 0.5f -> safeColor
        monthlyRatio < 0.8f -> warningColor
        else -> dangerColor
    }
    var showCategoryBreakdown by remember { mutableStateOf(false) }
    val formatter = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply { minimumFractionDigits = 0; maximumFractionDigits = 0 } }
    var selectedHomeTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- TOP CARD: DAILY SPENDING ---
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(all = 20.dp)) {
                Text("Daily Spending", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text("₹${formatter.format(dailySpent)}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(" / ₹${formatter.format(dailyLimit)}", fontSize = 16.sp, modifier = Modifier.padding(bottom = 3.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = dailyRatio)
                            .height(12.dp)
                            .background(dailyColor, RoundedCornerShape(6.dp))
                    )
                }
            }
        }

        // --- MIDDLE ROW: DONUT CHART & ACTION CARDS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Monthly Spending", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 25f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                            drawArc(
                                color = monthlyColor,
                                startAngle = -90f, sweepAngle = 360f * monthlyRatio, useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 25f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                        Text("${(monthlyRatio * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // --- THE RIGHT SIDE COLUMN (Restored!) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Static Category Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { showCategoryBreakdown = true } // 👈 Triggers the popup
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(id = R.drawable.ic_category), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Category\nSpending", fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
                    }
                }

                // 2. Static Analyze Savings Button (Unbroken)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { /* Open PopUp */ }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(id = R.drawable.ic_savings), contentDescription = null, tint = safeColor, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Analyze\nSavings", fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
                    }
                }
            }
        }

        // --- BOTTOM SECTION: 3-WAY TOGGLE SLIDER ---
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf("Recent", "Insights", "Recurring")
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (selectedHomeTab == index) MaterialTheme.colorScheme.background else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedHomeTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedHomeTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedHomeTab == index) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                    }
                }
            }
        }

        when (selectedHomeTab) {
            0 -> {
                val recentTxns = transactions.take(5)
                if (recentTxns.isEmpty()) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp), contentAlignment = Alignment.Center) {
                        Text("No recent transactions", color = Color.Gray)
                    }
                } else {
                    Column {
                        recentTxns.forEach { txn -> TransactionItem(txn = txn) }
                    }
                }
            }
            1 -> {
                Surface(shape = RoundedCornerShape(16.dp), color = safeColor.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("You spent 15% less this week compared to last week. Keep it up!", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            2 -> {
                // 1. WRAPPER BOX: Centers the entire toggle switch horizontally and adds vertical breathing room
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(50), // Makes it perfectly rounded like a pill
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.width(200.dp).height(32.dp)
                    ) {
                        // 2. INNER PADDING: Added 2.dp here so the active background doesn't touch the outer edge
                        Row(modifier = Modifier.fillMaxSize().padding(2.dp)) {

                            // "Pending" Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (recurringSubTab == "Pending") MaterialTheme.colorScheme.surface.copy(
                                            0.5f
                                        ) else Color.Transparent
                                    )
                                    .clickable {
                                        recurringSubTab = "Pending"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pending",
                                    color = if (recurringSubTab == "Pending") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                            // "Upcoming" Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (recurringSubTab == "Upcoming") MaterialTheme.colorScheme.surface.copy(
                                            0.5f
                                        ) else Color.Transparent
                                    )
                                    .clickable {
                                        recurringSubTab = "Upcoming"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Upcoming",
                                    color = if (recurringSubTab == "Upcoming") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showCategoryBreakdown) {
        CategoryBreakdownSheet(
            transactions = transactions,
            onDismiss = { showCategoryBreakdown = false }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBreakdownSheet(
    transactions: List<Transaction>,
    onDismiss: () -> Unit
) {
    var selectedTimeframe by remember { mutableIntStateOf(1) } // 0 = Today, 1 = This Month

    // Time math
    val todayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val todayString = LocalDateTime.now().format(todayFormatter)
    val currentMonthString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM yyyy"))

    // Filter and group based on the toggle
    val filteredTxns = transactions.filter { txn ->
        txn.isExpense && if (selectedTimeframe == 0) txn.date == todayString else txn.date.contains(currentMonthString)
    }

    val categoryTotals = filteredTxns
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second } // Highest spend at the top

    val formatter = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply { minimumFractionDigits = 0; maximumFractionDigits = 0 } }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Category Spending",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- THE DAY / MONTH TOGGLE ---
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(0.8f).height(40.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val tabs = listOf("Today", "This Month")
                    tabs.forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(if (selectedTimeframe == index) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                                .clickable { selectedTimeframe = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (selectedTimeframe == index) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- THE CATEGORY LIST ---
            if (categoryTotals.isEmpty()) {
                Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No expenses logged.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp), // Prevents it from getting too tall
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryTotals) { (category, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 🔥 THE FIX: Dynamic Initial Icon
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = category.take(1).uppercase(), // Grabs the first letter
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(category, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text("₹${formatter.format(amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HistoryTab(transactions: List<Transaction>,categories: List<String>,                     // 👈 Fixed: Added categories parameter
               onUpdateTransaction: (Transaction) -> Unit){

        var showDatePicker by remember { mutableStateOf(false) }
        var dateFilter by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var activeFilter by remember { mutableStateOf("All") }
        var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
        val displayedTransactions = transactions.filter { txn ->
        val matchesSearch = txn.title.contains(searchQuery, ignoreCase = true) || txn.amount.toString().contains(searchQuery)
            val matchesFilter = when (activeFilter) {
            "Expense" -> txn.isExpense
            "Income" -> !txn.isExpense
            else -> true
        }

        val matchesDate = if (dateFilter != null) txn.date.endsWith(dateFilter!!) else true
        matchesSearch && matchesFilter && matchesDate
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Search Bar & Calendar Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f), shape = CircleShape)
                    .border(width = 1.dp, color = Color.LightGray, shape = CircleShape)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, "Search", tint = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) { Text("Search transactions...", color = Color.Gray, fontSize = 16.sp) }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery, onValueChange = { searchQuery = it }, singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            IconButton(
                onClick = { showDatePicker=true },
                modifier = Modifier.background(color = if (dateFilter != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape)
            ) {
                Icon(Icons.Rounded.DateRange, "Calendar", tint = if (dateFilter != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (showDatePicker) {
            historyShowDatePicker(
                onDateSelected = { pickedDate -> dateFilter = pickedDate },
                onDismiss = { showDatePicker = false }
            )
        }


        // Filter Chips
        val filterOptions = listOf("All", "Expense", "Income", "This Week", "This Month")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filterOptions) { filter ->

                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { activeFilter = filter },
                    label = { Text(filter, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                        selectedLabelColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // The Full Scrollable List
        // The Full Scrollable List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(displayedTransactions.size) { index ->
                TransactionItem(
                    txn = displayedTransactions[index],
                    onClick = { selectedTransaction = displayedTransactions[index] } // 👈 TRIGGERS THE POPUP
                )
            }
        }

        // 👈 ADD THIS AT THE VERY BOTTOM OF HistoryTab:
        if (selectedTransaction != null) {
            TransactionDetailsCard(
                transaction = selectedTransaction!!,
                categories = categories,
                onDismiss = { selectedTransaction = null },
                onCategoryChange = { updatedTx ->
                    onUpdateTransaction(updatedTx)
                    selectedTransaction = null // Close sheet after saving
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionPopup(
    onDismiss: () -> Unit,
    categories: List<String>,
    onSave: (String, String, Boolean, String) -> Unit
) {
    var isExpense by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    // Category States
    var selectedCategory by remember { mutableStateOf("General") }
    var showCategorySheet by remember { mutableStateOf(false) } // 👈 Controls the bottom sheet

    val expenseColor = Color(0xFFE57373)
    val incomeColor = Color(0xFF81C784)

    // --- 1. THE MAIN POPUP DIALOG ---
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "New Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Expense/Income Toggle Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), shape = CircleShape)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = if (isExpense) expenseColor else Color.Transparent, shape = CircleShape)
                            .clickable { isExpense = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Expense", color = if (isExpense) Color.Black else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = if (!isExpense) incomeColor else Color.Transparent, shape = CircleShape)
                            .clickable { isExpense = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Income", color = if (!isExpense) Color.Black else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                }

                // 1. Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next)
                )

                // 2. Amount Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newText -> amount = newText.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹ ", fontWeight = FontWeight.Normal, fontSize = 16.sp) }
                )

                // 3. Category Trigger (Looks like a text field, acts like a button)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true, // Prevents the keyboard from popping up
                        label = { Text("Category") },
                        trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Invisible overlay to catch the click perfectly
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showCategorySheet = true }
                    )
                }

                // Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.width(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && amount.isNotBlank()) {
                                onSave(title, amount, isExpense, selectedCategory)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.width(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    // --- 2. THE BOTTOM SHEET (Category Picker) ---
    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            containerColor = MaterialTheme.colorScheme.background, // Pitch black in dark mode
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                // High-Contrast Grid of Categories
                val chunkedCategories = categories.chunked(3) // 3 items per row
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    chunkedCategories.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    // Highlights the selected one with Pure White (or Black in Light Mode)
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedCategory = cat
                                            showCategorySheet = false // Closes automatically when picked!
                                        }
                                ) {
                                    Text(
                                        text = cat,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            // Fill empty spaces if a row has fewer than 3 items
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp)) // Breathing room at the bottom
            }
        }
    }
}

@Composable
fun AnalyticsTab(transactions: List<Transaction>, categoryBudgets: Map<String, Float>, dailyLimit: Float) {
    val currentLocalDate = java.time.LocalDate.now()
    val currentYearNum = currentLocalDate.year
    val currentMonthStr = currentLocalDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM"))

    var selectedTimeframe by remember { mutableStateOf("$currentMonthStr $currentYearNum") }
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // --- THE DYNAMIC MATH ENGINE ---
    val timeframeExpenses = transactions.filter { it.isExpense && it.date.contains(selectedTimeframe) }
    val totalSpent = timeframeExpenses.sumOf { it.amount }

    val categoryTotals = timeframeExpenses
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val formatter = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply { minimumFractionDigits = 0; maximumFractionDigits = 0 } }

    val chartColors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFFFFB74D), Color(0xFF43A047), Color(0xFF8E24AA), Color(0xFF00ACC1))

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 8.dp)) {
        // --- ROW 1: TITLE & THE BUTTON ---
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Monthly spending", fontSize = 20.sp,fontWeight = FontWeight.SemiBold)
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f), modifier = Modifier.clickable { showDatePicker = true }) {
                Row(modifier = Modifier.padding(vertical = 4.dp).padding(start = 14.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedTimeframe, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        // --- ROW 2: MASSIVE AMOUNT & TREND ---
        Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Bottom) {
            Text(text = "₹ ${formatter.format(totalSpent)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, lineHeight = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            if (totalSpent > 0) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE53935).copy(alpha = 0.15f), modifier = Modifier.padding(bottom = 6.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = "Up", tint = Color(0xFFE53935), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- SMART INSIGHT CARD ---
        val topCategory = categoryTotals.firstOrNull()
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFF81C784).copy(alpha = 0.2f), modifier = Modifier.size(46.dp)) {
                    Icon(Icons.Rounded.Category, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.padding(10.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (topCategory != null) {
                        Text("Top Spend: ${topCategory.first}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("₹${formatter.format(topCategory.second)} spent here.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("No Insights Yet", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Log expenses to generate data.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        var selectedMode by remember { mutableIntStateOf(0) }

        // --- THE MAIN CARD ---
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).animateContentSize()) {

                // 3-Way Toggle
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Row(modifier = Modifier.fillMaxSize().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf("Heatmap", "Categories", "Weekly").forEachIndexed { index, title ->
                            val isSelected = selectedMode == index
                            Surface(modifier = Modifier.weight(1f).fillMaxHeight().clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { selectedMode = index }, shape = RoundedCornerShape(50), color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent, shadowElevation = if (isSelected) 2.dp else 0.dp) {
                                Box(contentAlignment = Alignment.Center) { Text(title, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // --- DYNAMIC CONTENT MODES ---
                when (selectedMode) {

                    // 🔥 MODE 0: CALENDAR HEATMAP (Fixed Budget Logic)
                    0 -> {
                        val dailySpends = timeframeExpenses.groupBy { it.date.take(2).toIntOrNull() ?: 1 }.mapValues { entry -> entry.value.sumOf { it.amount } }

                        val parts = selectedTimeframe.split(" ")
                        val year = parts.getOrNull(1)?.toIntOrNull() ?: currentYearNum
                        val month = java.time.Month.values().find { it.name.startsWith(parts[0].uppercase()) } ?: java.time.Month.JANUARY
                        val yearMonth = java.time.YearMonth.of(year, month)
                        val daysInMonth = yearMonth.lengthOfMonth()

                        val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
                        val calendarSlots = MutableList(firstDayOfWeek - 1) { 0 } + (1..daysInMonth).toList()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day -> Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            calendarSlots.chunked(7).forEach { week ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    for (i in 0 until 7) {
                                        if (i < week.size && week[i] != 0) {
                                            val day = week[i]
                                            val spentToday = dailySpends[day] ?: 0.0

                                            // The fixed Daily Limit color logic
                                            val upperBound = dailyLimit * 1.1
                                            val lowerBound = dailyLimit * 0.9

                                            val bgColor = when {
                                                spentToday == 0.0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                spentToday > upperBound -> Color(0xFFE53935).copy(alpha = 0.3f) // Red (>110%)
                                                spentToday >= lowerBound -> Color(0xFFFFB74D).copy(alpha = 0.3f) // Yellow (90-110%)
                                                else -> Color(0xFF4CAF50).copy(alpha = 0.3f) // Green (<90%)
                                            }
                                            val textColor = when {
                                                spentToday == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                                spentToday > upperBound -> Color(0xFFE53935)
                                                spentToday >= lowerBound -> Color(0xFFFFB74D)
                                                else -> Color(0xFF4CAF50)
                                            }

                                            Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp).clip(RoundedCornerShape(8.dp)).background(bgColor), contentAlignment = Alignment.Center) { Text(day.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor) }
                                        } else { Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)) }
                                    }
                                }
                            }
                        }
                    }

                    // 🔥 MODE 1: HYPER-USEFUL CATEGORIES (Fixed Ribbon)
                    1 -> {
                        val maxCategoryAmount = categoryTotals.maxOfOrNull { it.second } ?: 1.0

                        if (categoryTotals.isEmpty() || totalSpent == 0.0) {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { Text("No expenses logged.", color = Color.Gray) }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                                // 🔥 THE RESTORED CATEGORY RIBBON
                                Row(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(50))) {
                                    // Filter out 0 amounts so Compose doesn't crash on 0 weight!
                                    val validTotals = categoryTotals.filter { it.second > 0 }
                                    validTotals.forEachIndexed { index, (_, amount) ->
                                        val fraction = (amount / totalSpent).toFloat().coerceIn(0.01f, 1f)
                                        Box(modifier = Modifier.weight(fraction).fillMaxHeight().background(chartColors[index % chartColors.size]))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp)) // Added a breather between ribbon and list

                                categoryTotals.forEachIndexed { index, (category, amount) ->
                                    val limit = categoryBudgets[category]?.toDouble() ?: 0.0
                                    val percentage = if (totalSpent > 0) ((amount / totalSpent) * 100).toInt() else 0
                                    val isOverBudget = limit > 0 && amount > limit
                                    val barColor = if (isOverBudget) Color(0xFFE53935) else chartColors[index % chartColors.size]

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Line 1: Name and Percentage
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(chartColors[index % chartColors.size]))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(category, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Text("$percentage% of total", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Line 2: The Bar and Exact Amounts
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            val fraction = if (limit > 0) (amount / limit).toFloat() else (amount / maxCategoryAmount).toFloat()

                                            // Progress Bar
                                            Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                                Box(modifier = Modifier.fillMaxWidth(fraction.coerceIn(0.01f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(barColor))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))

                                            // The Exact Amounts
                                            if (limit > 0) {
                                                Text("₹${formatter.format(amount)} / ₹${formatter.format(limit)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(isOverBudget) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface)
                                            } else {
                                                Text("₹${formatter.format(amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 🔥 MODE 2: STOCK GRAPH (Fixed Exact Y-Axis & X-Axis Formats)
                    2 -> {
                        val w1 = timeframeExpenses.filter { it.date.take(2).toIntOrNull() in 1..7 }.sumOf { it.amount }
                        val w2 = timeframeExpenses.filter { it.date.take(2).toIntOrNull() in 8..14 }.sumOf { it.amount }
                        val w3 = timeframeExpenses.filter { it.date.take(2).toIntOrNull() in 15..21 }.sumOf { it.amount }
                        val w4 = timeframeExpenses.filter { (it.date.take(2).toIntOrNull() ?: 0) >= 22 }.sumOf { it.amount }
                        val maxWeek = maxOf(w1, w2, w3, w4, 1.0)

                        val points = listOf(w1, w2, w3, w4)
                        val lineColor = MaterialTheme.colorScheme.primary

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                            // --- Y-AXIS LABELS (EXACT AMOUNTS, NO 'K') ---
                            Column(
                                modifier = Modifier.height(160.dp).padding(end = 12.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("₹${formatter.format(maxWeek)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Text("₹${formatter.format(maxWeek / 2)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Text("₹0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }

                            // --- GRAPH & X-AXIS ---
                            Column(modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val canvasWidth = size.width
                                        val canvasHeight = size.height
                                        val paddingX = canvasWidth / (points.size * 2)

                                        val safeTop = 12f
                                        val safeBottom = canvasHeight - 12f
                                        val safeHeight = safeBottom - safeTop

                                        // Draw Horizontal Background Gridlines
                                        drawLine(color = androidx.compose.ui.graphics.Color.Gray.copy(alpha=0.15f), start = androidx.compose.ui.geometry.Offset(0f, safeTop), end = androidx.compose.ui.geometry.Offset(canvasWidth, safeTop), strokeWidth = 2f)
                                        drawLine(color = androidx.compose.ui.graphics.Color.Gray.copy(alpha=0.15f), start = androidx.compose.ui.geometry.Offset(0f, safeTop + (safeHeight/2)), end = androidx.compose.ui.geometry.Offset(canvasWidth, safeTop + (safeHeight/2)), strokeWidth = 2f)
                                        drawLine(color = androidx.compose.ui.graphics.Color.Gray.copy(alpha=0.15f), start = androidx.compose.ui.geometry.Offset(0f, safeBottom), end = androidx.compose.ui.geometry.Offset(canvasWidth, safeBottom), strokeWidth = 2f)

                                        val path = androidx.compose.ui.graphics.Path()

                                        // Draw the data points
                                        points.forEachIndexed { index, amount ->
                                            val x = paddingX + (index * (canvasWidth / points.size))
                                            val y = safeBottom - ((amount / maxWeek).toFloat() * safeHeight)

                                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                            drawCircle(color = lineColor, radius = 12f, center = androidx.compose.ui.geometry.Offset(x, y))
                                            drawCircle(color = androidx.compose.ui.graphics.Color.White, radius = 6f, center = androidx.compose.ui.geometry.Offset(x, y))
                                        }
                                        drawPath(path = path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                // X-Axis Labels (EXACT AMOUNTS)
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    points.forEachIndexed { index, amount ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("W${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(if(amount>0) "₹${formatter.format(amount)}" else "₹0", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // --- FULLY RESTORED DATE PICKER ---
    if (showDatePicker) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                val currentYear = java.time.LocalDate.now().year
                val currentMonthValue = java.time.LocalDate.now().monthValue
                var pickerMode by remember { mutableIntStateOf(0) }
                var tempYear by remember { mutableIntStateOf(currentYear) }

                val initialMonth = selectedTimeframe.split(" ").getOrNull(0) ?: currentMonthStr
                var tempMonth by remember { mutableStateOf(initialMonth) }

                Column(modifier = Modifier.animateContentSize().padding(top = 16.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape).padding(4.dp)) {
                        listOf("Month", "Year").forEachIndexed { index, mode ->
                            Box(modifier = Modifier.weight(1f).background(if (pickerMode == index) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape).clickable { pickerMode = index }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(mode, color = if (pickerMode == index) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                        }
                    }
                    Box(modifier = Modifier.padding(top = 16.dp)) {
                        when (pickerMode) {
                            0 -> { Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    androidx.compose.material3.IconButton(onClick = { tempYear-- }, modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), CircleShape)) { Icon(Icons.Rounded.ChevronLeft, contentDescription = null, modifier = Modifier.size(24.dp)) }
                                    Text(tempYear.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    androidx.compose.material3.IconButton(onClick = { if (tempYear < currentYear) tempYear++ }, enabled = tempYear < currentYear, modifier = Modifier.size(36.dp).background(if (tempYear < currentYear) MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f) else Color.Transparent, CircleShape)) { Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (tempYear < currentYear) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)) }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    months.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            rowMonths.forEachIndexed { colIndex, month ->
                                                val monthNumber = (rowIndex * 3) + colIndex + 1
                                                val isSelected = tempMonth == month
                                                val isFuture = tempYear == currentYear && monthNumber > currentMonthValue
                                                Box(modifier = Modifier.weight(1f).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else if (isFuture) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).then(if (!isFuture) Modifier.clickable { tempMonth = month } else Modifier).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                                    Text(month, fontWeight = FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (isFuture) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface)
                                                }
                                            }
                                        }
                                    }
                                }
                            }}
                            1 -> { val years = ((currentYear - 11)..currentYear).toList()
                                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    years.chunked(3).forEach { rowYears ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            rowYears.forEach { year ->
                                                val isSelected = tempYear == year
                                                Box(modifier = Modifier.weight(1f).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).clickable { tempYear = year }.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                                    Text(year.toString(), fontWeight = FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                        val exactButtonWidth = 80.dp
                        Box(modifier = Modifier.width(exactButtonWidth).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).clickable { selectedTimeframe = "$currentMonthStr $currentYearNum"; showDatePicker = false }.padding(vertical = 10.dp), Alignment.Center) {
                            Text("Clear", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.Button(onClick = { selectedTimeframe = if (pickerMode == 0) "$tempMonth $tempYear" else "$tempYear"; showDatePicker = false }, modifier = Modifier.width(exactButtonWidth), shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun AccountsTab(){
    val formatter = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply { minimumFractionDigits = 0; maximumFractionDigits = 0 } }
    var isBalanceVisible by remember { mutableStateOf(false) }

    // We keep the Total Net Worth math for the Hero Card
    val totalCash = 40000.0
    val totalFds = 50000.0
    val totalInvestments = 120000.0
    val totalDebt = -12000.0
    val netWorth = totalCash + totalFds + totalInvestments + totalDebt

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. THE NET WORTH HERO CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.75f))
        ) {
            // We use a Box here so we can pin the icon to the top right while keeping text centered
            Box(
                modifier = Modifier.fillMaxWidth().padding(all = 22.dp)
            ) {
                // 1. THE TOP-RIGHT EYE ICON
                Icon(
                    imageVector = if (isBalanceVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = "Toggle Balance Privacy",
                    modifier = Modifier
                        .align(Alignment.TopEnd) // <-- This pins it directly to the top right corner!
                        .size(24.dp)
                        .clickable { isBalanceVisible = !isBalanceVisible },
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                // 2. THE MAIN CARD CONTENT (Centered)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Net Worth",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Massive Net Worth Number
                    Text(
                        text = if (isBalanceVisible) "₹ 14,50,000" else "₹ ••••••",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Assets and Liabilities Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Assets
                        Column(horizontalAlignment = Alignment.Start) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Text(" Assets", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            }
                            Text(
                                text = if (isBalanceVisible) "₹ 16,00,000" else "₹ ••••••",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Liabilities
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                                Text(" Liabilities", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            }
                            Text(
                                text = if (isBalanceVisible) "₹ 1,50,000" else "₹ ••••••",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // --- 2. MONEY SECTION (Sleek Horizontal Pills) ---
        Text("Money", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // ROW 1: Bank & FDs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyGridBox(modifier = Modifier.weight(1f), icon = Icons.Rounded.AccountBalance, title = "Bank Accounts", onClick = { })
                MoneyGridBox(modifier = Modifier.weight(1f), icon = Icons.Rounded.Lock, title = "Fixed Deposits", onClick = { })
            }
            // ROW 2: Cash & Cards
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyGridBox(modifier = Modifier.weight(1f), iconRes =R.drawable.money_acc, title = "Physical Cash", onClick = { })
                MoneyGridBox(modifier = Modifier.weight(1f), icon = Icons.Rounded.CreditCard, title = "Credit Cards", onClick = { })
            }
        }

        // --- 3. INVESTMENTS SECTION (2x3 Squares, Icons + Text Only) ---
        Text("Investments", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ROW 1: Stocks, SIP, Gold
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InvestmentSquareBox(modifier = Modifier.weight(1f), iconRes = R.drawable.candlestick, title = "Stocks & MFs", onClick = {  })
                InvestmentSquareBox(modifier = Modifier.weight(1f), iconRes =R.drawable.icons8_gold_48__1_, title = "Gold & Metals", onClick = { })
                InvestmentSquareBox(modifier = Modifier.weight(1f), iconRes = R.drawable.market, title = "Real Estate", onClick = { })
            }
            // ROW 2: PPF, Crypto, Other
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InvestmentSquareBox(modifier = Modifier.weight(1f), iconRes = R.drawable.paper, title = "EPF & PPF", onClick = { })
                InvestmentSquareBox(modifier = Modifier.weight(1f), iconRes = R.drawable.virtual_currency, title = "Digital Assets", onClick = { })
                InvestmentSquareBox(modifier = Modifier.weight(1f), icon = Icons.Rounded.Category, title = "Other Assets", onClick = { })
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun historyShowDatePicker( onDateSelected: (String?) -> Unit, onDismiss: () -> Unit){
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                val currentLocalDate = java.time.LocalDate.now()
                val currentYear = currentLocalDate.year
                val currentMonthValue = currentLocalDate.monthValue

                var pickerMode by remember { mutableIntStateOf(0) }
                var tempYear by remember { mutableIntStateOf(currentYear) }
                var tempMonth by remember { mutableStateOf(currentLocalDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM"))) }
                val datePickerState = rememberDatePickerState(
                    selectableDates = object :SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= System.currentTimeMillis()
                        override fun isSelectableYear(year: Int): Boolean = year <= currentYear
                    }
                )

                Column(
                    modifier = Modifier.animateContentSize().padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- 1. THE TOGGLE ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                            .padding(4.dp)
                    ) {
                        val modes = listOf("Date", "Month", "Year")
                        modes.forEachIndexed { index, mode ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (pickerMode == index) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Transparent, androidx.compose.foundation.shape.CircleShape)
                                    .clickable { pickerMode = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    color = if (pickerMode == index) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.padding(top = 16.dp)) {
                        when (pickerMode) {
                            // --- 2. DATE MODE ---
                            0 -> {
                                DatePicker(
                                    state = datePickerState,
                                    title = null,
                                    headline = null,
                                    showModeToggle = false,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

                            // --- 3. MONTH MODE ---
                            1 -> {
                                Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                                    // UPDATED PREMIUM ARROWS
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                        IconButton(
                                            onClick = { tempYear-- },
                                            modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), androidx.compose.foundation.shape.CircleShape)
                                        ) {
                                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous Year", modifier = Modifier.size(24.dp))
                                        }

                                        Text(tempYear.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                                        IconButton(
                                            onClick = { if (tempYear < currentYear) tempYear++ },
                                            enabled = tempYear < currentYear,
                                            modifier = Modifier.size(36.dp).background(if (tempYear < currentYear) MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f) else androidx.compose.ui.graphics.Color.Transparent, androidx.compose.foundation.shape.CircleShape)
                                        ) {
                                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next Year", modifier = Modifier.size(24.dp), tint = if (tempYear < currentYear) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        months.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                rowMonths.forEachIndexed { colIndex, month ->
                                                    val monthNumber = (rowIndex * 3) + colIndex + 1
                                                    val isSelected = tempMonth == month
                                                    val isFuture = tempYear == currentYear && monthNumber > currentMonthValue

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(
                                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                                else if (isFuture) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                                shape = RoundedCornerShape(16.dp) // Rounder grid
                                                            )
                                                            .then(if (!isFuture) Modifier.clickable { tempMonth = month } else Modifier)
                                                            .padding(vertical = 16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = month,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                            else if (isFuture) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                            else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // --- 4. YEAR MODE ---
                            2 -> {
                                val years = ((currentYear - 11)..currentYear).toList()
                                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    years.chunked(3).forEach { rowYears ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            rowYears.forEach { year ->
                                                val isSelected = tempYear == year
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(16.dp)) // Rounder grid
                                                        .clickable { tempYear = year }
                                                        .padding(vertical = 16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = year.toString(), fontWeight = FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 5. BOTTOM ACTIONS ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val exactButtonWidth = 80.dp
                        Box(
                            modifier = Modifier
                                .width(exactButtonWidth)
                                .clip(RoundedCornerShape(50)) // Pill shape
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    onDateSelected(null)
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp),
                            Alignment.Center
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (pickerMode == 0) {datePickerState.selectedDateMillis?.let { millis ->
                                    val formatted = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.of("UTC")).toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
                                    onDateSelected(formatted) }
                                } else if (pickerMode == 1) {
                                    onDateSelected("$tempMonth $tempYear")
                                } else {
                                    onDateSelected("$tempYear")
                                }
                                onDismiss()
                            },
                            modifier = Modifier.width(exactButtonWidth),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp) // Shrinked horizontal width
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }

// THE LIST ITEM UI COMPONENT
@Composable
// 1. ADDED the onClick parameter here in the signature
fun TransactionItem(txn: Transaction, onClick: () -> Unit = {}) {
    val words = txn.title.trim().split("\\s+".toRegex())
    val initials = when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(1).uppercase()
        else -> "${words[0].take(1)}${words[1].take(1)}".uppercase()
    }

    // 2. ADDED the clickable modifier to the main Column
    Column(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = initials, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.Center) {
                Text(
                    text = txn.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = txn.date, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
            }
            val amountColor = if (txn.isExpense) Color(0xFFE57373) else Color(0xFF81C784)
            val sign = if (txn.isExpense) "-" else "+"
            val formatter = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 } }
            val formattedAmount = formatter.format(txn.amount)

            Text(text = "$sign ₹$formattedAmount", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = amountColor, maxLines = 1)
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
    }
}

//RECTANGLE MONEY BOX
@Composable
fun MoneyGridBox(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp), // Slightly sharper corners
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        modifier = modifier
            .height(60.dp) // Shorter, sleeker height
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // Left-aligned
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        }
    }
}

// SQUARE INVESTMENT BOX
@Composable
fun InvestmentSquareBox(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp), // Matches the pills
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
        modifier = modifier
            .aspectRatio(1f) // Forces perfect square
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp), // Tighter padding (down from 12dp)
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

// CUSTOM XML BOX
@Composable
fun MoneyGridBox(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier.height(60.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        }
    }
}

// SQUARE INVESTMENT BOX
@Composable
fun InvestmentSquareBox(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier.aspectRatio(1f).clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp), // <-- Removed side margins to maximize width
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter =painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp, // <-- Shrunk the font to force it to fit
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, // <-- Strict single-line rule
                overflow =TextOverflow.Ellipsis, // If it still barely misses, adds "..." instead of cutting the letter in half
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PermissionScreen(hasNotifPermission: Boolean, onFinish: () -> Unit) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = if (!hasNotifPermission) "🛡️" else "🔋", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (!hasNotifPermission) "Step 1: Read Payments" else "Step 2: Stay Awake",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (!hasNotifPermission)
                    "XpenseMate needs to read your payment notifications to automatically log your expenses."
                else
                    "To ensure we never miss a transaction, allow XpenseMate to run efficiently in the background.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(40.dp))

            if (!hasNotifPermission) {
                // STEP 1 BUTTON
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enable Notifications", fontSize = 16.sp)
                }
            } else {
                // STEP 2 BUTTONS
                Button(
                    onClick = { requestBatteryExemption(context) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green to encourage clicking
                ) {
                    Text("Allow Background Tracking", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // We give them a "Skip" option just in case the OS blocks the battery popup
                TextButton(onClick = { onFinish() }) {
                    Text("Skip for now", color = Color.Gray)
                }
            }
        }
    }
}

fun requestBatteryExemption(context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = context.packageName

    // 1. Check if we are already whitelisted
    if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
        android.util.Log.d("XpenseMate_Setup", "✅ Already exempt from battery limits!")
        return
    }

    // 2. If not, trigger the OS Exemption Dialog
    try {
        android.util.Log.d("XpenseMate_Setup", "⚠️ Requesting battery exemption...")
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("XpenseMate_Setup", "Failed to open battery settings: ${e.message}")
    }
}
// --- BUDGET SETTINGS HELPER ---
object SettingsManager {
    private const val PREFS_NAME = "xpensemate_settings"

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
}

@Composable
fun BudgetSettingsView(
    currentDaily: Float,
    currentMonthly: Float,
    categories: List<String>, // 👈 1. We pass your custom categories in
    currentCategoryLimits: Map<String, Float>, // 👈 2. We pass the saved limits in
    onBackClick: () -> Unit,
    onSave: (Float, Float, Map<String, Float>) -> Unit // 👈 3. Returns the new map!
) {
    var dailyInput by remember { mutableStateOf(currentDaily.toInt().toString()) }
    var monthlyInput by remember { mutableStateOf(currentMonthly.toInt().toString()) }

    // Create a working copy of the category limits that the user can edit
    val workingCategoryLimits = remember { mutableStateMapOf<String, String>().apply {
        categories.forEach { cat ->
            put(cat, currentCategoryLimits[cat]?.toInt()?.toString() ?: "")
        }
    }}

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

            // --- HEADER ---
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(40.dp)) {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))) {
                    Text("Budgets & Limits", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }

            // We make the form scrollable so the Save button can float at the bottom
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- GLOBAL LIMITS ---
                Text("Global Limits", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                // 🔥 THE FIX: Changed from a Row to a Column
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // 1. Daily Ceiling Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth() // Swapped .weight(1f) for .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Daily Ceiling", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = dailyInput, onValueChange = { if (it.all { char -> char.isDigit() }) dailyInput = it },
                                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), prefix = { Text("₹ ", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // 2. Monthly Ceiling Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth() // Swapped .weight(1f) for .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Ceiling", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = monthlyInput, onValueChange = { if (it.all { char -> char.isDigit() }) monthlyInput = it },
                                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), prefix = { Text("₹ ", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // --- CATEGORY LIMITS ---
                Text("Category Limits", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categories.forEach { category ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), // Matches the AMOLED glass look
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = category.take(1).uppercase(),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(category, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }

                                // 🔥 THE FIX: A completely custom, perfectly centered input box
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent, // Blends perfectly with your card
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(40.dp) // Sleek, tight height that won't stretch the row
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("₹ ", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

                                        androidx.compose.foundation.text.BasicTextField(
                                            value = workingCategoryLimits[category] ?: "",
                                            onValueChange = { if (it.all { char -> char.isDigit() }) workingCategoryLimits[category] = it },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                            decorationBox = { innerTextField ->
                                                if (workingCategoryLimits[category].isNullOrEmpty()) {
                                                    Text("No Limit", color = Color.Gray, fontSize = 14.sp)
                                                }
                                                innerTextField()
                                            }
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Breathing room before the save button
            }

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    val d = dailyInput.toFloatOrNull() ?: currentDaily
                    val m = monthlyInput.toFloatOrNull() ?: currentMonthly
                    // Filter out empty strings and convert back to Floats
                    val finalCategoryMap = workingCategoryLimits
                        .filterValues { it.isNotBlank() }
                        .mapValues { it.value.toFloat() }

                    onSave(d, m, finalCategoryMap)
                },
                modifier = Modifier.fillMaxWidth().height(80.dp).padding(bottom = 24.dp, top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.background)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

object ThemeManager {
    private const val PREFS_NAME = "xpensemate_theme"
    private const val KEY_IS_DARK = "is_dark_mode"

    fun saveTheme(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK, isDark).apply()
    }

    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Default to 'true' since your app's identity is currently dark/silver
        return prefs.getBoolean(KEY_IS_DARK, true)
    }
}
@Composable
fun DesignSettingsView(
    isDarkMode: Boolean,
    onBackClick: () -> Unit,
    onThemeChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // --- ALPHA PILL HEADER (Your style) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Text("Theme & Design", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- THE VISUAL THEME SELECTOR ---
            Text("Select Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LIGHT MODE OPTION
                    ThemeOption(
                        title = "Light",
                        icon = Icons.Rounded.LightMode,
                        isSelected = !isDarkMode,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(false) }
                    )

                    // DARK MODE OPTION
                    ThemeOption(
                        title = "Dark",
                        icon = Icons.Rounded.DarkMode,
                        isSelected = isDarkMode,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(true) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Helpful tip for the user
            Text(
                "Dark mode is optimized for AMOLED screens to save battery and reduce eye strain in low-light environments.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.background else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = if (isSelected) MaterialTheme.colorScheme.background else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CategorySettingsView(
    currentCategories: List<String>,
    onBackClick: () -> Unit,
    onCategoriesUpdated: (List<String>) -> Unit // Sends the new list back to save
) {
    var newCategoryName by remember { mutableStateOf("") }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    // We create a "working copy" of the list that the UI can instantly add/remove from
    val editableCategories = remember { mutableStateListOf(*currentCategories.toTypedArray()) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

            // --- 1. ALPHA PILL HEADER ---
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(36.dp)) {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))) {
                    Text("Tags & Categories", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. ADD NEW CATEGORY FIELD ---
            val onAddCategory = {
                if (newCategoryName.isNotBlank() && !editableCategories.contains(newCategoryName.trim())) {
                    editableCategories.add(newCategoryName.trim()) // Add to UI
                    onCategoriesUpdated(editableCategories.toList()) // Save to disk
                    newCategoryName = "" // Clear the box
                }
            }

            OutlinedTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                placeholder = { Text("Add new category...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),

                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        onAddCategory() // 1. Saves your category
                        keyboardController?.hide() // 2. Forces the keyboard down
                        focusManager.clearFocus() // 3. Removes the blinking cursor from the box
                    }
                ),

                trailingIcon = {
                    IconButton(onClick = { onAddCategory() }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Your Categories", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            // --- 3. THE CATEGORY LIST ---
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                editableCategories.forEach { category ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Subtle grey box
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)

                            // Prevent deleting the very last category so the app doesn't break
                            if (editableCategories.size > 1) {
                                IconButton(
                                    onClick = {
                                        editableCategories.remove(category)
                                        onCategoriesUpdated(editableCategories.toList())
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsCard(
    transaction: Transaction,
    categories: List<String>,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onCategoryChange: (Transaction) -> Unit
) {
    val formatter = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 } }

    var expanded by remember { mutableStateOf(false) }
    var draftCategory by remember {
        mutableStateOf(if (isNew) "Select Category" else transaction.category)
    }
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background, // Pitch Black
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. THE HEADER ---
                Text(
                    text = "${if (transaction.isExpense) "-" else "+"} ₹${formatter.format(transaction.amount)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.isExpense) Color(0xFFE57373) else Color(0xFF81C784)
                )
                Text(
                    text = transaction.merchantName.ifEmpty { transaction.title },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- 2. FULL-WIDTH DETAILS BOX (Restored!) ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailRow(label = "Account", value = transaction.account)
                        DetailRow(label = "Phone Number", value = transaction.phoneNumber) // 👈 Just add this line!
                        DetailRow(label = "Payment Mode", value = transaction.transactionType)
                        DetailRow(label = "Source", value = transaction.source)
                        DetailRow(label = "Date", value = "${transaction.date} • ${transaction.time}")}
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 3. CATEGORY DROPDOWN ---
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth().height(50.dp).clickable { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = draftCategory,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxWidth(0.7f)
                            // 🔥 THE FIX: Shrunk from 240dp to 160dp.
                            // This ensures it only shows ~3 items and won't hit the bottom of the screen!
                            .heightIn(max = 160.dp)
                    ) {
                        categories.forEach { category ->
                                DropdownMenuItem(
                                text = { Text(category, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    draftCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 4. THE TWO BUTTONS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // The Alpha Close Button
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Close", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onCategoryChange(transaction.copy(category = draftCategory))
                        },
                        // 🚀 THE FIX: Locks the button until they change the dropdown
                        enabled = draftCategory != "Select Category",
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            // 🚀 THE FIX: Gray when locked, Black when ready
                            containerColor = if (draftCategory != "Select Category") MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.LightGray,
                            contentColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.LightGray,
                            disabledContentColor = androidx.compose.ui.graphics.Color.DarkGray
                        )
                    ) {
                        Text("Save", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 👈 RESTORED HELPER COMPONENT
@Composable
fun DetailRow(label: String, value: String)

{
    // Failsafe for ugly AI defaults
    val displayValue = if (value.isBlank() || value == "UNKNOWN") "N/A" else value

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp)) // Gives breathing room between label and value
        Text(
            text = displayValue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End // Aligns text to the right if it wraps
        )
    }
}@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun RecurringPaymentsView(
    subscriptions: List<Transaction>,
    onBackClick: () -> Unit,
    onSaveNew: (String, Double, String, String) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val totalSpend = subscriptions.sumOf { it.amount }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // --- 1. ALPHA PILL HEADER ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        IconButton(onClick = { onBackClick() }) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "Recurring Payments",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // --- 2. THE UPGRADED DASHBOARD CARD ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Monthly Spend", // 🚀 Shorter, cleaner title
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${String.format("%.2f", totalSpend)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // 🚀 The new total count label!
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${subscriptions.size} active payments",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Active Subscriptions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // --- 3. THE LIST ---
                if (subscriptions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No subscriptions added yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(subscriptions) { sub ->
                            Box(modifier = Modifier.padding(bottom = 8.dp)) {
                                SubscriptionItemCard(sub = sub, onDelete = { onDelete(sub) })
                            }
                        }
                    }
                }
            }

            // --- 4. FLOATING BUTTON ---
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New", fontWeight = FontWeight.SemiBold)
            }

            // --- 5. THE DIALOG ---
            if (showAddDialog) {
                AddSubscriptionDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { merchant, amount, category, mode ->
                        onSaveNew(merchant, amount, category, mode)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun SubscriptionItemCard(sub: Transaction, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp), // More rounded, modern look
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Sleek, highly rounded Icon Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        Icons.Rounded.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = sub.merchantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 🚀 New "Monthly" badge tag
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Monthly",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sub.transactionType, // Shows "UPI" or "Credit Card"
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "- ₹${String.format("%.2f", sub.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                // A subtle, smaller delete button that doesn't distract
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String) -> Unit
) {
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Entertainment") }

    // Dropdown State
    var paymentMode by remember { mutableStateOf("UPI") }
    var expanded by remember { mutableStateOf(false) }
    val paymentOptions = listOf("UPI", "Credit Card", "Debit Card", "Net Banking")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("New Subscription", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. Merchant Name
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant (e.g. Netflix)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // 2. Amount (NOW STRICTLY A NUMBER PAD)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, // 🚀 FORCES NUMBER PAD!
                        imeAction = ImeAction.Done
                    )
                )

                // 3. Payment Mode (NOW A SLEEK DROPDOWN)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = {},
                        readOnly = true, // User can't type, only select
                        label = { Text("Payment Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        paymentOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    paymentMode = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amount.toDoubleOrNull() ?: 0.0
                    if (merchant.isNotBlank() && finalAmount > 0) {
                        onSave(merchant, finalAmount, category, paymentMode)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

fun addManualSubscription(context: Context, merchant: String, amount: Double, category: String, paymentMode: String) {
    val currentDate = java.time.LocalDateTime.now()
    val newSubscription = Transaction(
        title = merchant,
        amount = amount,
        isExpense = true,
        category = category,
        date = currentDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
        time = currentDate.format(DateTimeFormatter.ofPattern("hh:mm a")),
        timestamp = System.currentTimeMillis(),
        merchantName = merchant,
        transactionType = paymentMode,
        account = "---",
        phoneNumber = "---",
        source = "SUBSCRIPTION" // 🚀 Safely flags it!
    )
    ExpenseEngine.saveTransaction(newSubscription)
    android.widget.Toast.makeText(context, "Subscription saved!", android.widget.Toast.LENGTH_SHORT).show()
}
