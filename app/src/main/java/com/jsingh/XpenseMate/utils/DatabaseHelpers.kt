package com.jsingh.XpenseMate.utils

import android.content.Context
import android.widget.Toast
import com.jsingh.XpenseMate.data.AppDatabase
import com.jsingh.XpenseMate.data.Subscriptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

fun addManualSubscription(
    context: Context,
    merchant: String,
    amount: Double,
    frequency: String,
    billingDay: Int,
    category: String
) {
    CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDatabase(context)
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, billingDay)
        if (billingDay < currentDay) calendar.add(Calendar.MONTH, 1)

        val newSubscription = Subscriptions(
            merchant = merchant,
            amount = amount,
            frequency = frequency.uppercase(),
            billingDay = billingDay,
            nextDueDate = calendar.timeInMillis,
            category = category
        )
        db.subscriptionDao().insertSubscription(newSubscription)
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Subscription saved!", Toast.LENGTH_SHORT).show()
        }
    }
}