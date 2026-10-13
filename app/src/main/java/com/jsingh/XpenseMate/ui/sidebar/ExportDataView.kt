package com.jsingh.XpenseMate.ui.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jsingh.XpenseMate.data.Transaction
import com.jsingh.XpenseMate.utils.ExportManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataView(
    transactions: List<Transaction>,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isExporting by remember { mutableStateOf(false) }
    var lastExportDate by remember { mutableStateOf(ExportManager.getLastExportDate(context)) }

    // --- STATES ---
    var selectedType by remember { mutableStateOf("All") }
    var selectedTimePeriod by remember { mutableStateOf("All Time") }
    val scrollState = rememberScrollState()

    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTimePeriod, scrollState.maxValue) {
        if (selectedTimePeriod == "Custom") {
           scrollState.scrollTo(scrollState.maxValue)
        }
    }

    val filteredTransactions = remember(transactions, selectedType, selectedTimePeriod, startDateMillis, endDateMillis) {
        transactions.filter { tx ->
            val matchesType = when (selectedType) {
                "Expense" -> tx.isExpense
                "Income" -> !tx.isExpense
                else -> true
            }

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val txTime = try { sdf.parse(tx.date)?.time ?: 0L } catch (e: Exception) { 0L }

            val matchesTime = when (selectedTimePeriod) {
                "This Month" -> {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                    }
                    txTime >= cal.timeInMillis
                }
                "Custom" -> {
                    val start = startDateMillis ?: 0L
                    val end = endDateMillis?.let { it + 86400000L } ?: Long.MAX_VALUE
                    txTime in start..end
                }
                else -> true
            }
            matchesType && matchesTime
        }
    }

    val expenseCount = filteredTransactions.count { it.isExpense }
    val incomeCount = filteredTransactions.count { !it.isExpense }
    val displayDateFormat = SimpleDateFormat("dd MMM yy", Locale.getDefault()) // Shorter format for aesthetic

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Text(
                        "Export Data",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // --- MAIN SCROLLABLE CONTENT ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) // Flawless vertical rhythm
            ) {

                // --- 2. THE SUMMARY METRIC ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Transactions to Export",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${filteredTransactions.size}",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-2).sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Minimal breakdown pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Income ↓ $incomeCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            Box(modifier = Modifier.width(1.dp).height(12.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                            Text("Expenses ↑ $expenseCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                        }
                    }
                }

                // --- 3. CONFIGURATION BLOCK ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(300)), // Smoothly expands for date picker
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Include Section
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Include", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            PremiumSegmentedControl(
                                options = listOf("All", "Expense", "Income"),
                                selectedOption = selectedType,
                                onOptionSelected = { selectedType = it }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

                        // Period Section
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Time Period", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            PremiumSegmentedControl(
                                options = listOf("All Time", "This Month", "Custom"),
                                selectedOption = selectedTimePeriod,
                                onOptionSelected = { selectedTimePeriod = it }
                            )
                        }

                        // Custom Date Row (Beautifully integrated)
                        AnimatedVisibility(
                            visible = selectedTimePeriod == "Custom",
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp), // Slight offset from the toggle above
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SleekDateField(
                                    label = "From",
                                    value = startDateMillis?.let { displayDateFormat.format(Date(it)) } ?: "Start Date",
                                    isActive = startDateMillis != null,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showStartDatePicker = true }
                                )

                                Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))

                                SleekDateField(
                                    label = "To",
                                    value = endDateMillis?.let { displayDateFormat.format(Date(it)) } ?: "End Date",
                                    isActive = endDateMillis != null,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showEndDatePicker = true }
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. BOTTOM ACTION AREA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Subtle history text
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Last exported: $lastExportDate", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = {
                        if (filteredTransactions.isNotEmpty()) {
                            isExporting = true
                            coroutineScope.launch {
                                ExportManager.exportTransactionsToCsv(context, filteredTransactions)
                                lastExportDate = ExportManager.getLastExportDate(context)
                                isExporting = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape, // Fully rounded modern pill button
                    enabled = filteredTransactions.isNotEmpty() && !isExporting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp) // Flat design
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.background, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Text(
                            text = if (filteredTransactions.isEmpty()) "No Records Found" else "Export .CSV",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }

    // --- DATE PICKER DIALOGS ---
    if (showStartDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = { TextButton(onClick = { startDateMillis = state.selectedDateMillis; showStartDatePicker = false }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state, showModeToggle = false) }
    }

    if (showEndDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = { TextButton(onClick = { endDateMillis = state.selectedDateMillis; showEndDatePicker = false }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state, showModeToggle = false) }
    }
}

// 🚀 iOS-Style Premium Segmented Control
@Composable
fun PremiumSegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp), // Slim and elegant
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) // Subtle grey inset
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Removes the messy ripple for a cleaner feel
                        ) { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 🚀 Minimalist Date Field
@Composable
fun SleekDateField(
    label: String,
    value: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp, start = 2.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp),
            color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
            border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}