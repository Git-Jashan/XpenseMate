package com.jsingh.XpenseMate.ui.tabs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jsingh.XpenseMate.R
import com.jsingh.XpenseMate.data.Subscriptions
import com.jsingh.XpenseMate.data.Transaction
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import com.jsingh.XpenseMate.ui.components.*
import com.jsingh.XpenseMate.utils.getOrdinalSuffix

@Composable
fun HomeTab(
    transactions: List<Transaction>,
    subscriptions: List<Subscriptions>,
    dailyLimit: Double,
    monthlyLimit: Double,
    onAddManualClick: () -> Unit,
    onPayAnyoneClick: () -> Unit,
    isIncognito: Boolean,
    onMarkSubscriptionPaid: (Subscriptions) -> Unit
) {
    val context = LocalContext.current

    val todayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val todayString = LocalDateTime.now().format(todayFormatter)
    val currentMonthString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM yyyy"))

    val dailySpent = transactions.filter { it.isExpense && it.date == todayString }.sumOf { it.amount }
    val monthlySpent = transactions.filter { it.isExpense && it.date.contains(currentMonthString) }.sumOf { it.amount }

    // 🚀 DIVIDE BY ZERO CRASH PREVENTION
    val dailyRatio = if (dailyLimit > 0) (dailySpent / dailyLimit).toFloat().coerceIn(0f, 1f) else 0f
    val monthlyRatio = if (monthlyLimit > 0) (monthlySpent / monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f

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

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale("en", "IN"))
            .apply { minimumFractionDigits = 0; maximumFractionDigits = 0 }
    }
    var selectedHomeTab by remember { mutableIntStateOf(0) }
    var recurringSubTab by remember { mutableStateOf("Pending") }
    var categoryTimeframe by remember { mutableStateOf("This Month") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(all = 20.dp)) {
                Text("Daily Spending", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "₹${formatter.format(dailySpent)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        " / ₹${formatter.format(dailyLimit)}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(6.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = dailyRatio)
                            .height(12.dp)
                            .background(
                                dailyColor,
                                RoundedCornerShape(6.dp)
                            )
                    )
                }
            }
        }

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
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Monthly Spending",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                                style = Stroke(width = 25f, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = monthlyColor,
                                startAngle = -90f,
                                sweepAngle = 360f * monthlyRatio,
                                useCenter = false,
                                style = Stroke(width = 25f, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            "${(monthlyRatio * 100).toInt()}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { onPayAnyoneClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(id = R.drawable.cash_register),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(39.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            "Pay\nAnyone",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onClick = onAddManualClick
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(id = R.drawable.cash_register),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(39.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            "Manual\nEntry",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
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
                val tabs = listOf("Recent", "Categories", "Recurring")
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
                // Safely sort the recent transactions so newest is on top
                val recentTxns = transactions.reversed().take(5)
                if (recentTxns.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp), contentAlignment = Alignment.Center
                    ) {
                        Text("No recent transactions", color = Color.Gray)
                    }
                } else {
                    Column {
                        recentTxns.forEach { txn -> TransactionItem(txn = txn) }
                    }
                }
            }

            1 -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .width(200.dp)
                                .height(32.dp)
                        ) {
                            Row(modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(if (categoryTimeframe == "Today") MaterialTheme.colorScheme.surface.copy(0.5f) else Color.Transparent)
                                        .clickable { categoryTimeframe = "Today" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Today", color = if (categoryTimeframe == "Today") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(if (categoryTimeframe == "This Month") MaterialTheme.colorScheme.surface.copy(0.5f) else Color.Transparent)
                                        .clickable { categoryTimeframe = "This Month" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("This Month", color = if (categoryTimeframe == "This Month") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val categoryTotals = transactions
                        .filter { txn -> txn.isExpense && if (categoryTimeframe == "Today") txn.date == todayString else txn.date.contains(currentMonthString) }
                        .groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                        .toList()
                        .sortedByDescending { it.second }

                    if (categoryTotals.isEmpty()) {
                        val emptyText = if (categoryTimeframe == "Today") "today." else "this month."
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No expenses logged $emptyText", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            categoryTotals.take(5).forEach { (category, amount) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(36.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = category.take(1).uppercase(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(category, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Text("₹${formatter.format(amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }

            2 -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.width(200.dp).height(32.dp)) {
                            Row(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(50)).background(if (recurringSubTab == "Pending") MaterialTheme.colorScheme.surface else Color.Transparent).clickable { recurringSubTab = "Pending" }, contentAlignment = Alignment.Center) {
                                    Text("Pending", color = if (recurringSubTab == "Pending") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(50)).background(if (recurringSubTab == "Upcoming") MaterialTheme.colorScheme.surface else Color.Transparent).clickable { recurringSubTab = "Upcoming" }, contentAlignment = Alignment.Center) {
                                    Text("Upcoming", color = if (recurringSubTab == "Upcoming") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    val endOfToday = calendar.timeInMillis

                    val displayedSubs = subscriptions.filter { sub ->
                        if (recurringSubTab == "Pending") {
                            sub.nextDueDate <= endOfToday
                        } else {
                            sub.nextDueDate > endOfToday
                        }
                    }.sortedBy { it.nextDueDate }

                    if (displayedSubs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                            Text("No $recurringSubTab subscriptions.", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        Column {
                            displayedSubs.forEach { sub ->
                                // 🚀 PERFECTLY WIRED UP
                                HomeSubscriptionItem(
                                    sub = sub,
                                    onMarkPaid = {
                                        onMarkSubscriptionPaid(sub)
                                        Toast.makeText(context, "${sub.merchant} marked as paid!", Toast.LENGTH_SHORT).show()
                                    }
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
fun SubscriptionItem(sub: Subscriptions, onDelete: () -> Unit) {
    val firstLetter = sub.merchant.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val safeFrequency =
        if (sub.frequency.isNotEmpty()) sub.frequency.take(1).uppercase() + sub.frequency.drop(1)
            .lowercase() else "Unknown"
    val subtitleText =
        "$safeFrequency • Due on ${sub.billingDay}${getOrdinalSuffix(sub.billingDay)}"

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        firstLetter,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    sub.merchant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitleText, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "- ₹${String.format("%.2f", sub.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE57373),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun HomeSubscriptionItem(
    sub: Subscriptions,
    onMarkPaid: () -> Unit
) {
    val firstLetter = sub.merchant.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val safeFrequency =
        if (sub.frequency.isNotEmpty()) sub.frequency.take(1).uppercase() + sub.frequency.drop(1).lowercase() else "Unknown"
    val subtitleText = "$safeFrequency • Due on ${sub.billingDay}${getOrdinalSuffix(sub.billingDay)}"

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        firstLetter,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    sub.merchant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitleText, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${String.format("%.2f", sub.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                FilledTonalButton(
                    onClick = onMarkPaid,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Mark Paid",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    }
}