package com.budgettracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgettracker.data.entities.CategoryTotal
import com.budgettracker.data.entities.Expense
import com.budgettracker.data.entities.ExpenseWithCategory

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Transaction
    @Query("""
        SELECT e.*, c.name as categoryName, c.colorHex FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.userId = :userId
          AND e.date >= :startDate
          AND e.date <= :endDate
        ORDER BY e.date DESC, e.startTime DESC
    """)
    fun getExpensesWithCategoryForPeriod(
        userId: Int,
        startDate: String,
        endDate: String
    ): LiveData<List<ExpenseWithCategory>>

    @Query("""
        SELECT e.categoryId, c.name as categoryName, SUM(e.amount) as totalAmount
        FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.userId = :userId
          AND e.date >= :startDate
          AND e.date <= :endDate
        GROUP BY e.categoryId
        ORDER BY totalAmount DESC
    """)
    fun getCategoryTotalsForPeriod(
        userId: Int,
        startDate: String,
        endDate: String
    ): LiveData<List<CategoryTotal>>

    @Query("""
        SELECT SUM(amount) FROM expenses
        WHERE userId = :userId
          AND date >= :startDate
          AND date <= :endDate
    """)
    fun getTotalSpentForPeriod(
        userId: Int,
        startDate: String,
        endDate: String
    ): LiveData<Double?>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Int): Expense?

    @Query("""
SELECT e.*, c.name as categoryName, c.colorHex FROM expenses e
LEFT JOIN categories c ON e.categoryId = c.id
WHERE e.userId = :userId
AND e.date >= :startDate
AND e.date <= :endDate
ORDER BY e.date DESC, e.startTime DESC
""")
    suspend fun getExpensesWithCategoryForPeriodSync(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<ExpenseWithCategory>
}

