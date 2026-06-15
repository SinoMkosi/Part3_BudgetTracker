package com.budgettracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgettracker.data.entities.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesForUser(userId: Int): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesForUserSync(userId: Int): List<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Int): Category?
}
