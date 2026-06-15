package com.budgettracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.budgettracker.data.dao.CategoryDao
import com.budgettracker.data.dao.ExpenseDao
import com.budgettracker.data.dao.UserDao
import com.budgettracker.data.entities.Category
import com.budgettracker.data.entities.Expense
import com.budgettracker.data.entities.User

@Database(
    entities = [User::class, Category::class, Expense::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
