package com.jsingh.XpenseMate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- 1. THE HIGH-CONTRAST PALETTE ---
val PureBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)

// Subtle elevation colors so cards don't completely vanish against the background
val DarkElevatedSurface = Color(0xFF0A0A0A) // Almost black, for subtle card separation
val LightElevatedSurface = Color(0xFFF2F2F2) // Very light grey, for cards in light mode

// --- 2. THE COLOR SCHEMES ---

// THE AMOLED MINIMALIST (Dark Mode)
private val XpenseMateDarkScheme = darkColorScheme(
    primary = PureWhite,           // Buttons and active icons are Pure White
    onPrimary = PureBlack,         // Text inside buttons is Pure Black

    background = PureBlack,        // Pitch black AMOLED base
    onBackground = PureWhite,      // Main text

    surface = DarkElevatedSurface, // Cards are slightly lighter than background
    onSurface = PureWhite,

    surfaceVariant = Color(0xFF161616), // Secondary elements (Chips, search bars)
    onSurfaceVariant = Color.LightGray  // Secondary text
)

// THE STARK MINIMALIST (Light Mode)
private val XpenseMateLightScheme = lightColorScheme(
    primary = PureBlack,           // Buttons and active icons are Pure Black
    onPrimary = PureWhite,         // Text inside buttons is Pure White

    background = PureWhite,        // Pure white base
    onBackground = PureBlack,      // Main text

    surface = LightElevatedSurface, // Cards are slightly darker than background
    onSurface = PureBlack,

    surfaceVariant = Color(0xFFEBEBEB),
    onSurfaceVariant = Color.DarkGray
)

// --- 3. THE MAIN THEME COMPOSABLE ---
@Composable
fun XpenseMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> XpenseMateDarkScheme
        else -> XpenseMateLightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar matches the theme background
            window.statusBarColor = colorScheme.background.toArgb()
            // Navigation bar (bottom) matches theme background
            window.navigationBarColor = colorScheme.background.toArgb()

            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}