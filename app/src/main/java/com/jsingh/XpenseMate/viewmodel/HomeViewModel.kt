package com.jsingh.XpenseMate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jsingh.XpenseMate.data.Subscriptions
import com.jsingh.XpenseMate.data.SubscriptionsDao
import com.jsingh.XpenseMate.data.Transaction
import com.jsingh.XpenseMate.data.TransactionDao
import com.jsingh.XpenseMate.data.TransactionDto
import com.jsingh.XpenseMate.network.SupabaseNetwork
import com.jsingh.XpenseMate.utils.SettingsManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class HomeViewModel(
    application: Application,
    private val transactionDao: TransactionDao,
    private val subscriptionDao: SubscriptionsDao
) : AndroidViewModel(application) {

    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog = _showAccountDialog.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _dailyLimit = MutableStateFlow(SettingsManager.getBudgets(application).first.toDouble())
    val dailyLimit = _dailyLimit.asStateFlow()

    private val _monthlyLimit = MutableStateFlow(SettingsManager.getBudgets(application).second.toDouble())
    val monthlyLimit = _monthlyLimit.asStateFlow()

    private val _categories = MutableStateFlow(loadInitialCategories(application))
    val categories = _categories.asStateFlow()

    private val _categoryLimits = MutableStateFlow(SettingsManager.getCategoryBudgets(application))
    val categoryLimits = _categoryLimits.asStateFlow()

    val transactions = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val subscriptions = subscriptionDao.getActiveSubscriptions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val supabase = SupabaseNetwork.client

    val currentUserEmail = MutableStateFlow(supabase.auth.currentSessionOrNull()?.user?.email ?: "Loading...")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                supabase.auth.awaitInitialization()

                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    verifyUserAndCache(application)
                }
            } catch (e: Exception) {
                println("Auth Initialization Error: ${e.message}")
            }
        }
    }

    private fun loadInitialCategories(context: Application): List<String> {
        val saved = SettingsManager.getCategories(context).toList()
        if (saved.isEmpty()) {
            val defaultCategories = listOf(
                "Food & Dining",
                "Shopping",
                "Entertainment",
                "Groceries",
                "General"
            )
            SettingsManager.saveCategories(context, defaultCategories.toSet())
            return defaultCategories
        }
        return saved
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun signUpWithEmail(emailInput: String, passwordInput: String) {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                supabase.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed. Try again.")
            }
        }
    }

    fun signInWithEmail(emailInput: String, passwordInput: String) {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                supabase.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Invalid email or password.")
            }
        }
    }

    fun updateAddDialogState(show: Boolean) { _showAddDialog.value = show }

    fun updateAccountDialogState(show: Boolean) { _showAccountDialog.value = show }

    fun saveBudgets(newDaily: Double, newMonthly: Double, newCategoryLimits: Map<String, Float>) {
        val context = getApplication<Application>()
        SettingsManager.saveBudgets(context, newDaily.toFloat(), newMonthly.toFloat())
        SettingsManager.saveCategoryBudgets(context, newCategoryLimits)
        _dailyLimit.value = newDaily
        _monthlyLimit.value = newMonthly
        _categoryLimits.value = newCategoryLimits
    }

    fun saveCategories(newList: List<String>) {
        val context = getApplication<Application>()
        SettingsManager.saveCategories(context, newList.toSet())
        _categories.value = newList
    }

    fun saveTransaction(
        title: String, amount: Double, isExpense: Boolean,
        category: String, date: String, time: String, source: String = "Manual"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newTxn = Transaction(title = title, amount = amount, isExpense = isExpense, category = category, date = date, time = time)
            transactionDao.insertTransaction(newTxn)
            _showAddDialog.value = false

            try {
                val activeUserId = supabase.auth.currentSessionOrNull()?.user?.id
                if (activeUserId != null) {

                    val cloudReceipt = TransactionDto(
                        user_id = activeUserId,
                        merchantName = title,
                        amount = amount,
                        category = category,
                        source = source,
                        isExpense = isExpense
                    )

                    supabase.from("transactions").insert(cloudReceipt)
                    println("Cloud Sync: Successfully UPLOADED transaction to Supabase!")
                } else {
                    println("Cloud Sync Warning: User not logged in. Saved locally only.")
                }
            } catch (e: Exception) {
                println("Cloud Sync UPLOAD Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.updateTransaction(transaction)
        }
    }

    fun addManualSubscription(merchant: String, amount: Double, frequency: String, billingDay: Int, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(Calendar.DAY_OF_MONTH, billingDay)
            if (billingDay < currentDay) calendar.add(Calendar.MONTH, 1)

            val newSub = Subscriptions(
                merchant = merchant, amount = amount, frequency = frequency.uppercase(),
                billingDay = billingDay, nextDueDate = calendar.timeInMillis, category = category
            )
            subscriptionDao.insertSubscription(newSub)
        }
    }

    fun deleteSubscription(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            subscriptionDao.cancelSubscription(id)
        }
    }

    fun verifyUserAndCache(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = supabase.auth.currentSessionOrNull()
                val activeUserId = session?.user?.id ?: return@launch

                currentUserEmail.value = session.user?.email ?: "Unknown User"

                val lastUserId = SettingsManager.getLastUserId(context)

                if (activeUserId == lastUserId) {
                    println("Smart Cache: Welcome back! Local data is safe.")
                } else {
                    println("Smart Cache: New user! Nuking old data...")

                    transactionDao.nukeTransactions()
                    transactionDao.nukePendingTransactions()
                    subscriptionDao.nukeSubscriptions()

                    SettingsManager.saveLastUserId(context, activeUserId)

                    fetchCloudDataAndSaveToRoom(activeUserId)
                }
            } catch (e: Exception) {
                println("Smart Cache Error: ${e.message}")
            }
        }
    }

    private suspend fun fetchCloudDataAndSaveToRoom(userId: String) {
        try {
            println("Cloud Sync: Fetching secure DTOs from Supabase...")

            val cloudDtos = supabase.from("transactions")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<TransactionDto>()

            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())

            cloudDtos.forEach { dto ->

                val rawTimestamp = dto.created_at ?: ""
                var parsedDate = "Unknown Date"
                var parsedTime = ""

                if (rawTimestamp.contains("T")) {
                    val rawDateString = rawTimestamp.substringBefore("T")

                    try {
                        val dateObj = inputFormat.parse(rawDateString)
                        parsedDate = if (dateObj != null) outputFormat.format(dateObj) else rawDateString
                    } catch (e: Exception) {
                        parsedDate = rawDateString
                    }

                    parsedTime = rawTimestamp.substringAfter("T").substringBefore(".")
                }

                val localTransaction = Transaction(
                    title = dto.merchantName ?: "Unknown Merchant",
                    amount = dto.amount ?: 0.0,
                    isExpense = dto.isExpense ?: true,
                    category = dto.category ?: "Uncategorized",
                    date = parsedDate,
                    time = parsedTime
                )
                transactionDao.insertTransaction(localTransaction)
            }

            println("Cloud Sync: Successfully synced ${cloudDtos.size} items to Room.")

        } catch (e: Exception) {
            println("Cloud Sync Error: ${e.message}")
            e.printStackTrace()
        }
    }

    fun logOut(onLogoutComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                supabase.auth.signOut()

                _authState.value = AuthState.Idle
                currentUserEmail.value = ""
                _showAccountDialog.value = false

                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onLogoutComplete()
                }
            } catch (e: Exception) {
                println("Logout Error: ${e.message}")
            }
        }
    }

    // 🚀 PROPERLY PLACED INSIDE THE CLASS!
    fun markSubscriptionPaid(subscription: com.jsingh.XpenseMate.data.Subscriptions) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = java.time.LocalDateTime.now()
            val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
            val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")

            // 1. Automatically log the expense to your history
            saveTransaction(
                title = subscription.merchant,
                amount = subscription.amount,
                isExpense = true,
                category = if (subscription.category.isNotEmpty()) subscription.category else "Subscriptions",
                date = now.format(dateFormatter),
                time = now.format(timeFormatter)
            )

            // 2. Calculate the next due date
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = subscription.nextDueDate

            // Move forward by 1 year or 1 month
            if (subscription.frequency.lowercase(java.util.Locale.getDefault()) == "yearly") {
                calendar.add(java.util.Calendar.YEAR, 1)
            } else {
                calendar.add(java.util.Calendar.MONTH, 1)
            }

            val updatedSub = subscription.copy(nextDueDate = calendar.timeInMillis)

            try {
                subscriptionDao.updateSubscription(updatedSub)
            } catch (e: Exception) {
                subscriptionDao.insertSubscription(updatedSub)
            }
        }
    }
}

class HomeViewModelFactory(
    private val application: Application,
    private val transactionDao: TransactionDao,
    private val subscriptionDao: SubscriptionsDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, transactionDao, subscriptionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}