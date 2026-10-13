package com.jsingh.XpenseMate.ui.components

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import com.jsingh.XpenseMate.service.BudgetWatchdogService

@Composable
fun PermissionScreen(
    hasNotifPermission: Boolean,
    hasBatteryExemption: Boolean,
    onFinish: () -> Unit
) {
    val context = LocalContext.current

    // Real-time tracking for the new WatchDog permissions
    var hasUsageStats by remember { mutableStateOf(checkUsageStats(context)) }
    var hasOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    val allGranted = hasNotifPermission && hasBatteryExemption && hasUsageStats && hasOverlay

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // 👈 Makes it scrollable for smaller phones
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 1. Header
            Text(
                text = "Setup XpenseMate",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Four quick steps to fully automate your tracking.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Notification Card
            PermissionCard(
                icon = Icons.Rounded.Sms,
                title = "Read Payments",
                description = "Auto-log expenses directly from bank SMS.",
                isGranted = hasNotifPermission,
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Battery Card
            PermissionCard(
                icon = Icons.Rounded.Bolt,
                title = "Stay Awake",
                description = "Keep the app awake to never miss a transaction.",
                isGranted = hasBatteryExemption,
                onClick = { requestBatteryExemption(context) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Usage Access (WatchDog Brain)
            PermissionCard(
                icon = Icons.Rounded.Visibility,
                title = "App Tracking",
                description = "Allows us to detect when you open Zomato, Amazon, etc.",
                isGranted = hasUsageStats,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Overlay Access (WatchDog UI)
            PermissionCard(
                icon = Icons.Rounded.Layers,
                title = "Warning Overlay",
                description = "Allows us to drop the budget warning over your screen.",
                isGranted = hasOverlay,
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🔄 Refresh Text for User UX
            if (!allGranted) {
                TextButton(onClick = {
                    hasUsageStats = checkUsageStats(context)
                    hasOverlay = Settings.canDrawOverlays(context)
                }) {
                    Text("I granted them, refresh status", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. The Hard Gate Button (IGNITION SWITCH)
            Button(
                onClick = {
                    // 🚀 THIS IS THE FIX: It starts the service using your exact class name
                    try {
                        val serviceIntent = Intent(context, com.jsingh.XpenseMate.service.BudgetWatchdogService::class.java)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        android.widget.Toast.makeText(context, "WatchDog Activated! 🐕", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("WatchDog", "Failed to start service", e)
                    }

                    onFinish() // Moves them to the dashboard
                },
                enabled = allGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    contentColor = Color.White,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    text = if (allGranted) "Go to Dashboard" else "Complete steps to continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), label = "container"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else Color.Transparent, label = "border"
    )
    val iconBgColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), label = "iconBg"
    )
    val iconTintColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant, label = "iconTint"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), lineHeight = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isGranted) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(50), // Sleek pill shape
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 72.dp, minHeight = 36.dp)
                ) {
                    Text("Allow", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun requestBatteryExemption(context: Context) {
    val intent = Intent()
    val packageName = context.packageName
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        context.startActivity(intent)
    }
}

// 🕵️ Helper to check for Usage Access
fun checkUsageStats(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}