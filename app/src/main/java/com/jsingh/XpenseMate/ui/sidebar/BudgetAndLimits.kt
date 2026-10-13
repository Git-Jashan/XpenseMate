package com.jsingh.XpenseMate.ui.sidebar

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BudgetSettingsView(
    currentDaily: Float,
    currentMonthly: Float,
    categories: List<String>,
    currentCategoryLimits: Map<String, Float>,
    onBackClick: () -> Unit,
    onCategoriesUpdated: (List<String>) -> Unit,
    onSaveBudgets: (Float, Float, Map<String, Float>) -> Unit
) {
    val context = LocalContext.current
    var dailyInput by remember { mutableStateOf(currentDaily.toInt().toString()) }
    var monthlyInput by remember { mutableStateOf(currentMonthly.toInt().toString()) }
    var searchQuery by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Popup State
    var showAddDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatLimit by remember { mutableStateOf("") }

    // Core default categories that the app relies on
    val defaultCategories = listOf(
        "General",
        "Food & Dining",
        "Shopping",
        "Groceries",
        "Entertainment"
    )

    // Live list & map
    val editableCategories = remember { mutableStateListOf<String>() }
    val workingCategoryLimits = remember { mutableStateMapOf<String, String>() }

    // Automatically syncs and ensures defaults are ALWAYS present
    LaunchedEffect(categories, currentCategoryLimits) {
        editableCategories.clear()

        // Combine ViewModel categories with hardcoded defaults to guarantee they show up
        val combined = (defaultCategories + categories).distinctBy { it.lowercase() }
        editableCategories.addAll(combined)

        combined.forEach { cat ->
            if (!workingCategoryLimits.containsKey(cat)) {
                workingCategoryLimits[cat] = currentCategoryLimits[cat]?.toInt()?.toString() ?: ""
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add New Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = newCatLimit,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) newCatLimit = it
                        },
                        label = { Text("Budget Limit") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        prefix = { Text("₹ ") }
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = newCatName.trim()
                        val trimmedLimit = newCatLimit.trim()

                        if (trimmedName.isBlank() || trimmedLimit.isBlank()) {
                            Toast.makeText(context, "Both name and limit are required!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (editableCategories.any { it.equals(trimmedName, ignoreCase = true) }) {
                            Toast.makeText(context, "Category already exists!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        editableCategories.add(trimmedName)
                        workingCategoryLimits[trimmedName] = trimmedLimit
                        onCategoriesUpdated(editableCategories.toList())

                        Toast.makeText(context, "Added '$trimmedName'", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                        searchQuery = ""
                    }
                ) {
                    Text("Add Budget", fontWeight = FontWeight.Bold)
                }
            },

            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)) {

            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Text(
                        "Budgets & Categories",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // --- SCROLLABLE FORM ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // 1. GLOBAL LIMITS
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Global Limits", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    // Daily Ceiling
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Daily Ceiling", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = dailyInput,
                                onValueChange = { if (it.all { char -> char.isDigit() }) dailyInput = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // Monthly Ceiling
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Ceiling", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = monthlyInput,
                                onValueChange = { if (it.all { char -> char.isDigit() }) monthlyInput = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // 2. CATEGORIES & LIMITS
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Category Budgets", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    // Search + Add Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search categories...", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = Color.Gray) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Button(
                            onClick = {
                                newCatName = searchQuery.trim()
                                newCatLimit = ""
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    // Filter list dynamically
                    val displayedCategories = editableCategories.filter { it.contains(searchQuery, ignoreCase = true) }

                    displayedCategories.forEach { category ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: Icon + Name
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
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
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(category, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }

                                // Right: Budget Input + Conditional Delete Icon
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(40.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("₹ ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            BasicTextField(
                                                value = workingCategoryLimits[category] ?: "",
                                                onValueChange = { if (it.all { char -> char.isDigit() }) workingCategoryLimits[category] = it },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                                modifier = Modifier.fillMaxWidth(),
                                                decorationBox = { innerTextField ->
                                                    if (workingCategoryLimits[category].isNullOrEmpty()) {
                                                        Text("Limit", color = Color.Gray, fontSize = 13.sp)
                                                    }
                                                    innerTextField()
                                                }
                                            )
                                        }
                                    }

                                    // System categories cannot be deleted
                                    val isDefaultCategory = defaultCategories.any { it.equals(category, ignoreCase = true) }

                                    if (!isDefaultCategory) {
                                        IconButton(
                                            onClick = {
                                                editableCategories.remove(category)
                                                workingCategoryLimits.remove(category)
                                                onCategoriesUpdated(editableCategories.toList())
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .padding(start = 4.dp)
                                        ) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    val d = dailyInput.toFloatOrNull() ?: currentDaily
                    val m = monthlyInput.toFloatOrNull() ?: currentMonthly
                    val finalCategoryMap = workingCategoryLimits.filterValues { it.isNotBlank() }.mapValues { it.value.toFloat() }
                    onSaveBudgets(d, m, finalCategoryMap)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 24.dp, top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save Budgets", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}