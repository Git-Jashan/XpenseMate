package com.jsingh.XpenseMate.utils

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object SecurityManager {
    private const val PREF_NAME = "SecurityPrefs"
    private const val KEY_APP_LOCK = "app_lock_enabled"
    private const val KEY_TIMEOUT_MS = "lock_timeout_ms"
    private const val KEY_LAST_BACKGROUND_TIME = "last_background_time"

    fun isAppLockEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_APP_LOCK, false)

    fun setAppLockEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_APP_LOCK, enabled).apply()
    }

    // 🚀 NEW: Timeout Preferences (Default is 0 = Instant)
    fun getLockTimeoutMs(context: Context): Long =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getLong(KEY_TIMEOUT_MS, 0L)

    fun setLockTimeoutMs(context: Context, timeoutMs: Long) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putLong(KEY_TIMEOUT_MS, timeoutMs).apply()
    }

    // 🚀 NEW: Tracking when they left the app
    fun saveBackgroundTime(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putLong(KEY_LAST_BACKGROUND_TIME, System.currentTimeMillis()).apply()
    }

    // 🚀 NEW: Did they exceed their grace period?
    fun shouldLockOnReturn(context: Context): Boolean {
        if (!isAppLockEnabled(context)) return false

        val lastTime = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getLong(KEY_LAST_BACKGROUND_TIME, 0L)
        val timeout = getLockTimeoutMs(context)

        // If they were gone longer than the timeout, LOCK IT.
        return (System.currentTimeMillis() - lastTime) > timeout
    }

    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = androidx.biometric.BiometricManager.from(context)
        val authenticators = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val authenticators = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock XpenseMate")
            .setSubtitle("Confirm your identity to continue")
            .setAllowedAuthenticators(authenticators)
            .build()
        val biometricPrompt = androidx.biometric.BiometricPrompt(activity, executor, object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) { onSuccess() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { onError(errString.toString()) }
        })
        biometricPrompt.authenticate(promptInfo)
    }
}