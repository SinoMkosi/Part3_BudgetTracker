package com.budgettracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgettracker.data.entities.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Int): LiveData<User>

    @Update
    suspend fun updateUser(user: User)
}
