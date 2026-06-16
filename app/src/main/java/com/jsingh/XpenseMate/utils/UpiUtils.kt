package com.jsingh.XpenseMate.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri

data class UpiApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable
)

object UpiUtils {

    fun getInstalledUpiApps(context: Context): List<UpiApp> {
        val uri = Uri.parse("upi://pay")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val packageManager = context.packageManager

        val resolveInfoList = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        return resolveInfoList.map { resolveInfo ->
            UpiApp(
                packageName = resolveInfo.activityInfo.packageName,
                appName = resolveInfo.loadLabel(packageManager).toString(),
                icon = resolveInfo.loadIcon(packageManager)
            )
        }.distinctBy { it.packageName }
    }
}