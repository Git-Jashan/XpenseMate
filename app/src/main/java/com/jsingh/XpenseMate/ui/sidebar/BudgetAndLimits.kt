package com.jsingh.XpenseMate.ui.sidebar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.collections.set
import kotlin.text.isNullOrEmpty

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
    val workingCategoryLimits = remember {
        mutableStateMapOf<String, String>().apply {
            categories.forEach { cat ->
                put(cat, currentCategoryLimits[cat]?.toInt()?.toString() ?: "")
            }
        }
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
                    .padding(bottom = 12.dp),
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                ) {
                    Text(
                        "Budgets & Limits",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // We make the form scrollable so the Save button can float at the bottom
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- GLOBAL LIMITS ---
                Text("Global Limits", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                // 🔥 THE FIX: Changed from a Row to a Column
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // 1. Daily Ceiling Card
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth() // Swapped .weight(1f) for .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Daily Ceiling",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = dailyInput,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() }) dailyInput = it
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // 2. Monthly Ceiling Card
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth() // Swapped .weight(1f) for .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Monthly Ceiling",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = monthlyInput,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() }) monthlyInput = it
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // --- CATEGORY LIMITS ---
                Text("Category Limits", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categories.forEach { category ->
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), // Matches the AMOLED glass look
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            ),
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
                                    Text(
                                        category,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // 🔥 THE FIX: A completely custom, perfectly centered input box
                                Surface(
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    color = Color.Transparent, // Blends perfectly with your card
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(40.dp) // Sleek, tight height that won't stretch the row
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "₹ ",
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        BasicTextField(
                                            value = workingCategoryLimits[category] ?: "",
                                            onValueChange = {
                                                if (it.all { char -> char.isDigit() }) workingCategoryLimits[category] =
                                                    it
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 2.dp),
                                            decorationBox = { innerTextField ->
                                                if (workingCategoryLimits[category].isNullOrEmpty()) {
                                                    Text(
                                                        "No Limit",
                                                        color = Color.Gray,
                                                        fontSize = 14.sp
                                                    )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 24.dp, top = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}