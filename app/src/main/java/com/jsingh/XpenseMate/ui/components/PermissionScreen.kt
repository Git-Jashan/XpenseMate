package com.jsingh.XpenseMate.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionScreen(
    hasNotifPermission: Boolean,
    hasBatteryExemption: Boolean,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val allGranted = hasNotifPermission && hasBatteryExemption

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp), // 👈 Drastically reduced horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. Header
            Text(
                text = "Setup XpenseMate",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Two quick steps to automate your tracking.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 2. Notification Card (Compact height, full width)
            PermissionCard(
                emoji = "🛡️",
                title = "Read Payments",
                description = "Auto-log expenses directly from bank SMS.",
                isGranted = hasNotifPermission,
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Battery Card (Compact height, full width)
            PermissionCard(
                emoji = "🔋",
                title = "Stay Awake",
                description = "Keep the app awake to never miss a transaction.",
                isGranted = hasBatteryExemption,
                onClick = { requestBatteryExemption(context) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 4. The Hard Gate Button
            Button(
                onClick = onFinish,
                enabled = allGranted,
                modifier = Modifier.fillMaxWidth().height(56.dp), // Normal button height
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PermissionCard(
    emoji: String,
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), label = ""
    )

    Card(
        modifier = Modifier.fillMaxWidth(), // 👈 Removed the hardcoded height limits entirely
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), // 👈 Tighter padding inside the box
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), lineHeight = 18.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Allow", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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