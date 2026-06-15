package com.budgettracker.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithCategory(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?
)

data class CategoryTotal(
    val categoryId: Int?,
    val categoryName: String?,
    val totalAmount: Double
)
