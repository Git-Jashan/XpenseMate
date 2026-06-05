package com.jsingh.XpenseMate.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jsingh.XpenseMate.data.Transaction
import com.jsingh.XpenseMate.ui.components.*

@Composable
fun HistoryTab(
    transactions: List<Transaction>,
    categories: List<String>,                     // 👈 Fixed: Added categories parameter
    onUpdateTransaction: (Transaction) -> Unit
) {

    var showDatePicker by remember { mutableStateOf(false) }
    var dateFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    val displayedTransactions = transactions.filter { txn ->
        val matchesSearch =
            txn.title.contains(searchQuery, ignoreCase = true) || txn.amount.toString()
                .contains(searchQuery)
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
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                        shape = CircleShape
                    )
                    .border(width = 1.dp, color = Color.LightGray, shape = CircleShape)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Search,
                    "Search",
                    tint = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            "Search transactions...",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            IconButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.background(
                    color = if (dateFilter != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
            ) {
                Icon(
                    Icons.Rounded.DateRange,
                    "Calendar",
                    tint = if (dateFilter != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
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
                    onClick = {
                        selectedTransaction = displayedTransactions[index]
                    } // 👈 TRIGGERS THE POPUP
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