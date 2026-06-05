package com.jsingh.XpenseMate.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jsingh.XpenseMate.utils.requestBatteryExemption
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.chunked
import kotlin.collections.forEach
import kotlin.collections.toList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Int, String) -> Unit
) {
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var billingDay by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Entertainment") }

    var frequency by remember { mutableStateOf("Monthly") }
    var expanded by remember { mutableStateOf(false) }
    val frequencyOptions = listOf("Monthly", "Yearly", "Weekly")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "New Subscription",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant (e.g. Netflix)") },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        frequencyOptions.forEach { selectionOption ->
                            DropdownMenuItem(text = {
                                Text(
                                    selectionOption
                                )
                            }, onClick = { frequency = selectionOption; expanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = billingDay,
                    onValueChange = { if (it.length <= 2) billingDay = it },
                    label = { Text("Billing Day (1-31)") },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalAmount = amount.toDoubleOrNull() ?: 0.0
                val finalDay = billingDay.toIntOrNull() ?: 1
                if (merchant.isNotBlank() && finalAmount > 0) onSave(
                    merchant,
                    finalAmount,
                    frequency,
                    finalDay,
                    category
                )
            }, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                Text(
                    "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun PermissionScreen(hasNotifPermission: Boolean, onFinish: () -> Unit) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text("Enable Notifications", fontSize = 16.sp)
                }
            } else {
                // STEP 2 BUTTONS
                Button(
                    onClick = { requestBatteryExemption(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isExpense) expenseColor else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { isExpense = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Expense",
                            color = if (isExpense) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (!isExpense) incomeColor else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { isExpense = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Income",
                            color = if (!isExpense) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 1. Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // 2. Amount Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newText ->
                        amount = newText.filter { char -> char.isDigit() || char == '.' }
                    },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = {
                        Text(
                            "₹ ",
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                )

                // 3. Category Trigger (Looks like a text field, acts like a button)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true, // Prevents the keyboard from popping up
                        label = { Text("Category") },
                        trailingIcon = {
                            Icon(
                                Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null
                            )
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    // Highlights the selected one with Pure White (or Black in Light Mode)
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedCategory = cat
                                            showCategorySheet =
                                                false // Closes automatically when picked!
                                        }
                                ) {
                                    Text(
                                        text = cat,
                                        textAlign = TextAlign.Center,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun historyShowDatePicker(onDateSelected: (String?) -> Unit, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val currentLocalDate = LocalDate.now()
            val currentYear = currentLocalDate.year
            val currentMonthValue = currentLocalDate.monthValue

            var pickerMode by remember { mutableIntStateOf(0) }
            var tempYear by remember { mutableIntStateOf(currentYear) }
            var tempMonth by remember {
                mutableStateOf(
                    currentLocalDate.format(
                        DateTimeFormatter.ofPattern(
                            "MMM"
                        )
                    )
                )
            }
            val datePickerState = rememberDatePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        utcTimeMillis <= System.currentTimeMillis()

                    override fun isSelectableYear(year: Int): Boolean = year <= currentYear
                }
            )

            Column(
                modifier = Modifier
                    .animateContentSize()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. THE TOGGLE ---
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
                    val modes = listOf("Date", "Month", "Year")
                    modes.forEachIndexed { index, mode ->
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
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                // UPDATED PREMIUM ARROWS
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    IconButton(
                                        onClick = { tempYear-- },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Rounded.ChevronLeft,
                                            contentDescription = "Previous Year",
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
                                            contentDescription = "Next Year",
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
                                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                            else if (isFuture) MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                alpha = 0.2f
                                                            )
                                                            else MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                alpha = 0.5f
                                                            ),
                                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                                                16.dp
                                                            ) // Rounder grid
                                                        )
                                                        .then(if (!isFuture) Modifier.clickable {
                                                            tempMonth = month
                                                        } else Modifier)
                                                        .padding(vertical = 16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = month,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                        else if (isFuture) MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.3f
                                                        )
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
                                                            16.dp
                                                        )
                                                    ) // Rounder grid
                                                    .clickable { tempYear = year }
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = year.toString(),
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

                // --- 5. BOTTOM ACTIONS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val exactButtonWidth = 80.dp
                    Box(
                        modifier = Modifier
                            .width(exactButtonWidth)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50)) // Pill shape
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                onDateSelected(null)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        Alignment.Center
                    ) {
                        Text(
                            "Clear",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (pickerMode == 0) {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val formatted =
                                        Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC"))
                                            .toLocalDate()
                                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                                    onDateSelected(formatted)
                                }
                            } else if (pickerMode == 1) {
                                onDateSelected("$tempMonth $tempYear")
                            } else {
                                onDateSelected("$tempYear")
                            }
                            onDismiss()
                        },
                        modifier = Modifier.width(exactButtonWidth),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical = 8.dp
                        ) // Shrinked horizontal width
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
