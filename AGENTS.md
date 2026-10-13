# XpenseMate — Agent Instructions

## Project identity
- **App:** XpenseMate — AI-powered Android expense tracker for Indian UPI users
- **Package:** `com.jsingh.XpenseMate`
- **minSdk:** 26 (Android 8.0) | **targetSdk:** 35 (Android 15)
- **Language:** Kotlin 2.0.21 | **AGP:** 8.5.2 | **Compose BOM:** 2024.09.00
- **Room:** 2.6.1 | **KSP:** 2.0.21-1.0.28

> ⚠️ Version notes: Compose BOM 2024.09.00 is Sept 2024 — do NOT suggest upgrading unless asked.
> AGP 8.5.2 is behind current — same rule. Never change versions unless explicitly asked.

---

## Architecture
- Pattern: **MVVM**, manual DI — **no Hilt**, no Dagger
- Single ViewModel: `HomeViewModel` — all UI state lives here
- ViewModels call Repositories/DAOs directly — **no use cases or interactors layer**
- UI state modelled as **data classes** (not sealed classes)
- Navigation: `Navigation.kt` switches between Login, Permission, and Main screens
- Error handling: Logcat + Firebase Crashlytics. No Result<T> wrappers currently.
- Coroutines: use `viewModelScope` in ViewModel, `lifecycleScope` in Services
- **No callbacks** — Kotlin coroutines and Flow throughout

---

## Package structure

```
com.jsingh.XpenseMate/
├── data/
│   ├── AppDatabase.kt          — Room database, singleton
│   ├── Transaction.kt          — CRITICAL: core data model, never restructure
│   ├── TransactionDao.kt       — Room DAO for transactions
│   ├── Subscription.kt         — Subscription model
│   ├── SubscriptionDao.kt      — Room DAO for subscriptions
│   ├── PendingTransaction.kt   — Holds unsynced transactions
│   └── TransactionInsert.kt    — Insert helper model
│
├── network/
│   └── SupabaseClient.kt       — Supabase initialisation and client singleton
│
├── service/
│   ├── BootReceiver.kt         — Restarts services on device boot
│   ├── ExpenseEngine.kt        — Core engine: deduplication + transaction processing
│   ├── NotificationReceiver.kt — Intercepts UPI payment notifications
│   ├── SmsReceiver.kt          — Intercepts UPI SMS messages
│   └── SyncWorker.kt           — WorkManager worker: syncs Room → Supabase
│
├── ui/
│   ├── components/             — Reusable Compose components
│   ├── sidebar/                — Sidebar navigation UI (budgets, categories)
│   ├── tabs/                   — Tab screens: HomeTab, HistoryTab, AnalyticsTab
│   ├── theme/                  — App theme, colours, typography, design tokens
│   └── MainActivity.kt
│
├── utils/
│   ├── DatabaseHelper.kt
│   ├── Formatter.kt            — Currency, date, number formatting
│   ├── PermissionUtils.kt
│   ├── SettingsManager.kt
│   ├── ThemeManager.kt
│   └── UpiUtils.kt             — UPI-specific parsing helpers
│
└── viewmodel/
    └── HomeViewModel.kt        — Single ViewModel, owns all UI state
```

---

## Data layer

### Room (local, offline-first)
- `AppDatabase` — single Room database, accessed via singleton
- Primary table: `Transaction` — **DO NOT change column names or types without being asked**
- `PendingTransaction` — stores transactions waiting to sync when offline
- Always access Room on background threads via coroutines — **never on main thread**

### Supabase (remote sync)
- Only the `transactions` table is active. Other tables exist but are unused.
- Auth: email/password + Google OAuth via Supabase Auth
- RLS: not configured yet — plan to add in future. Do not generate RLS-specific code unless asked.
- All Supabase calls go through `SupabaseClient.kt` — never initialise Supabase inline
- No Edge Functions currently deployed

### Sync strategy
- `SyncWorker` (WorkManager) syncs `PendingTransaction` records → Supabase
- Offline writes go to Room first, then queue in `PendingTransaction`
- **Known issue:** SyncWorker + SmsReceiver + NotificationReceiver + ExpenseEngine integration
  has uninvestigated bugs — treat this entire pipeline as fragile

---

## Service pipeline (critical — understand before touching)

```
SMS arrives           → SmsReceiver          → ExpenseEngine
Notification arrives  → NotificationReceiver → ExpenseEngine
                                                     ↓
                                           deduplication check
                                                     ↓
                                           write to Room (Transaction)
                                                     ↓
                                           queue in PendingTransaction
                                                     ↓
                                           SyncWorker → Supabase
```

- `ExpenseEngine` is the central processing unit — all transaction creation goes here
- Duplicate detection in `ExpenseEngine` exists but correctness is **unverified**
- `BootReceiver` restarts the listener services after reboot
- Each major UPI app (GPay, PhonePe, Paytm, etc.) has **separate parsing logic**
- Current: Groq API via Cloudflare Worker extracts transaction data from SMS/notification text
- Planned: shift to on-device regex parsing + on-device AI (Gemma) — do not assume this exists yet

---

## API integrations

### Groq (via Cloudflare Worker)
- App sends SMS/notification text → Cloudflare Worker → Groq API → structured transaction data
- Purpose: extract amount, merchant, UPI app from raw message text
- Keys stored in `local.properties` → exposed via `BuildConfig` — never hardcode
- Future plan: replace with on-device regex + Gemma model

### Firebase Crashlytics
- Integrated and active
- Use `FirebaseCrashlytics.getInstance().recordException(e)` for non-fatal errors
- Use `FirebaseCrashlytics.getInstance().log("context")` for breadcrumbs

### Keys — never hardcode
```kotlin
// Always use BuildConfig:
BuildConfig.SUPABASE_URL
BuildConfig.SUPABASE_ANON_KEY
BuildConfig.GROQ_WORKER_URL
```

---

## UI structure

### Navigation flow
```
Login screen → Permission screen → Main screen
```
Handled in `Navigation.kt`. Do not add new top-level destinations without asking.

### Main screen layout
- **Bottom bar:** HomeTab | HistoryTab | AnalyticsTab | ScanQR
- **Sidebar:** Budget tab, Category tab
- ScanQR is functional. Pay Anyone button exists in HomeTab but is **broken — do not attempt to fix unless specifically asked**

### HomeTab layout
1. Daily spending bar
2. Monthly limit indicator
3. Manual entry button
4. Pay Anyone button (**broken, leave alone**)
5. Three sub-tabs: Recent | Recurring | Category

### Feature status
| Feature | Status |
|---|---|
| Home tab | Working (pay anyone broken) |
| History tab | Done |
| Analytics tab | Working — improvement opportunities |
| Scan QR | Working |
| Category management | Working — user-editable |
| Budget management | Working — sidebar tabs |
| UPI SMS/notification parsing | Active, regex migration in progress |
| Offline sync | In progress — has known integration bugs |
| On-device AI (Gemma) | Planned, not built |
| UPI payment (Pay Anyone) | Broken — do not touch |
| UPI deeplink security | Error state — in progress |

---

## Code conventions — follow strictly

### Naming
```kotlin
// ViewModels
class HomeViewModel : ViewModel()

// DAOs
interface TransactionDao

// Repositories (if added)
class TransactionRepository

// Compose screens
@Composable fun HomeTab()
@Composable fun HistoryTab()

// Compose components (reusable)
@Composable fun SpendingCard()
```

### UI patterns
- Always use theme colours from `ui/theme/` — **never hardcode hex colours**
- Follow existing design language — match spacing, typography, component style of current screens
- Use `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*` consistently
- Dark/light theme handled by `ThemeManager` — all new UI must support both

### Data patterns
- `Transaction` data class — **keep all fields intact, never rename or remove a column**
- Room queries return `Flow<List<T>>` for observed data, `suspend fun` for one-shots
- New database columns must have default values for migration compatibility

### Error handling
```kotlin
// Preferred pattern for coroutine error handling:
try {
    // operation
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().recordException(e)
    Log.e("TAG", "description", e)
}
```

### Coroutine scopes
```kotlin
// ViewModel operations:
viewModelScope.launch { }

// Service/Worker operations:
lifecycleScope.launch { }  // or coroutineScope { } inside suspend fun

// NEVER use GlobalScope
// NEVER block main thread with .await() outside coroutine
```

---

## Active development context

### Currently building
- Offline UPI tracking via on-device regex (in `SmsReceiver`, `NotificationReceiver`)
- Investigating SyncWorker + ExpenseEngine integration bugs

### Priority bugs to investigate
1. `SyncWorker` — sync may not trigger correctly after offline period
2. `ExpenseEngine` — duplicate detection logic unverified, possible silent drops
3. `SmsReceiver` + `NotificationReceiver` — may not both fire correctly together
4. Services may not restart properly after force-stop (beyond BootReceiver scope)

### Do not touch without being explicitly asked
- `Transaction.kt` field definitions
- `Navigation.kt` routing logic
- Pay Anyone feature
- Supabase table schema
- Any version numbers in `libs.versions.toml`
- Cloudflare Worker URL or Groq integration (shifting to regex soon)

---

## What to always do
- Add KDoc comments explaining what each function does and why
- When writing new Room queries, add the corresponding DAO function AND update any relevant ViewModel collectors
- When adding a new Compose screen, follow the naming and structure of existing tabs in `ui/tabs/`
- When touching the service pipeline, document the data flow in comments
- Respect the single-ViewModel architecture — do not add new ViewModels without asking
- All string literals go in `strings.xml` — never hardcode user-facing text

## What to never do
- Never use callbacks — use coroutines and Flow
- Never access Room or Supabase on the main thread
- Never hardcode API keys, URLs, or credentials
- Never change the `Transaction` data class structure without being asked
- Never add Hilt, Dagger, or other DI frameworks
- Never introduce a use-case/interactor layer unless asked
- Never suggest downgrading minSdk below 26
- Never generate UI that ignores the existing theme system