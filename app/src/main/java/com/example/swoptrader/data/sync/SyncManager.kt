package com.example.swoptrader.data.sync

import android.util.Log
import com.example.swoptrader.data.local.dao.ItemDao
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.model.toEntity
import com.example.swoptrader.data.model.toItem
import com.example.swoptrader.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val itemDao: ItemDao
) {
    private val TAG = "SyncManager"
    
    // Sync items from Firestore to local database
    suspend fun syncItemsFromFirestore(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting sync from Firestore to local database")
            
            val result = firestoreRepository.getAllItems()
            result.fold(
                onSuccess = { items ->
                    Log.d(TAG, "Retrieved ${items.size} items from Firestore")
                    
                    // Convert to entities and save to local database
                    val entities = items.map { it.toEntity() }
                    itemDao.insertItems(entities)
                    
                    Log.d(TAG, "Successfully synced ${items.size} items to local database")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync items from Firestore: ${error.message}", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during sync from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Sync items from local database to Firestore
    suspend fun syncItemsToFirestore(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting sync from local database to Firestore")
            
            val localItems = itemDao.getAllItems()
            Log.d(TAG, "Found ${localItems.size} items in local database")
            
            var successCount = 0
            var failureCount = 0
            
            for (itemEntity in localItems) {
                val item = itemEntity.toItem()
                val result = firestoreRepository.saveItem(item)
                
                result.fold(
                    onSuccess = {
                        successCount++
                        Log.d(TAG, "Successfully synced item to Firestore: ${item.id}")
                    },
                    onFailure = { error ->
                        failureCount++
                        Log.e(TAG, "Failed to sync item to Firestore: ${item.id}, error: ${error.message}")
                    }
                )
            }
            
            Log.d(TAG, "Sync to Firestore completed: $successCount successful, $failureCount failed")
            
            if (failureCount == 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("$failureCount items failed to sync to Firestore"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during sync to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Create item with sync to both local and Firestore
    suspend fun createItemWithSync(item: Item): Result<Item> {
        return try {
            Log.d(TAG, "Creating item with sync: ${item.id}")
            
            // First save to local database
            val itemEntity = item.toEntity()
            itemDao.insertItem(itemEntity)
            Log.d(TAG, "Item saved to local database: ${item.id}")
            
            // Then sync to Firestore
            val firestoreResult = firestoreRepository.saveItem(item)
            firestoreResult.fold(
                onSuccess = {
                    Log.d(TAG, "Item synced to Firestore successfully: ${item.id}")
                    Result.success(item)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync item to Firestore: ${item.id}, error: ${error.message}")
                    // Item is still saved locally, so we return success but log the Firestore error
                    Result.success(item)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during create item with sync: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Update item with sync to both local and Firestore
    suspend fun updateItemWithSync(item: Item): Result<Item> {
        return try {
            Log.d(TAG, "Updating item with sync: ${item.id}")
            
            // First update local database
            val itemEntity = item.toEntity()
            itemDao.updateItem(itemEntity)
            Log.d(TAG, "Item updated in local database: ${item.id}")
            
            // Then sync to Firestore
            val firestoreResult = firestoreRepository.updateItem(item)
            firestoreResult.fold(
                onSuccess = {
                    Log.d(TAG, "Item synced to Firestore successfully: ${item.id}")
                    Result.success(item)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync item to Firestore: ${item.id}, error: ${error.message}")
                    // Item is still updated locally, so we return success but log the Firestore error
                    Result.success(item)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during update item with sync: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Delete item with sync to both local and Firestore
    suspend fun deleteItemWithSync(itemId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting item with sync: $itemId")
            
            // First delete from local database
            itemDao.deleteItemById(itemId)
            Log.d(TAG, "Item deleted from local database: $itemId")
            
            // Then sync to Firestore
            val firestoreResult = firestoreRepository.deleteItem(itemId)
            firestoreResult.fold(
                onSuccess = {
                    Log.d(TAG, "Item deleted from Firestore successfully: $itemId")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to delete item from Firestore: $itemId, error: ${error.message}")
                    // Item is still deleted locally, so we return success but log the Firestore error
                    Result.success(Unit)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during delete item with sync: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Get items by owner with sync
    suspend fun getItemsByOwnerWithSync(ownerId: String): Result<List<Item>> {
        return try {
            Log.d(TAG, "Getting items by owner with sync: $ownerId")
            
            // First try to get from Firestore (most up-to-date)
            val firestoreResult = firestoreRepository.getItemsByOwner(ownerId)
            firestoreResult.fold(
                onSuccess = { firestoreItems ->
                    Log.d(TAG, "Retrieved ${firestoreItems.size} items from Firestore for owner: $ownerId")
                    
                    // Update local database with Firestore data
                    val entities = firestoreItems.map { it.toEntity() }
                    itemDao.insertItems(entities) // This will update existing items
                    
                    Result.success(firestoreItems)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to get items from Firestore, falling back to local: ${error.message}")
                    
                    // Fallback to local database
                    val localItems = itemDao.getItemsByOwner(ownerId).map { it.toItem() }
                    Log.d(TAG, "Retrieved ${localItems.size} items from local database for owner: $ownerId")
                    Result.success(localItems)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during get items by owner with sync: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Get all items with sync
    suspend fun getAllItemsWithSync(): Result<List<Item>> {
        return try {
            Log.d(TAG, "Getting all items with sync")
            
            // First try to get from Firestore (most up-to-date)
            val firestoreResult = firestoreRepository.getAllItems()
            firestoreResult.fold(
                onSuccess = { firestoreItems ->
                    Log.d(TAG, "Retrieved ${firestoreItems.size} items from Firestore")
                    
                    // Update local database with Firestore data
                    val entities = firestoreItems.map { it.toEntity() }
                    itemDao.insertItems(entities) // This will update existing items
                    
                    Result.success(firestoreItems)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to get items from Firestore, falling back to local: ${error.message}")
                    
                    // Fallback to local database
                    val localItems = itemDao.getAllItems().map { it.toItem() }
                    Log.d(TAG, "Retrieved ${localItems.size} items from local database")
                    Result.success(localItems)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during get all items with sync: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Get items flow with automatic sync
    fun getItemsByOwnerFlowWithSync(ownerId: String): Flow<List<Item>> {
        return flow {
            // First emit local data immediately
            val localItems = itemDao.getItemsByOwner(ownerId).map { it.toItem() }
            emit(localItems)
            
            // Then try to sync from Firestore in the background
            try {
                val firestoreResult = firestoreRepository.getItemsByOwner(ownerId)
                firestoreResult.fold(
                    onSuccess = { firestoreItems ->
                        // Update local database and emit updated data
                        val entities = firestoreItems.map { it.toEntity() }
                        itemDao.insertItems(entities)
                        emit(firestoreItems)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to sync items from Firestore: ${error.message}")
                        // Keep emitting local data
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during background sync: ${e.message}")
            }
        }
    }
    
    // Get all items flow with automatic sync
    fun getAllItemsFlowWithSync(): Flow<List<Item>> {
        return flow {
            // First emit local data immediately
            val localItems = itemDao.getAllItems().map { it.toItem() }
            emit(localItems)
            
            // Then try to sync from Firestore in the background
            try {
                val firestoreResult = firestoreRepository.getAllItems()
                firestoreResult.fold(
                    onSuccess = { firestoreItems ->
                        // Update local database and emit updated data
                        val entities = firestoreItems.map { it.toEntity() }
                        itemDao.insertItems(entities)
                        emit(firestoreItems)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to sync items from Firestore: ${error.message}")
                        // Keep emitting local data
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during background sync: ${e.message}")
            }
        }
    }
    
    // Full sync - sync both ways
    suspend fun fullSync(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting full sync")
            
            // First sync from Firestore to local (get latest data)
            val syncFromResult = syncItemsFromFirestore()
            syncFromResult.fold(
                onSuccess = {
                    Log.d(TAG, "Successfully synced from Firestore to local")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync from Firestore to local: ${error.message}")
                }
            )
            
            // Then sync from local to Firestore (push any local changes)
            val syncToResult = syncItemsToFirestore()
            syncToResult.fold(
                onSuccess = {
                    Log.d(TAG, "Successfully synced from local to Firestore")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync from local to Firestore: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during full sync: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Clean up orphaned items in Firebase
    suspend fun cleanupOrphanedItems(): Result<Int> {
        return try {
            Log.d(TAG, "Starting cleanup of orphaned items")
            val result = firestoreRepository.cleanupOrphanedItems()
            result.fold(
                onSuccess = { deletedCount ->
                    Log.d(TAG, "Successfully cleaned up $deletedCount orphaned items")
                    Result.success(deletedCount)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to cleanup orphaned items: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during cleanup: ${e.message}", e)
            Result.failure(e)
        }
    }
}

