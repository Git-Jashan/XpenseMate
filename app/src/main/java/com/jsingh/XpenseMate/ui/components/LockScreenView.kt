package com.jsingh.XpenseMate.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jsingh.XpenseMate.utils.SecurityManager
import kotlinx.coroutines.delay
import androidx.compose.material.icons.rounded.Add

@Composable
fun LockScreenView(
    onUnlockSuccess: () -> Unit,
    onQuickAddClick: () -> Unit
) {
    val context = LocalContext.current as FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    var showRetryMessage by remember { mutableStateOf(false) }
    var isPromptShowing by remember { mutableStateOf(false) }
    var triggerPromptSignal by remember { mutableStateOf(0) }

    // 🚀 INTERCEPT BACK BUTTON: Kicks intruders out if they swipe back!
    BackHandler {
        context.finish()
    }

    val triggerBiometric = {
        if (!isPromptShowing) {
            isPromptShowing = true
            SecurityManager.authenticate(
                activity = context,
                onSuccess = {
                    isPromptShowing = false
                    onUnlockSuccess()
                },
                onError = {
                    isPromptShowing = false
                    showRetryMessage = true
                }
            )
        }
    }

    // 🚀 FIX: Wait for the app to be fully visible before asking for fingerprint
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                triggerPromptSignal++ // Signal the app is awake!
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 🚀 Safely trigger the prompt 0.2 seconds after the app is visible
    LaunchedEffect(triggerPromptSignal) {
        if (triggerPromptSignal > 0) {
            delay(200)
            triggerBiometric()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp)
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("XpenseMate Locked", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Verify your identity to access your data.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(
                onClick = onQuickAddClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.6f).height(56.dp).padding(bottom = 12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick Log Expense", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { triggerBiometric() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.6f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock App", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (showRetryMessage) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Authentication canceled or failed.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}