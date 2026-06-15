# Budget Tracker App — Kotlin / Android

## Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/budgettracker/
│   ├── data/
│   │   ├── entities/        # Room @Entity data classes
│   │   │   ├── User.kt
│   │   │   ├── Category.kt
│   │   │   ├── Expense.kt
│   │   │   └── ExpenseWithCategory.kt   (joined query result + CategoryTotal)
│   │   ├── dao/             # Room @Dao interfaces
│   │   │   ├── UserDao.kt
│   │   │   ├── CategoryDao.kt
│   │   │   └── ExpenseDao.kt
│   │   └── database/
│   │       └── AppDatabase.kt           (singleton Room DB)
│   ├── ui/
│   │   ├── activities/
│   │   │   ├── LoginActivity.kt         # Login + Register
│   │   │   ├── MainActivity.kt          # Dashboard
│   │   │   ├── AddExpenseActivity.kt    # Create expense + photo
│   │   │   ├── AddCategoryActivity.kt   # Create / delete categories
│   │   │   ├── ExpenseListActivity.kt   # List with date-range filter
│   │   │   ├── CategoryReportActivity.kt# Spending totals by category
│   │   │   ├── GoalSettingsActivity.kt  # SeekBar min/max goals
│   │   │   └── ExpenseDetailActivity.kt # Full detail + photo view
│   │   └── adapters/
│   │       ├── ExpenseAdapter.kt
│   │       ├── CategoryAdapter.kt
│   │       └── CategoryReportAdapter.kt
│   └── utils/
│       ├── SessionManager.kt    # SharedPreferences login session
│       └── DateUtils.kt         # Date/time formatting helpers
└── res/
    ├── layout/          # All XML layouts
    ├── values/          # colors, strings, themes
    ├── drawable/        # Vector icons + shape drawables
    └── xml/file_paths.xml   # FileProvider paths for camera
```

## Setup in Android Studio

1. **Open** Android Studio → File → Open → select the `BudgetTracker` folder.
2. Wait for Gradle sync to finish (requires internet for dependency download).
3. **Run** on an emulator (API 24+) or physical device.

### Required SDK
- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 34 (Android 14)
- **Kotlin**: 1.9.10
- **KSP**: 1.9.10-1.0.13 (for Room annotation processing)

### Key Dependencies
| Library | Purpose |
|---|---|
| Room 2.6.1 | Local SQLite database (RoomDB) |
| Glide 4.16 | Loading expense photos |
| Material Components 1.11 | UI components (TextInputLayout, Buttons, Cards) |
| Lifecycle / LiveData 2.7 | Reactive UI updates |
| Coroutines | Background DB operations |

---

## Features Implemented

### ✅ Login / Register
- `LoginActivity` handles both modes.
- Username uniqueness validated before registration.
- Passwords stored in RoomDB (extend with hashing for production use).
- `SessionManager` (SharedPreferences) persists login across app restarts.

### ✅ Categories
- `AddCategoryActivity` — create named categories with a colour.
- `CategoryAdapter` — lists existing categories with delete button.
- Categories are user-scoped (foreign key → `users.id`).

### ✅ Expense Entry
- `AddExpenseActivity` collects:
  - **Date** — `DatePickerDialog`
  - **Start & End time** — `TimePickerDialog` (validates end ≥ start)
  - **Description** — `TextInputEditText` with error handling
  - **Amount** — `NumberDecimal` input with `R ` prefix
  - **Category** — `Spinner` populated from LiveData
  - **Photo** — Camera (`TakePicture`) or Gallery (`GetContent`), stored to external files dir via `FileProvider`

### ✅ Expense List with Date Range
- `ExpenseListActivity` — user selects start/end date via `DatePickerDialog`.
- Shows running total for the period.
- Camera icon indicates entries with a photo.
- Tapping an entry opens `ExpenseDetailActivity` (photo + full details).

### ✅ Category Report
- `CategoryReportActivity` — same date-range selector.
- Shows each category's total + percentage bar (`ProgressBar`).
- Grand total displayed at the top.

### ✅ Monthly Goals with SeekBar
- `GoalSettingsActivity` — dual `SeekBar` controls (0–R10,000 in R50 steps).
- Text inputs allow typing an exact value.
- Min/max saved back to the `users` table via Room.
- Dashboard shows live status: on track / over budget / below minimum.

### ✅ RoomDB Persistence
- Single `AppDatabase` singleton with three tables: `users`, `categories`, `expenses`.
- All queries return `LiveData` for reactive UI.
- Background operations run in coroutine `lifecycleScope`.

---

## Extending the App

### Add password hashing
In `LoginActivity.performRegister`, replace plain text with:
```kotlin
import java.security.MessageDigest
fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
```
Then compare hashes in `UserDao.login`.

### Add expense editing
Pass `expense_id` to `AddExpenseActivity`, pre-populate fields, and call `updateExpense()` instead of `insertExpense()`.

### Add database migrations
When changing entities, increment `version` in `AppDatabase` and add a `Migration` object:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE expenses ADD COLUMN notes TEXT")
    }
}
Room.databaseBuilder(...).addMigrations(MIGRATION_1_2).build()
```
