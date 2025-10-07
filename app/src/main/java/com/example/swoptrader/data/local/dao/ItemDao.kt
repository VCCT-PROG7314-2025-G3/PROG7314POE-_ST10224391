package com.example.swoptrader.data.local.dao

import androidx.room.*
import com.example.swoptrader.data.model.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    
    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: String): ItemEntity?
    
    @Query("SELECT * FROM items WHERE id = :itemId")
    fun getItemByIdFlow(itemId: String): Flow<ItemEntity?>
    
    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<ItemEntity>
    
    @Query("SELECT * FROM items")
    fun getAllItemsFlow(): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE ownerId = :ownerId")
    suspend fun getItemsByOwner(ownerId: String): List<ItemEntity>
    
    @Query("SELECT * FROM items WHERE ownerId = :ownerId")
    fun getItemsByOwnerFlow(ownerId: String): Flow<List<ItemEntity>>
    
    @Query("SELECT * FROM items WHERE category = :category")
    suspend fun getItemsByCategory(category: String): List<ItemEntity>
    
    @Query("SELECT * FROM items WHERE isAvailable = 1")
    suspend fun getAvailableItems(): List<ItemEntity>
    
    @Query("SELECT * FROM items WHERE isAvailable = 1")
    fun getAvailableItemsFlow(): Flow<List<ItemEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)
    
    @Update
    suspend fun updateItem(item: ItemEntity)
    
    @Delete
    suspend fun deleteItem(item: ItemEntity)
    
    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)
    
    @Query("DELETE FROM items")
    suspend fun deleteAllItems()
}