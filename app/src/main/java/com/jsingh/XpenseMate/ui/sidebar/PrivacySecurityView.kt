package com.jsingh.XpenseMate.ui.sidebar

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.jsingh.XpenseMate.utils.SecurityManager

@Composable
fun PrivacySecurityView(
    onBackClick: () -> Unit,
    onWipeData: () -> Unit
) {
    val context = LocalContext.current as FragmentActivity
    val prefs = context.getSharedPreferences("SecurityPrefs", Context.MODE_PRIVATE)

    // States
    var isAppLockEnabled by remember { mutableStateOf(SecurityManager.isAppLockEnabled(context)) }
    var lockTimeoutMs by remember { mutableLongStateOf(SecurityManager.getLockTimeoutMs(context)) }
    var isIncognitoEnabled by remember { mutableStateOf(prefs.getBoolean("incognito_mode", false)) }

    val canAuthenticate = remember { SecurityManager.canAuthenticate(context) }
    var showWipeDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- 1. PREMIUM HEADER ---
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
                        "Privacy & Security",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // --- 2. ACCESS CONTROL (SMART LOCK) ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("App Lock", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column {
                            SecuritySettingRow(
                                icon = Icons.Rounded.Fingerprint,
                                title = "Require Biometrics",
                                subtitle = "Lock app when minimized",
                                isChecked = isAppLockEnabled,
                                onCheckedChange = { wantsEnabled ->
                                    if (!canAuthenticate) {
                                        Toast.makeText(context, "No screen lock setup on this device.", Toast.LENGTH_SHORT).show()
                                        return@SecuritySettingRow
                                    }
                                    if (wantsEnabled) {
                                        SecurityManager.authenticate(
                                            activity = context,
                                            onSuccess = {
                                                isAppLockEnabled = true
                                                SecurityManager.setAppLockEnabled(context, true)
                                            },
                                            onError = { Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show() }
                                        )
                                    } else {
                                        isAppLockEnabled = false
                                        SecurityManager.setAppLockEnabled(context, false)
                                    }
                                }
                            )

                            // 🚀 ANIMATED TIMEOUT SELECTOR
                            AnimatedVisibility(
                                visible = isAppLockEnabled,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 16.dp))
                                    Text("Grace Period (Auto-Lock Timeout)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TimeoutChip(
                                            title = "Instant",
                                            isSelected = lockTimeoutMs == 0L,
                                            onClick = { lockTimeoutMs = 0L; SecurityManager.setLockTimeoutMs(context, 0L) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        TimeoutChip(
                                            title = "1 Min",
                                            isSelected = lockTimeoutMs == 60000L,
                                            onClick = { lockTimeoutMs = 60000L; SecurityManager.setLockTimeoutMs(context, 60000L) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        TimeoutChip(
                                            title = "5 Min",
                                            isSelected = lockTimeoutMs == 300000L,
                                            onClick = { lockTimeoutMs = 300000L; SecurityManager.setLockTimeoutMs(context, 300000L) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 3. DASHBOARD PRIVACY ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dashboard Privacy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        SecuritySettingRow(
                            icon = Icons.Rounded.VisibilityOff,
                            title = "Quick-Reveal Balances",
                            subtitle = "Hide balances by default (tap to show)",
                            isChecked = isIncognitoEnabled,
                            onCheckedChange = {
                                isIncognitoEnabled = it
                                prefs.edit().putBoolean("incognito_mode", it).apply()
                            }
                        )
                    }
                }

                // --- 4. DANGER ZONE ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Danger Zone", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Erase All Data", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("Permanently delete all records", fontSize = 12.sp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            }
                            Button(
                                onClick = { showWipeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Wipe", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // --- WIPE CONFIRMATION DIALOG ---
    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wipe All Data?", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text("This will permanently delete all your transactions, budgets, and subscriptions. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onWipeData()
                        showWipeDialog = false
                        Toast.makeText(context, "Data Wiped (Mock)", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

// --- REUSABLE UI ROW ---
@Composable
fun SecuritySettingRow(icon: ImageVector, title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(42.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// --- CUSTOM SEGMENTED CHIP ---
@Composable
fun TimeoutChip(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}