package com.jsingh.XpenseMate.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jsingh.XpenseMate.R

@Composable
fun AppBottomBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Column {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.background,
            contentPadding = PaddingValues(0.dp),
        ) {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray
            )

            NavigationBarItem(
                icon = { Icon(Icons.Rounded.Home, "Home") },
                label = { Text("Home", fontWeight = FontWeight.Medium) },
                selected = selectedItem == 0,
                onClick = { onItemSelected(0) },
                colors = navItemColors
            )

            NavigationBarItem(
                icon = { Icon(painter = painterResource(id = R.drawable.history), "Payments", modifier = Modifier.size(24.dp)) },
                label = { Text("History", fontWeight = FontWeight.Medium) },
                selected = selectedItem == 1,
                onClick = { onItemSelected(1) },
                colors = navItemColors
            )

            NavigationBarItem(
                selected = false,
                onClick = { onAddClick() },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(58.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = "Scan UPI QR",
                                modifier = Modifier.size(32.dp)
                            )
                           }
                    }
                },
                colors = navItemColors
            )

            NavigationBarItem(
                icon = { Icon(painter = painterResource(id = R.drawable.analytics), contentDescription = "Analytics", modifier = Modifier.size(24.dp)) },
                label = { Text("Analytics", fontWeight = FontWeight.Medium) },
                selected = selectedItem == 2,
                onClick = { onItemSelected(2) },
                colors = navItemColors
            )

            NavigationBarItem(
                icon = { Icon(Icons.Rounded.AccountCircle, "Accounts") },
                label = { Text("Accounts", fontWeight = FontWeight.Medium) },
                selected = selectedItem == 3,
                onClick = { onItemSelected(3) },
                colors = navItemColors
            )
        }
    }
}
