package com.jsingh.XpenseMate.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jsingh.XpenseMate.ui.MainActivity.SidebarScreen


@Composable
fun AppDrawer(
    activeRoute: SidebarScreen?,
    onRouteSelected: (SidebarScreen) -> Unit,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(0.80f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        )
                    )
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
                            Text(
                                "JS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "J. Singh",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "jsingh@gmail.com",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = "Open Account",
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.7f
                        )
                    ) {
                        Text(
                            text = "Free Plan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Upgrade to Pro",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.1f
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                // Group 1
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.AccountBalance,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Manage Banks & Wallets",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.PieChart,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Budgets & Limits",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = {
                        closeDrawer()
                        onRouteSelected(SidebarScreen.BUDGET)
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Label,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Tags & Categories",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = {
                        closeDrawer()
                        onRouteSelected(SidebarScreen.CATEGORY)
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Repeat,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Recurring Payments",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = {
                        closeDrawer()
                        onRouteSelected(SidebarScreen.RECURRING)
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.1f
                    )
                )

                // Group 2
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Palette,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Theme & Design",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = {
                        closeDrawer()
                        onRouteSelected(SidebarScreen.DESIGN)
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Notifications,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Notifications",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.1f
                    )
                )

                // Group 3
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Privacy & Security",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Cloud,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Cloud Backup & Sync",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Download,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Export Data",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.1f
                    )
                )

                // Group 4
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Help,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Contact & Support",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Rounded.Description,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            "Terms & Policy",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        12.dp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- FOOTER: VERSION ---
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.1f
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "XpenseMate v1.0",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}