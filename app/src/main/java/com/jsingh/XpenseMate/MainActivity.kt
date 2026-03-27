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
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import androidx.room.Room.databaseBuilder
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {

    //DATABASE CONNECTION
    private val db by lazy {
            databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "xpense_db"
        ).fallbackToDestructiveMigration()
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XpenseMateTheme {
                //APP STATE
                var selectedItem by remember { mutableIntStateOf(0) }
                var showAddDialog by remember { mutableStateOf(false) }

                val transactions by db.transactionDao().getAllTransactions().collectAsState(initial = emptyList())

                val coroutineScope = rememberCoroutineScope()

                // LEFT-SIDE NAVIGATION DRAWER
                val drawerState = rememberDrawerState(
                    initialValue = DrawerValue.Closed
                )

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
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.AccountBalance, contentDescription = null) },
                                        label = { Text("Manage Banks & Wallets", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.PieChart, contentDescription = null) },
                                        label = { Text("Budgets & Limits", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Label, contentDescription = null) },
                                        label = { Text("Tags & Categories", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Repeat, contentDescription = null) },
                                        label = { Text("Recurring Payments", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                    // Group 2
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                                        label = { Text("Theme & Design", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Notifications, contentDescription = null) },
                                        label = { Text("Notifications", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                    // Group 3
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                                        label = { Text("Privacy & Security", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Cloud, contentDescription = null) },
                                        label = { Text("Cloud Backup & Sync", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                                        label = { Text("Export Data", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                    // Group 4
                                   NavigationDrawerItem(
                                        icon = { Icon(Icons.Rounded.Help, contentDescription = null) },
                                        label = { Text("Contact & Support", fontWeight = FontWeight.Medium) },
                                        selected = false, onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    NavigationDrawerItem(
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
                    //  MAIN APP CONTENT SCAFFOLD
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
                                    label = { Text("History") },
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

                                0 -> {HomeTab(transactions = transactions) }

                                1 -> {HistoryTab(transactions = transactions)}

                                2 ->{AnalyticsTab()}

                                3 -> {AccountsTab()}
                            }

                            //TRANSACTION POPUP
                            if (showAddDialog) {TransactionPopup(
                                onDismiss = { showAddDialog = false },
                                onSave = { savedTitle, savedAmount, savedIsExpense ->
                                    val now = LocalDateTime.now()
                                    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                                    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

                                    val amountDouble = savedAmount.toDoubleOrNull() ?: 0.0

                                    coroutineScope.launch {
                                        db.transactionDao().insertTransaction(
                                            Transaction(
                                                title = savedTitle,
                                                amount = amountDouble,
                                                isExpense = savedIsExpense,
                                                date = now.format(dateFormatter),
                                                time = now.format(timeFormatter)
                                            )
                                        )
                                    }
                                }
                            )}
                        }
                    }
                }
            }
        }
    }
}

//TABS
@Composable
fun HomeTab(transactions: List<Transaction>){

    var recurringSubTab by remember { mutableStateOf("Pending") }
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
                                        ) else androidx.compose.ui.graphics.Color.Transparent
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
}

@Composable
fun HistoryTab(transactions: List<Transaction>){

        var showDatePicker by remember { mutableStateOf(false) }
        var dateFilter by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var activeFilter by remember { mutableStateOf("All") }
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
                onClick = { showDatePicker=true },
                modifier = Modifier.background(color = if (dateFilter != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
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

@Composable
fun TransactionPopup(onDismiss: () -> Unit,onSave: (String, String, Boolean) -> Unit){
    var isExpense by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val expenseColor = Color(0xFFE57373)
    val incomeColor = Color(0xFF81C784)

    AlertDialog(
        onDismissRequest = { onDismiss() },
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
                        onSave(title, amount, isExpense)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), contentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text("Cancel", modifier = Modifier.padding(horizontal = 6.dp)) }
        }
    )
}
@Composable
fun AnalyticsTab() {
    // Dynamically get the current date so it's always accurate
    val currentLocalDate = java.time.LocalDate.now()
    val currentYearNum = currentLocalDate.year
    val currentMonthStr = currentLocalDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM"))

    // Set the default timeframe to THIS month instead of a hardcoded string
    var selectedTimeframe by remember { mutableStateOf("$currentMonthStr $currentYearNum") }
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp)
    ) {
        // --- ROW 1: TITLE & THE BUTTON ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly spending",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            // The Dropdown Trigger Button
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clickable { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 14.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTimeframe,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- ROW 2: MASSIVE AMOUNT & TREND ---
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "₹ 34,250",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.ArrowDownward,
                        contentDescription = "Down",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "12% vs last",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        smartInsightCard()

        Spacer(modifier = Modifier.height(16.dp))

        var selectedMode by remember { mutableIntStateOf(0) } // 0 = Heatmap, 1 = Donut, 2 = Velocity

        // 2. The Main Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp).animateContentSize()
            ) {

                // --- THE DESIGNER 3-WAY TOGGLE (Upgraded) ---
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(4.dp), // Inner padding so the pill floats
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val modes = listOf("Heatmap", "Categories", "Velocity")
                        modes.forEachIndexed { index, title ->
                            val isSelected = selectedMode == index

                            // Using a Surface for the selected item creates a real 3D shadow effect
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null // Removes the ugly grey ripple effect for a cleaner tap
                                    ) { selectedMode = index },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
                                shadowElevation = if (isSelected) 2.dp else 0.dp // Floating shadow only on the active pill
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Added more breathing room

                // --- THE CONTENT ---
                when (selectedMode) {
                    // MODE 0: THE BEHAVIORAL CALENDAR HEATMAP
                    0 -> {
                        // Days of the week header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                                Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val daysInMonth = (1..30).toList()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            daysInMonth.chunked(7).forEach { week ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    for (i in 0 until 7) {
                                        if (i < week.size) {
                                            val day = week[i]
                                            val healthStatus = when {
                                                day % 5 == 0 -> "Over"
                                                day % 3 == 0 -> "Warning"
                                                else -> "Good"
                                            }

                                            val bgColor = when (healthStatus) {
                                                "Good" -> androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                "Warning" -> androidx.compose.ui.graphics.Color(0xFFFFB74D).copy(alpha = 0.3f)
                                                else -> androidx.compose.ui.graphics.Color(0xFFE53935).copy(alpha = 0.2f)
                                            }
                                            val textColor = when (healthStatus) {
                                                "Good" -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                                "Warning" -> androidx.compose.ui.graphics.Color(0xFFF57C00)
                                                else -> androidx.compose.ui.graphics.Color(0xFFC62828)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(bgColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(day.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        // INLINED Legend
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFF4CAF50)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Under", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFFFFB74D)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Close", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFFE53935)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Over", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // MODE 1: DISTRIBUTION (Native Donut Chart)
                    1 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(130.dp)) {
                                    val strokeWidth = 40f
                                    drawArc(color = Color(0xFFE53935), startAngle = -90f, sweepAngle = 162f, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                                    drawArc(color = Color(0xFF1E88E5), startAngle = 72f, sweepAngle = 108f, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                                    drawArc(color = Color(0xFFFFB74D), startAngle = 180f, sweepAngle = 90f, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Food", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("45%", fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            // INLINED Legend
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFFE53935)))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Food", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFF1E88E5)))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Transport", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFFFFB74D)))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Shopping", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // MODE 2: VELOCITY (Weekly Bar Chart)
                    2 -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Inclined Bars

                                val weeks = listOf(
                                    Triple("W1", 0.6f, "12K"),
                                    Triple("W2", 0.9f, "18K"), // Spike
                                    Triple("W3", 0.4f, "8K"),
                                    Triple("W4", 0.2f, "4K")
                                )

                                weeks.forEach { (week, heightFraction , amount) ->
                                    val isDanger = heightFraction >= 0.7f
                                    val nearBudget = heightFraction > 0.35f && heightFraction < 0.7f
                                    val barColor = if (isDanger) Color(0xFFE53935) else if (nearBudget) Color (0xFFFFFF00 ) else Color(0xFF00FF00)

                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {

                                        Spacer(modifier = Modifier.weight(1f - heightFraction))

                                        Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Box(
                                            modifier = Modifier
                                                .width(32.dp)
                                                .weight(heightFraction)
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                .background(barColor)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(week, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // --- YOUR CUSTOM DIALOG DATE PICKER ---
    if (showDatePicker) {
        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                // Get absolute current date info for future lockout logic
                val currentYear = java.time.LocalDate.now().year
                val currentMonthValue = java.time.LocalDate.now().monthValue

                var pickerMode by remember { mutableIntStateOf(0) } // 0 = Month, 1 = Year
                var tempYear by remember { mutableIntStateOf(currentYear) }
                var tempMonth by remember { mutableStateOf(currentMonthStr) }

                Column(
                    modifier = Modifier.animateContentSize().padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- 1. THE SEGMENTED TOGGLE (Month / Year) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                            .padding(4.dp)
                    ) {
                        val modes = listOf("Month", "Year")
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
                            // --- 2. MONTH MODE ---
                            0 -> { Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                                                            shape = RoundedCornerShape(12.dp)
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

                            // --- 3. YEAR MODE ---
                            1 -> {
                                val years = ((currentYear - 11)..currentYear).toList()
                                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    years.chunked(3).forEach { rowYears ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            rowYears.forEach { year ->
                                                val isSelected = tempYear == year
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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

                    // --- 4. BOTTOM ACTIONS ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val exactButtonWidth = 80.dp
                        Box(
                            modifier = Modifier
                                .width(exactButtonWidth)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    // Resets to the exact current month and year
                                    selectedTimeframe = "$currentMonthStr $currentYearNum"
                                    showDatePicker = false
                                }
                                .padding(vertical = 10.dp),
                            Alignment.Center
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // The Apply Button
                       Button(
                            onClick = {
                                if (pickerMode == 0) {
                                    selectedTimeframe = "$tempMonth $tempYear"
                                } else {
                                    selectedTimeframe = "$tempYear"
                                }
                                showDatePicker = false
                            },
                            modifier = Modifier.width(exactButtonWidth),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp) // Shrinked horizontal width */

                        ) {
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
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

//DATABASE TABLE ARCHITECTURE
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val isExpense: Boolean,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis()
)

// THE LIST ITEM UI COMPONENT
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
fun smartInsightCard() {
    // A single, full-width clickable card
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable { /* We will make this open a details sheet later! */ },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. The Alert Icon
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = androidx.compose.ui.graphics.Color(0xFFE57373).copy(alpha = 0.2f),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFFE57373),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 2. The Text (Takes up middle space)
            Column(modifier = Modifier.weight(1f)) {
                Text("High Burn Rate", fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text("You've spent 80% of your budget.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }

            // 3. The Right Arrow
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.KeyboardArrowRight,
                contentDescription = "More details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

