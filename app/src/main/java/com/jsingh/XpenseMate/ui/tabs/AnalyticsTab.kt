package com.jsingh.XpenseMate.ui.tabs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jsingh.XpenseMate.data.Transaction
import java.text.NumberFormat
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.chunked
import kotlin.collections.forEach
import kotlin.collections.toList

@Composable
fun AnalyticsTab(
    transactions: List<Transaction>,
    categoryBudgets: Map<String, Float>,
    dailyLimit: Float
) {
    val currentLocalDate = LocalDate.now()
    val currentYearNum = currentLocalDate.year
    val currentMonthStr = currentLocalDate.format(DateTimeFormatter.ofPattern("MMM"))

    var selectedTimeframe by remember { mutableStateOf("$currentMonthStr $currentYearNum") }
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // --- THE DYNAMIC MATH ENGINE ---
    val timeframeExpenses =
        transactions.filter { it.isExpense && it.date.contains(selectedTimeframe) }
    val totalSpent = timeframeExpenses.sumOf { it.amount }

    val categoryTotals = timeframeExpenses
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale("en", "IN"))
            .apply { minimumFractionDigits = 0; maximumFractionDigits = 0 }
    }

    val chartColors = listOf(
        Color(0xFFE53935),
        Color(0xFF1E88E5),
        Color(0xFFFFB74D),
        Color(0xFF43A047),
        Color(0xFF8E24AA),
        Color(0xFF00ACC1)
    )

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
            Text("Monthly spending", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.clickable { showDatePicker = true }) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .padding(start = 14.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        selectedTimeframe,
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
                text = "₹ ${formatter.format(totalSpent)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            if (totalSpent > 0) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = Color(0xFFE53935).copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.ArrowUpward,
                            contentDescription = "Up",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- SMART INSIGHT CARD ---
        val topCategory = categoryTotals.firstOrNull()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF81C784).copy(alpha = 0.2f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        Icons.Rounded.Category,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (topCategory != null) {
                        Text(
                            "Top Spend: ${topCategory.first}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "₹${formatter.format(topCategory.second)} spent here.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "No Insights Yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Log expenses to generate data.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        var selectedMode by remember { mutableIntStateOf(0) }

        // --- THE MAIN CARD ---
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier
                .padding(20.dp)
                .animateContentSize()) {

                // 3-Way Toggle
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Heatmap", "Categories", "Weekly").forEachIndexed { index, title ->
                            val isSelected = selectedMode == index
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        selectedMode = index
                                    },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        title,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // --- DYNAMIC CONTENT MODES ---
                when (selectedMode) {

                    // 🔥 MODE 0: CALENDAR HEATMAP (Fixed Budget Logic)
                    0 -> {
                        val dailySpends =
                            timeframeExpenses.groupBy { it.date.take(2).toIntOrNull() ?: 1 }
                                .mapValues { entry -> entry.value.sumOf { it.amount } }

                        val parts = selectedTimeframe.split(" ")
                        val year = parts.getOrNull(1)?.toIntOrNull() ?: currentYearNum
                        val month = Month.values().find { it.name.startsWith(parts[0].uppercase()) }
                            ?: Month.JANUARY
                        val yearMonth = YearMonth.of(year, month)
                        val daysInMonth = yearMonth.lengthOfMonth()

                        val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
                        val calendarSlots =
                            MutableList(firstDayOfWeek - 1) { 0 } + (1..daysInMonth).toList()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                                Text(
                                    day,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            calendarSlots.chunked(7).forEach { week ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (i in 0 until 7) {
                                        if (i < week.size && week[i] != 0) {
                                            val day = week[i]
                                            val spentToday = dailySpends[day] ?: 0.0

                                            // The fixed Daily Limit color logic
                                            val upperBound = dailyLimit * 1.1
                                            val lowerBound = dailyLimit * 0.9

                                            val bgColor = when {
                                                spentToday == 0.0 -> MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.3f
                                                )

                                                spentToday > upperBound -> Color(0xFFE53935).copy(
                                                    alpha = 0.3f
                                                ) // Red (>110%)
                                                spentToday >= lowerBound -> Color(0xFFFFB74D).copy(
                                                    alpha = 0.3f
                                                ) // Yellow (90-110%)
                                                else -> Color(0xFF4CAF50).copy(alpha = 0.3f) // Green (<90%)
                                            }
                                            val textColor = when {
                                                spentToday == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                                spentToday > upperBound -> Color(0xFFE53935)
                                                spentToday >= lowerBound -> Color(0xFFFFB74D)
                                                else -> Color(0xFF4CAF50)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(
                                                        androidx.compose.foundation.shape.RoundedCornerShape(
                                                            8.dp
                                                        )
                                                    )
                                                    .background(bgColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    day.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                            }
                                        } else {
                                            Spacer(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 🔥 MODE 1: HYPER-USEFUL CATEGORIES (Fixed Ribbon)
                    1 -> {
                        val maxCategoryAmount = categoryTotals.maxOfOrNull { it.second } ?: 1.0

                        if (categoryTotals.isEmpty() || totalSpent == 0.0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("No expenses logged.", color = Color.Gray) }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                                // 🔥 THE RESTORED CATEGORY RIBBON
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .clip(
                                            androidx.compose.foundation.shape.RoundedCornerShape(50)
                                        )
                                ) {
                                    // Filter out 0 amounts so Compose doesn't crash on 0 weight!
                                    val validTotals = categoryTotals.filter { it.second > 0 }
                                    validTotals.forEachIndexed { index, (_, amount) ->
                                        val fraction =
                                            (amount / totalSpent).toFloat().coerceIn(0.01f, 1f)
                                        Box(
                                            modifier = Modifier
                                                .weight(fraction)
                                                .fillMaxHeight()
                                                .background(chartColors[index % chartColors.size])
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp)) // Added a breather between ribbon and list

                                categoryTotals.forEachIndexed { index, (category, amount) ->
                                    val limit = categoryBudgets[category]?.toDouble() ?: 0.0
                                    val percentage =
                                        if (totalSpent > 0) ((amount / totalSpent) * 100).toInt() else 0
                                    val isOverBudget = limit > 0 && amount > limit
                                    val barColor =
                                        if (isOverBudget) Color(0xFFE53935) else chartColors[index % chartColors.size]

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Line 1: Name and Percentage
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(chartColors[index % chartColors.size])
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    category,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                "$percentage% of total",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Line 2: The Bar and Exact Amounts
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val fraction =
                                                if (limit > 0) (amount / limit).toFloat() else (amount / maxCategoryAmount).toFloat()

                                            // Progress Bar
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(8.dp)
                                                    .clip(
                                                        androidx.compose.foundation.shape.RoundedCornerShape(
                                                            50
                                                        )
                                                    )
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(
                                                            alpha = 0.5f
                                                        )
                                                    )
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(
                                                            fraction.coerceIn(0.01f, 1f)
                                                        )
                                                        .fillMaxHeight()
                                                        .clip(
                                                            androidx.compose.foundation.shape.RoundedCornerShape(
                                                                50
                                                            )
                                                        )
                                                        .background(barColor)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))

                                            // The Exact Amounts
                                            if (limit > 0) {
                                                Text(
                                                    "₹${formatter.format(amount)} / ₹${
                                                        formatter.format(
                                                            limit
                                                        )
                                                    }",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isOverBudget) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                                                )
                                            } else {
                                                Text(
                                                    "₹${formatter.format(amount)}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 🔥 MODE 2: STOCK GRAPH (Fixed Exact Y-Axis & X-Axis Formats)
                    2 -> {
                        val w1 = timeframeExpenses.filter { it.date.take(2).toIntOrNull() in 1..7 }
                            .sumOf { it.amount }
                        val w2 = timeframeExpenses.filter { it.date.take(2).toIntOrNull() in 8..14 }
                            .sumOf { it.amount }
                        val w3 =
                            timeframeExpenses.filter { it.date.take(2).toIntOrNull() in 15..21 }
                                .sumOf { it.amount }
                        val w4 =
                            timeframeExpenses.filter { (it.date.take(2).toIntOrNull() ?: 0) >= 22 }
                                .sumOf { it.amount }
                        val maxWeek = maxOf(w1, w2, w3, w4, 1.0)

                        val points = listOf(w1, w2, w3, w4)
                        val lineColor = MaterialTheme.colorScheme.primary

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)) {
                            // --- Y-AXIS LABELS (EXACT AMOUNTS, NO 'K') ---
                            Column(
                                modifier = Modifier
                                    .height(160.dp)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    "₹${formatter.format(maxWeek)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "₹${formatter.format(maxWeek / 2)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "₹0",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // --- GRAPH & X-AXIS ---
                            Column(modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier
                                    .height(160.dp)
                                    .fillMaxWidth()) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        val canvasWidth = size.width
                                        val canvasHeight = size.height
                                        val paddingX = canvasWidth / (points.size * 2)

                                        val safeTop = 12f
                                        val safeBottom = canvasHeight - 12f
                                        val safeHeight = safeBottom - safeTop

                                        // Draw Horizontal Background Gridlines
                                        drawLine(
                                            color = Color.Gray.copy(alpha = 0.15f),
                                            start = Offset(0f, safeTop),
                                            end = Offset(canvasWidth, safeTop),
                                            strokeWidth = 2f
                                        )
                                        drawLine(
                                            color = Color.Gray.copy(alpha = 0.15f),
                                            start = Offset(0f, safeTop + (safeHeight / 2)),
                                            end = Offset(canvasWidth, safeTop + (safeHeight / 2)),
                                            strokeWidth = 2f
                                        )
                                        drawLine(
                                            color = Color.Gray.copy(alpha = 0.15f),
                                            start = Offset(0f, safeBottom),
                                            end = Offset(canvasWidth, safeBottom),
                                            strokeWidth = 2f
                                        )

                                        val path = Path()

                                        // Draw the data points
                                        points.forEachIndexed { index, amount ->
                                            val x = paddingX + (index * (canvasWidth / points.size))
                                            val y =
                                                safeBottom - ((amount / maxWeek).toFloat() * safeHeight)

                                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                                            drawCircle(
                                                color = lineColor,
                                                radius = 12f,
                                                center = Offset(x, y)
                                            )
                                            drawCircle(
                                                color = Color.White,
                                                radius = 6f,
                                                center = Offset(x, y)
                                            )
                                        }
                                        drawPath(
                                            path = path,
                                            color = lineColor,
                                            style = Stroke(
                                                width = 6f,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                // X-Axis Labels (EXACT AMOUNTS)
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    points.forEachIndexed { index, amount ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                "W${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                if (amount > 0) "₹${formatter.format(amount)}" else "₹0",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1
                                            )
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
        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val currentYear = LocalDate.now().year
                val currentMonthValue = LocalDate.now().monthValue
                var pickerMode by remember { mutableIntStateOf(0) }
                var tempYear by remember { mutableIntStateOf(currentYear) }

                val initialMonth = selectedTimeframe.split(" ").getOrNull(0) ?: currentMonthStr
                var tempMonth by remember { mutableStateOf(initialMonth) }

                Column(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        listOf("Month", "Year").forEachIndexed { index, mode ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (pickerMode == index) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { pickerMode = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    mode,
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
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        IconButton(
                                            onClick = { tempYear-- },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(
                                                        alpha = 0.5f
                                                    ),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                Icons.Rounded.ChevronLeft,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Text(
                                            tempYear.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(
                                            onClick = { if (tempYear < currentYear) tempYear++ },
                                            enabled = tempYear < currentYear,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    if (tempYear < currentYear) MaterialTheme.colorScheme.surfaceVariant.copy(
                                                        alpha = 0.5f
                                                    ) else Color.Transparent, CircleShape
                                                )
                                        ) {
                                            Icon(
                                                Icons.Rounded.ChevronRight,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (tempYear < currentYear) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.2f
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    val months = listOf(
                                        "Jan",
                                        "Feb",
                                        "Mar",
                                        "Apr",
                                        "May",
                                        "Jun",
                                        "Jul",
                                        "Aug",
                                        "Sep",
                                        "Oct",
                                        "Nov",
                                        "Dec"
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        months.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                rowMonths.forEachIndexed { colIndex, month ->
                                                    val monthNumber = (rowIndex * 3) + colIndex + 1
                                                    val isSelected = tempMonth == month
                                                    val isFuture =
                                                        tempYear == currentYear && monthNumber > currentMonthValue
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else if (isFuture) MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                    alpha = 0.2f
                                                                ) else MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                    alpha = 0.5f
                                                                ),
                                                                androidx.compose.foundation.shape.RoundedCornerShape(
                                                                    12.dp
                                                                )
                                                            )
                                                            .then(if (!isFuture) Modifier.clickable {
                                                                tempMonth = month
                                                            } else Modifier)
                                                            .padding(vertical = 16.dp),
                                                        contentAlignment = Alignment.Center) {
                                                        Text(
                                                            month,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (isFuture) MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.3f
                                                            ) else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                val years = ((currentYear - 11)..currentYear).toList()
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    years.chunked(3).forEach { rowYears ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            rowYears.forEach { year ->
                                                val isSelected = tempYear == year
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                alpha = 0.5f
                                                            ),
                                                            androidx.compose.foundation.shape.RoundedCornerShape(
                                                                12.dp
                                                            )
                                                        )
                                                        .clickable { tempYear = year }
                                                        .padding(vertical = 16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        year.toString(),
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp), horizontalArrangement = Arrangement.End
                    ) {
                        val exactButtonWidth = 80.dp
                        Box(
                            modifier = Modifier
                                .width(exactButtonWidth)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    selectedTimeframe =
                                        "$currentMonthStr $currentYearNum"; showDatePicker = false
                                }
                                .padding(vertical = 10.dp), Alignment.Center
                        ) {
                            Text(
                                "Clear",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                selectedTimeframe =
                                    if (pickerMode == 0) "$tempMonth $tempYear" else "$tempYear"; showDatePicker =
                                false
                            },
                            modifier = Modifier.width(exactButtonWidth),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}