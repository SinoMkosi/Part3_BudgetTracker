package com.budgettracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId"), Index("categoryId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int?,
    val date: String,          // stored as "yyyy-MM-dd"
    val startTime: String,     // stored as "HH:mm"
    val endTime: String,       // stored as "HH:mm"
    val description: String,
    val amount: Double,
    val photoPath: String? = null
)
