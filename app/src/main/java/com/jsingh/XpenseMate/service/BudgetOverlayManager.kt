package com.jsingh.XpenseMate.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jsingh.XpenseMate.ui.MainActivity
import com.jsingh.XpenseMate.utils.AlertSettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object BudgetOverlayManager {

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var postPurchaseView: ComposeView? = null

    // 🚀 Track the auto-dismiss timer so we can cancel it if the user manually closes the overlay
    private var autoDismissJob: Job? = null

    @SuppressLint("InflateParams")
    fun showWarningOverlay(
        context: Context,
        appName: String,
        category: String,
        spentAmount: Double,
        budgetLimit: Double
    ) {
        if (composeView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        composeView = ComposeView(context).apply {
            setContent {
                MaterialTheme {
                    OverlayUI(
                        context = context,
                        category = category,
                        spentAmount = spentAmount,
                        budgetLimit = budgetLimit,
                        onDismiss = { removeOverlay() }
                    )
                }
            }
        }

        val lifecycleOwner = FakeLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        composeView?.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView?.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        composeView?.setViewTreeViewModelStoreOwner(lifecycleOwner)

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 60
        }

        try {
            windowManager?.addView(composeView, params)

            // 🚀 1. Trigger Haptics & Sound
            if (spentAmount >= budgetLimit && AlertSettingsManager.isSoundEnabled(context)) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION") vibrator.vibrate(300)
                }
                val uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                android.media.RingtoneManager.getRingtone(context, uri).play()
            }

            // 🚀 2. Start Auto-Dismiss Timer
            if (AlertSettingsManager.isAutoDismissEnabled(context)) {
                val delayTime = (AlertSettingsManager.getAutoDismissTime(context) * 1000).toLong()
                autoDismissJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(delayTime)
                    removeOverlay()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeOverlay() {
        autoDismissJob?.cancel()
        autoDismissJob = null
        try {
            composeView?.let { windowManager?.removeView(it) }
            composeView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Compact Top Post-Purchase Prompt Overlay ---

    @SuppressLint("InflateParams")
    // 👈 We added "category" here!
    fun showPostPurchasePrompt(context: Context, appName: String, category: String, wentToUpi: Boolean) {
        if (postPurchaseView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        postPurchaseView = ComposeView(context).apply {
            setContent {
                MaterialTheme {
                    PostPurchaseUI(
                        context = context,
                        appName = appName,
                        wentToUpi = wentToUpi,
                        onDismiss = { removePostPurchasePrompt() },
                        onConfirm = {
                            removePostPurchasePrompt()

                            // 🚀 THE MAGIC HAPPENS HERE: We stuff the intent with our pre-fill data!
                            val intent = Intent(context, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                putExtra("SHOW_ADD_TRANSACTION", true)
                                putExtra("PREFILL_TITLE", appName)       // e.g. "Zomato"
                                putExtra("PREFILL_CATEGORY", category)   // e.g. "Food & Dining"
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
            }
        }

        // 🚀 RESTORED THIS MISSING BLOCK!
        val lifecycleOwner = FakeLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        postPurchaseView?.setViewTreeLifecycleOwner(lifecycleOwner)
        postPurchaseView?.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        postPurchaseView?.setViewTreeViewModelStoreOwner(lifecycleOwner)

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 60
        }

        try {
            windowManager?.addView(postPurchaseView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removePostPurchasePrompt() {
        try {
            postPurchaseView?.let { windowManager?.removeView(it) }
            postPurchaseView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private class FakeLifecycleOwner : SavedStateRegistryOwner, ViewModelStoreOwner {
    private var mLifecycleRegistry = LifecycleRegistry(this)
    private var mSavedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = mLifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = mSavedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        mLifecycleRegistry.handleLifecycleEvent(event)
    }
    fun performRestore(savedState: Bundle?) {
        mSavedStateRegistryController.performRestore(savedState)
    }
}

// --- UI COMPONENTS ---

@Composable
fun OverlayUI(
    context: Context,
    category: String,
    spentAmount: Double,
    budgetLimit: Double,
    onDismiss: () -> Unit
) {
    val progress = if (budgetLimit > 0) (spentAmount / budgetLimit).toFloat() else 0f
    val amountLeft = budgetLimit - spentAmount
    val isOverBudget = spentAmount >= budgetLimit
    val isSafe = progress <= 0.8f

    val activeColor = when {
        isOverBudget -> Color(0xFFFF3B30)
        !isSafe -> Color(0xFFFFCC00)
        else -> Color(0xFF34C759)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSafe) Icons.Rounded.Info else Icons.Rounded.Warning,
                        contentDescription = "Status",
                        tint = activeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "₹${spentAmount.toInt()} / ₹${budgetLimit.toInt()}",
                    color = if (isOverBudget) activeColor else Color.LightGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalSegments = 10
                val activeSegments = (progress * totalSegments).toInt().coerceIn(0, totalSegments)

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(totalSegments) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(if (index < activeSegments) activeColor else Color(0xFF2A2A2A))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(activeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (isOverBudget) "OVER ₹${kotlin.math.abs(amountLeft).toInt()}" else "₹${amountLeft.toInt()} LEFT",
                        color = activeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSafe) Color.White else Color(0xFF2A2A2A),
                        contentColor = if (isSafe) Color.Black else Color.LightGray
                    ),
                    shape = CircleShape
                ) {
                    Text("Ignore", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
                Button(
                    onClick = {
                        // 🛑 Tell the Watchdog to ignore this exit and NOT trigger the post-purchase prompt
                        BudgetWatchdogService.clearTracking()

                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(homeIntent)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isSafe) Color.White else Color(0xFF2A2A2A),
                        contentColor = if (isOverBudget) Color(0xFFFF3B30) else if (!isSafe) Color.Black else Color.LightGray
                    ),
                    shape = CircleShape
                ) {
                    Text("Close", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun PostPurchaseUI(
    context: Context,
    appName: String,
    wentToUpi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)), // Pitch Black
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with Icon & Dynamic Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (wentToUpi) Color(0xFF34C759).copy(alpha = 0.2f) else Color(0xFF222222),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = "Expense Icon",
                            tint = if (wentToUpi) Color(0xFF34C759) else Color(0xFFFFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (wentToUpi) "Payment Detected!" else "Did you spend on $appName?",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                    if (wentToUpi) {
                        Text(
                            text = "Log your $appName expense now.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = Color.LightGray
                    ),
                    shape = CircleShape
                ) {
                    Text("Just Browsing", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFFFFF),
                        contentColor = Color.Black
                    ),
                    shape = CircleShape
                ) {
                    Text(if (wentToUpi) "Log Expense" else "Yes, Log it", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}