package com.example.swoptrader.data.local.dao

import androidx.room.*
import com.example.swoptrader.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
    
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}