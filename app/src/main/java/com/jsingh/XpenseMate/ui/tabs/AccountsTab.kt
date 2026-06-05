package com.jsingh.XpenseMate.ui.tabs

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jsingh.XpenseMate.R
import java.text.NumberFormat
import java.util.Locale
import com.jsingh.XpenseMate.ui.components.*

@Composable
fun AccountsTab() {
    val formatter = remember {
        NumberFormat.getNumberInstance(Locale("en", "IN"))
            .apply { minimumFractionDigits = 0; maximumFractionDigits = 0 }
    }
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.75f
                )
            )
        ) {
            // We use a Box here so we can pin the icon to the top right while keeping text centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 22.dp)
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
                                Icon(
                                    Icons.Rounded.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    " Assets",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
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
                                Icon(
                                    Icons.Rounded.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    " Liabilities",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
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
                MoneyGridBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.AccountBalance,
                    title = "Bank Accounts",
                    onClick = { })
                MoneyGridBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Lock,
                    title = "Fixed Deposits",
                    onClick = { })
            }
            // ROW 2: Cash & Cards
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyGridBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.money_acc,
                    title = "Physical Cash",
                    onClick = { })
                MoneyGridBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.CreditCard,
                    title = "Credit Cards",
                    onClick = { })
            }
        }

        // --- 3. INVESTMENTS SECTION (2x3 Squares, Icons + Text Only) ---
        Text("Investments", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ROW 1: Stocks, SIP, Gold
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InvestmentSquareBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.candlestick,
                    title = "Stocks & MFs",
                    onClick = { })
                InvestmentSquareBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.icons8_gold_48__1_,
                    title = "Gold & Metals",
                    onClick = { })
                InvestmentSquareBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.market,
                    title = "Real Estate",
                    onClick = { })
            }
            // ROW 2: PPF, Crypto, Other
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InvestmentSquareBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.paper,
                    title = "EPF & PPF",
                    onClick = { })
                InvestmentSquareBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.virtual_currency,
                    title = "Digital Assets",
                    onClick = { })
                InvestmentSquareBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Category,
                    title = "Other Assets",
                    onClick = { })
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}