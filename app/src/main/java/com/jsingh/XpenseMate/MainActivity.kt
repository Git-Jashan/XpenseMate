package com.jsingh.XpenseMate

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
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

class MainActivity : ComponentActivity() {

    // ==========================================
    // 1. DATABASE CONNECTION
    // ==========================================
    private val db by lazy {
        androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "xpense_db"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XpenseMateTheme {
                // ==========================================
                // 2. APP STATE (MEMORY VARIABLES)
                // ==========================================
                var selectedItem by remember { mutableIntStateOf(0) }
                var showAddDialog by remember { mutableStateOf(false) }

                // Form Data
                var isExpense by remember { mutableStateOf(true) }
                var title by remember { mutableStateOf("") }
                var amount by remember { mutableStateOf("") }

                // Live Data
                val transactions by db.transactionDao().getAllTransactions().collectAsState(initial = emptyList())

                // Filters
                var searchQuery by remember { mutableStateOf("") }
                var activeFilter by remember { mutableStateOf("All") }
                var dateFilter by remember { mutableStateOf<String?>(null) }
                var showDatePicker by remember { mutableStateOf(false) }

                val coroutineScope = rememberCoroutineScope()

                // Standard UI Colors
                val expenseColor = Color(0xFFE57373)
                val incomeColor = Color(0xFF81C784)

                val drawerState = androidx.compose.material3.rememberDrawerState(
                    initialValue = androidx.compose.material3.DrawerValue.Closed
                )

                // ==========================================
                // 3. THE LEFT-SIDE NAVIGATION DRAWER
                // ==========================================
                androidx.compose.material3.ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        androidx.compose.material3.ModalDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth(0.80f)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {

                                // --- HEADER: ACCOUNT & PREMIUM CARD ---
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .clickable { /* TODO: Open Account Info */ }
                                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
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
                                                Text("JS", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("J. Singh", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text("jsingh@gmail.com", fontSize = 14.sp, color = Color.Gray)
                                        }
                                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Open Account", tint = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                        ) {
                                            Text(
                                                text = "Free Plan",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Upgrade to Pro",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                // --- MIDDLE: SCROLLABLE FEATURE LIST ---
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Group 1
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.AccountBalance, contentDescription = null) },
                                        label = { Text("Manage Banks & Wallets", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.PieChart, contentDescription = null) },
                                        label = { Text("Budgets & Limits", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Label, contentDescription = null) },
                                        label = { Text("Tags & Categories", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Repeat, contentDescription = null) },
                                        label = { Text("Recurring Payments", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                    // Group 2
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                                        label = { Text("Theme & Design", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Notifications, contentDescription = null) },
                                        label = { Text("Notifications", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                    // Group 3
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                                        label = { Text("Privacy & Security", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Cloud, contentDescription = null) },
                                        label = { Text("Cloud Backup & Sync", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                                        label = { Text("Export Data", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                    // Group 4
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Help, contentDescription = null) },
                                        label = { Text("Contact & Support", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    androidx.compose.material3.NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Description, contentDescription = null) },
                                        label = { Text("Terms & Policy", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // --- FOOTER: VERSION ---
                                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("XpenseMate v1.0", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                ) {
                    // ==========================================
                    // 4. MAIN APP CONTENT SCAFFOLD
                    // ==========================================
                    @OptIn(ExperimentalMaterial3Api::class)
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),

                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
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
                            BottomAppBar(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Rounded.Home, "Home") },
                                    label = { Text("Home") },
                                    selected = selectedItem == 0,
                                    onClick = { selectedItem = 0 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.history), "Payments") },
                                    label = { Text("Payments") },
                                    selected = selectedItem == 1,
                                    onClick = { selectedItem = 1 }
                                )

                                NavigationBarItem(
                                    selected = false,
                                    onClick = { showAddDialog = true },
                                    icon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                            modifier = Modifier.size(60.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Rounded.Add, "Add", tint = MaterialTheme.colorScheme.inverseOnSurface, modifier = Modifier.size(35.dp))
                                            }
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(painter = painterResource(id = R.drawable.analytics), contentDescription = "Analytics") },
                                    label = { Text("Analytics") },
                                    selected = selectedItem == 2,
                                    onClick = { selectedItem = 2 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Rounded.AccountCircle, "Accounts") },
                                    label = { Text("Accounts") },
                                    selected = selectedItem == 3,
                                    onClick = { selectedItem = 3 }
                                )
                            }
                        }
                    ) { innerPadding ->

                        Box(modifier = Modifier.padding(innerPadding)) {

                            when (selectedItem) {
                                // ------------------------------------------
                                // TAB 0: THE HOME DASHBOARD
                                // ------------------------------------------
                                0 -> {
                                    val todayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                                    val todayString = LocalDateTime.now().format(todayFormatter)
                                    val currentMonthString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM yyyy"))

                                    val dailySpent = transactions.filter { it.isExpense && it.date == todayString }.sumOf { it.amount }
                                    val monthlySpent = transactions.filter { it.isExpense && it.date.contains(currentMonthString) }.sumOf { it.amount }

                                    val dailyLimit = 1000.0
                                    val monthlyLimit = 30000.0

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
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(all = 20.dp)) {
                                                Text("Daily Spending", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text("₹${formatter.format(dailySpent)}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                                    Text(" / ₹${formatter.format(dailyLimit)}", fontSize = 16.sp, modifier = Modifier.padding(bottom = 3.dp), color = MaterialTheme.colorScheme.primary)
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
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
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
                                                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                                            drawArc(
                                                                color = Color.LightGray.copy(alpha = 0.3f),
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

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(160.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxWidth()
                                                        .clickable { /* Open PopUp */ }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(painterResource(id = R.drawable.ic_category), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                                        Spacer(modifier = Modifier.width(14.dp))
                                                        Text("Category\nSpending", fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxWidth()
                                                        .clickable { /* Open PopUp */ }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
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
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
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
                                                                color = if (selectedHomeTab == index) MaterialTheme.colorScheme.surface else Color.Transparent,
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
                                                Surface(shape = RoundedCornerShape(16.dp), color = warningColor.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth()) {
                                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Text("💳", fontSize = 24.sp)
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text("Netflix Subscription", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                            Text("₹649 • Due in 2 Days", fontSize = 12.sp, color = Color.Gray)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // ------------------------------------------
                                // TAB 1: PAYMENTS HISTORY
                                // ------------------------------------------
                                1 -> {
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
                                                    .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = CircleShape)
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
                                                onClick = { showDatePicker = true },
                                                modifier = Modifier.background(color = if (dateFilter != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                                            ) {
                                                Icon(Icons.Rounded.DateRange, "Calendar", tint = if (dateFilter != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        // Filter Chips
                                        val filterOptions = listOf("All", "Expense", "Income", "This Week", "This Month")
                                        androidx.compose.foundation.lazy.LazyRow(
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            items(filterOptions.size) { index ->
                                                val filter = filterOptions[index]
                                                FilterChip(
                                                    selected = activeFilter == filter,
                                                    onClick = { activeFilter = filter },
                                                    label = { Text(filter, fontWeight = FontWeight.Medium) },
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.onSurface, selectedLabelColor = MaterialTheme.colorScheme.surface)
                                                )
                                            }
                                        }

                                        // The Full Scrollable List
                                        androidx.compose.foundation.lazy.LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(top = 0.dp),
                                            verticalArrangement = Arrangement.spacedBy(0.dp)
                                        ) {
                                            items(displayedTransactions.size) { index ->
                                                TransactionItem(txn = displayedTransactions[index])
                                            }
                                        }
                                    }
                                }
// ------------------------------------------
                                // TAB 3: WEALTH & ACCOUNTS (Clean Launchpad)
                                // ------------------------------------------
                                3 -> {
                                    val formatter = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).apply { minimumFractionDigits = 0; maximumFractionDigits = 0 } }

                                    // We keep the Total Net Worth math for the Hero Card
                                    val totalCash = 40000.0
                                    val totalFds = 50000.0
                                    val totalInvestments = 120000.0
                                    val totalDebt = -12000.0
                                    val netWorth = totalCash + totalFds + totalInvestments + totalDebt

                                    // Popup States
                                    var showCardsScreen by remember { mutableStateOf(false) }
                                    var showStocksScreen by remember { mutableStateOf(false) }
                                    var showSipsScreen by remember { mutableStateOf(false) }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        Spacer(modifier = Modifier.height(4.dp))

                                        // --- 1. THE NET WORTH HERO CARD ---
                                        Surface(
                                            shape = RoundedCornerShape(24.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("Total Net Worth", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("₹${formatter.format(netWorth)}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Assets", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                                        Text("+₹${formatter.format(totalCash + totalFds + totalInvestments)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50))
                                                    }
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Liabilities", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                                        Text("-₹${formatter.format(Math.abs(totalDebt))}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE57373))
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
                                                MoneyGridBox(modifier = Modifier.weight(1f), icon = Icons.Rounded.CreditCard, title = "Credit Cards", onClick = { showCardsScreen = true })
                                            }
                                        }

                                        // --- 3. INVESTMENTS SECTION (2x3 Squares, Icons + Text Only) ---
                                        Text("Investments", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            // ROW 1: Stocks, SIP, Gold
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                InvestmentSquareBox(modifier = Modifier.weight(1f), iconRes = R.drawable.candlestick, title = "Stocks & MFs", onClick = { showStocksScreen = true })
                                                InvestmentSquareBox(modifier = Modifier.weight(1f), iconRes =R.drawable.icons8_gold_48__1_, title = "Gold & Metals", onClick = { showSipsScreen = true })
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

                                    // --- Wealth Tab Popups (Unchanged) ---
                                    if (showCardsScreen) {
                                        Dialog(onDismissRequest = { showCardsScreen = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
                                            Surface(modifier = Modifier.fillMaxSize().padding(top = 60.dp), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), color = MaterialTheme.colorScheme.background) {
                                                Column(modifier = Modifier.padding(24.dp)) {
                                                    Text("My Credit Cards", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(24.dp))
                                                    Text("Card details will go here...", color = Color.Gray)
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Button(onClick = { showCardsScreen = false }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Close") }
                                                }
                                            }
                                        }
                                    }

                                    if (showStocksScreen) {
                                        Dialog(onDismissRequest = { showStocksScreen = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
                                            Surface(modifier = Modifier.fillMaxSize().padding(top = 60.dp), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), color = MaterialTheme.colorScheme.background) {
                                                Column(modifier = Modifier.padding(24.dp)) {
                                                    Text("Stock Portfolio", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(24.dp))
                                                    Text("Zerodha, Groww, etc. will go here...", color = Color.Gray)
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Button(onClick = { showStocksScreen = false }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Close") }
                                                }
                                            }
                                        }
                                    }

                                    if (showSipsScreen) {
                                        Dialog(onDismissRequest = { showSipsScreen = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
                                            Surface(modifier = Modifier.fillMaxSize().padding(top = 60.dp), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), color = MaterialTheme.colorScheme.background) {
                                                Column(modifier = Modifier.padding(24.dp)) {
                                                    Text("SIPs & Mutual Funds", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(24.dp))
                                                    Text("Monthly investments will go here...", color = Color.Gray)
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Button(onClick = { showSipsScreen = false }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Close") }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ==========================================
                            // 6. NEW TRANSACTION POPUP LOGIC
                            // ==========================================
                            // This sits OUTSIDE the "when" block so it can pop up from any tab!
                            if (showAddDialog) {
                                AlertDialog(
                                    onDismissRequest = { showAddDialog = false },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    title = null,
                                    shape = RoundedCornerShape(24.dp),
                                    text = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("New Transaction", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                                            // Expense/Income Toggle Switch
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), shape = CircleShape)
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
                                                    Text("Expense", color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(color = if (!isExpense) incomeColor else Color.Transparent, shape = CircleShape)
                                                        .clickable { isExpense = false }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("Income", color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                                }
                                            }

                                            // Form Fields
                                            OutlinedTextField(
                                                value = title,
                                                onValueChange = { title = it },
                                                label = { Text("Title") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = amount,
                                                onValueChange = { newText -> amount = newText.filter { char -> char.isDigit() || char == '.' } },
                                                label = { Text("Amount") },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                prefix = { Text("₹ ", fontWeight = FontWeight.Normal, fontSize = 16.sp) }
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                if (title.isNotBlank() && amount.isNotBlank()) {
                                                    val now = LocalDateTime.now()
                                                    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                                                    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

                                                    val titleToSave = title
                                                    val amountToSave = amount.toDoubleOrNull() ?: 0.00
                                                    val isExpenseToSave = isExpense

                                                    coroutineScope.launch {
                                                        db.transactionDao().insertTransaction(
                                                            Transaction(
                                                                title = titleToSave,
                                                                amount = amountToSave,
                                                                isExpense = isExpenseToSave,
                                                                date = now.format(dateFormatter),
                                                                time = now.format(timeFormatter)
                                                            )
                                                        )
                                                    }
                                                    showAddDialog = false
                                                    title = ""; amount = ""
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
                                        ) { Text("Save") }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showAddDialog = false },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), contentColor = MaterialTheme.colorScheme.onSurface)
                                        ) { Text("Cancel", modifier = Modifier.padding(horizontal = 6.dp)) }
                                    }
                                )
                            }

                            // ==========================================
                            // 7. CALENDAR DATEPICKER
                            // ==========================================
                            // Sits OUTSIDE the "when" block so it can pop up from any tab!
                            if (showDatePicker) {
                                Dialog(
                                    onDismissRequest = { showDatePicker = false },
                                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                    ) {
                                        var pickerMode by remember { mutableIntStateOf(0) }
                                        val datePickerState = androidx.compose.material3.rememberDatePickerState()
                                        var selectedYear by remember { mutableIntStateOf(LocalDateTime.now().year) }
                                        var selectedMonth by remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM"))) }

                                        Column(
                                            modifier = Modifier.animateContentSize().padding(top = 16.dp, bottom = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), CircleShape)
                                                    .padding(4.dp)
                                            ) {
                                                val modes = listOf("Date", "Month", "Year")
                                                modes.forEachIndexed { index, mode ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(if (pickerMode == index) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
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
                                                    0 -> {
                                                        androidx.compose.material3.DatePicker(
                                                            state = datePickerState,
                                                            title = null,
                                                            headline = null,
                                                            showModeToggle = false,
                                                            modifier = Modifier.padding(horizontal = 8.dp)
                                                        )
                                                    }
                                                    1 -> {
                                                        Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                                IconButton(onClick = { selectedYear-- }) { Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                                                                Text(selectedYear.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                                                IconButton(onClick = { selectedYear++ }) { Text(">", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                                                            }
                                                            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                months.chunked(3).forEach { rowMonths ->
                                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                                        rowMonths.forEach { month ->
                                                                            val isSelected = selectedMonth == month
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .weight(1f)
                                                                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(12.dp))
                                                                                    .clickable { selectedMonth = month }
                                                                                    .padding(vertical = 16.dp),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                Text(text = month, fontWeight = FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    2 -> {
                                                        val currentYear = LocalDateTime.now().year
                                                        val years = (currentYear - 5 .. currentYear + 6).toList()
                                                        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            years.chunked(3).forEach { rowYears ->
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                                    rowYears.forEach { year ->
                                                                        val isSelected = selectedYear == year
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .weight(1f)
                                                                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(12.dp))
                                                                                .clickable { selectedYear = year }
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

                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { dateFilter = null; showDatePicker = false }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
                                                Button(onClick = {
                                                    if (pickerMode == 0) {
                                                        datePickerState.selectedDateMillis?.let { millis ->
                                                            dateFilter = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.of("UTC")).toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                                                        }
                                                    } else if (pickerMode == 1) {
                                                        dateFilter = "$selectedMonth $selectedYear"
                                                    } else {
                                                        dateFilter = "$selectedYear"
                                                    }
                                                    showDatePicker = false
                                                }) { Text("Apply") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. DATABASE TABLE ARCHITECTURE
// ==========================================
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val isExpense: Boolean,
    val date: String,
    val time: String
)

// ==========================================
// 9. THE LIST ITEM UI COMPONENT
// ==========================================
@Composable
fun TransactionItem(txn: Transaction) {
    val words = txn.title.trim().split("\\s+".toRegex())
    val initials = when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(1).uppercase()
        else -> "${words[0].take(1)}${words[1].take(1)}".uppercase()
    }

    Column {
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
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
// ==========================================
// 10. RECTANGLE MONEY BOX (Sleek Horizontal Pill)
// ==========================================
@Composable
fun MoneyGridBox(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp), // Slightly sharper corners
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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

// ==========================================
// 11. SQUARE INVESTMENT BOX (Tighter Padding)
// ==========================================
@Composable
fun InvestmentSquareBox(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp), // Matches the pills
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
// ==========================================
// CUSTOM XML BOX (Paste at the bottom)
// ==========================================
@Composable
fun MoneyGridBox(
    modifier: Modifier = Modifier,
    iconRes: Int, // <-- This grabs your XML file
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier.height(60.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        }
    }
}
// ==========================================
// 11. SQUARE INVESTMENT BOX (For Custom XML / R.drawable)
// ==========================================
@Composable
fun InvestmentSquareBox(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier.aspectRatio(1f).clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp), // <-- Removed side margins to maximize width
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = iconRes),
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
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, // If it still barely misses, adds "..." instead of cutting the letter in half
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}