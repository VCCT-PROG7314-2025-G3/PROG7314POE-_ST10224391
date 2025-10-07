package com.example.swoptrader.data.repository

import com.example.swoptrader.data.local.dao.ItemDao
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.model.ItemCategory
import com.example.swoptrader.data.model.Location
import com.example.swoptrader.data.model.toEntity
import com.example.swoptrader.data.model.toItem
import com.example.swoptrader.data.remote.api.SwopTraderApi
import com.example.swoptrader.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ItemRepository {
    suspend fun getAllItems(): Result<List<Item>>
    suspend fun getItemById(id: String): Result<Item?>
    suspend fun getItemsByCategory(category: ItemCategory): Result<List<Item>>
    suspend fun searchItems(query: String): Result<List<Item>>
    suspend fun getItemsByOwner(ownerId: String): Result<List<Item>>
    suspend fun createItem(item: Item): Result<Item>
    suspend fun updateItem(item: Item): Result<Item>
    suspend fun deleteItem(id: String): Result<Unit>
    fun getAvailableItemsFlow(): Flow<List<Item>>
    fun getItemsByCategoryFlow(category: ItemCategory): Flow<List<Item>>
    fun searchItemsFlow(query: String): Flow<List<Item>>
    fun getItemsByOwnerFlow(ownerId: String): Flow<List<Item>>
    suspend fun getItemByIdSync(id: String): Item?
    suspend fun createItemSync(item: Item): Item
    suspend fun updateItemSync(item: Item): Item
    suspend fun deleteItemSync(id: String)
    suspend fun cleanupOrphanedItems(): Result<Int>
}

class ItemRepositoryImpl @Inject constructor(
    private val api: SwopTraderApi,
    private val itemDao: ItemDao,
    private val syncManager: SyncManager
) : ItemRepository {
    
    private val mockItems = mutableListOf<Item>()
    
    init {
    }
    
    override suspend fun getAllItems(): Result<List<Item>> {
        return try {
            // Try REST API first
            try {
                val response = api.getItems()
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val items = apiResponse.data?.data ?: emptyList()
                        // Cache items locally for offline access
                        cacheItemsLocally(items)
                        return Result.success(items)
                    }
                }
            } catch (apiException: Exception) {
                // API failed, fall back to Firebase
                println("API failed, falling back to Firebase: ${apiException.message}")
            }
            
            // Fallback to Firebase/SyncManager
            initializeSampleDataIfNeeded()
            syncManager.getAllItemsWithSync()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun cacheItemsLocally(items: List<Item>) {
        try {
            val itemEntities = items.map { it.toEntity() }
            itemDao.insertItems(itemEntities)
        } catch (e: Exception) {
            println("Failed to cache items locally: ${e.message}")
        }
    }
    
    private suspend fun initializeSampleDataIfNeeded() {
        val existingItems = itemDao.getAllItems()
        if (existingItems.isEmpty()) {
            val sampleItems = getMockItems()
            val itemEntities = sampleItems.map { it.toEntity() }
            itemDao.insertItems(itemEntities)
        }
    }
    
    override suspend fun getItemById(id: String): Result<Item?> {
        return try {
            // Try REST API first
            try {
                val response = api.getItemById(id)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val item = apiResponse.data
                        if (item != null) {
                            // Cache item locally
                            cacheItemsLocally(listOf(item))
                        }
                        return Result.success(item)
                    }
                }
            } catch (apiException: Exception) {
                // API failed, fall back to local database
                println("API failed for getItemById, falling back to local DB: ${apiException.message}")
            }
            
            // Fallback to local database
            initializeSampleDataIfNeeded()
            val itemEntity = itemDao.getItemById(id)
            val item = itemEntity?.toItem()
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getItemsByCategory(category: ItemCategory): Result<List<Item>> {
        return try {
            val items = mockItems.filter { it.category == category }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchItems(query: String): Result<List<Item>> {
        return try {
            val items = mockItems.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getItemsByOwner(ownerId: String): Result<List<Item>> {
        return try {
            initializeSampleDataIfNeeded()
            
            // Use SyncManager to get items with Firestore sync
            syncManager.getItemsByOwnerWithSync(ownerId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createItem(item: Item): Result<Item> {
        return try {
            val currentUserLocation = Location(
                latitude = -26.2041, // Johannesburg coordinates
                longitude = 28.0473
            )
            
            val distance = if (item.location != null) {
                calculateDistance(
                    currentUserLocation.latitude, currentUserLocation.longitude,
                    item.location.latitude, item.location.longitude
                )
            } else null
            
            val itemWithDistance = item.copy(distance = distance)
            
            // Try REST API first
            try {
                val response = api.createItem(itemWithDistance)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val createdItem = apiResponse.data ?: itemWithDistance
                        // Cache locally
                        cacheItemsLocally(listOf(createdItem))
                        return Result.success(createdItem)
                    }
                }
            } catch (apiException: Exception) {
                // API failed, fall back to Firebase
                println("API failed for createItem, falling back to Firebase: ${apiException.message}")
            }
            
            // Fallback to Firebase/SyncManager
            syncManager.createItemWithSync(itemWithDistance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
    
    override suspend fun updateItem(item: Item): Result<Item> {
        return try {
            // Use SyncManager to update item with Firestore sync
            syncManager.updateItemWithSync(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            // Use SyncManager to delete item with Firestore sync
            syncManager.deleteItemWithSync(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAvailableItemsFlow(): Flow<List<Item>> {
        return itemDao.getAvailableItemsFlow().map { entities ->
            entities.map { it.toItem() }
        }
    }
    
    override fun getItemsByCategoryFlow(category: ItemCategory): Flow<List<Item>> {
        return flow { emit(mockItems.filter { it.category == category }) }
    }
    
    override fun searchItemsFlow(query: String): Flow<List<Item>> {
        return flow { 
            emit(mockItems.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            })
        }
    }
    
    override fun getItemsByOwnerFlow(ownerId: String): Flow<List<Item>> {
        return syncManager.getItemsByOwnerFlowWithSync(ownerId)
    }
    
    override suspend fun getItemByIdSync(id: String): Item? {
        return itemDao.getItemById(id)?.toItem()
    }
    
    override suspend fun createItemSync(item: Item): Item {
        val itemEntity = item.toEntity()
        itemDao.insertItem(itemEntity)
        return item
    }
    
    override suspend fun updateItemSync(item: Item): Item {
        val itemEntity = item.toEntity()
        itemDao.updateItem(itemEntity)
        return item
    }
    
    override suspend fun deleteItemSync(id: String) {
        itemDao.deleteItemById(id)
    }
    
    private fun getMockItems(): List<Item> {
        return listOf(
            Item(
                id = "item_1",
                name = "MacBook Pro 13-inch",
                description = "2022 MacBook Pro in excellent condition. Perfect for work and creative projects. Comes with original charger and box.",
                category = ItemCategory.ELECTRONICS,
                condition = com.example.swoptrader.data.model.ItemCondition.LIKE_NEW,
                images = listOf("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=300&fit=crop"),
                ownerId = "user_1",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Sandton, Johannesburg"
                ),
                desiredTrades = listOf("Gaming PC", "iPad Pro", "Camera Equipment"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 86400000,
                viewCount = 156,
                pitchCount = 12,
                distance = 2.5
            ),
            Item(
                id = "item_2",
                name = "Nike Air Jordan 1 Retro",
                description = "Classic Air Jordan 1 in Chicago colorway. Size 10.5, worn only a few times. Authentic with original box.",
                category = ItemCategory.CLOTHING,
                condition = com.example.swoptrader.data.model.ItemCondition.GOOD,
                images = listOf("https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400&h=300&fit=crop"),
                ownerId = "user_2",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -33.9249,
                    longitude = 18.4241,
                    address = "Cape Town, South Africa"
                ),
                desiredTrades = listOf("Yeezy 350", "Off-White Sneakers", "Supreme Hoodie"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 172800000,
                viewCount = 89,
                pitchCount = 8,
                distance = 15.2
            ),
            Item(
                id = "item_3",
                name = "Vintage Gibson Les Paul",
                description = "Beautiful 1980s Gibson Les Paul electric guitar. Cherry sunburst finish. Recently serviced and plays beautifully.",
                category = ItemCategory.MUSIC,
                condition = com.example.swoptrader.data.model.ItemCondition.GOOD,
                images = listOf("https://images.unsplash.com/photo-1564186763535-ebb21ef5277f?w=400&h=300&fit=crop"),
                ownerId = "user_3",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.1076,
                    longitude = 28.0567,
                    address = "Melrose Arch, Johannesburg"
                ),
                desiredTrades = listOf("Fender Stratocaster", "Drum Kit", "Audio Interface"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 259200000,
                viewCount = 67,
                pitchCount = 5,
                distance = 1.8
            ),
            Item(
                id = "item_4",
                name = "Canon EOS R5 Camera",
                description = "Professional mirrorless camera with 45MP sensor. Includes 24-70mm f/2.8 lens. Perfect for photography enthusiasts.",
                category = ItemCategory.ELECTRONICS,
                condition = com.example.swoptrader.data.model.ItemCondition.LIKE_NEW,
                images = listOf("https://images.unsplash.com/photo-1606983340126-99ab4feaa64a?w=400&h=300&fit=crop"),
                ownerId = "user_4",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.1467,
                    longitude = 28.0433,
                    address = "Rosebank, Johannesburg"
                ),
                desiredTrades = listOf("Sony A7R IV", "MacBook Pro", "DJI Drone"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 345600000,
                viewCount = 134,
                pitchCount = 9,
                distance = 3.2
            ),
            Item(
                id = "item_5",
                name = "Designer Leather Jacket",
                description = "Premium leather jacket from a local designer. Size M, black color. Timeless style that never goes out of fashion.",
                category = ItemCategory.CLOTHING,
                condition = com.example.swoptrader.data.model.ItemCondition.GOOD,
                images = listOf("https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=300&fit=crop"),
                ownerId = "user_5",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -33.8847,
                    longitude = 18.5042,
                    address = "Canal Walk, Cape Town"
                ),
                desiredTrades = listOf("Designer Watch", "Sneakers", "Backpack"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 432000000,
                viewCount = 78,
                pitchCount = 6,
                distance = 12.7
            ),
            Item(
                id = "item_6",
                name = "Vintage Vinyl Collection",
                description = "Collection of 50+ classic rock and jazz vinyl records. Includes Pink Floyd, Led Zeppelin, Miles Davis, and more.",
                category = ItemCategory.MUSIC,
                condition = com.example.swoptrader.data.model.ItemCondition.GOOD,
                images = listOf("https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=300&fit=crop"),
                ownerId = "user_6",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.1167,
                    longitude = 28.0500,
                    address = "Melrose Arch, Johannesburg"
                ),
                desiredTrades = listOf("Turntable", "Guitar", "Audio Equipment"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 518400000,
                viewCount = 92,
                pitchCount = 7,
                distance = 1.5
            ),
            Item(
                id = "item_7",
                name = "Gaming Setup - RTX 3080 PC",
                description = "High-end gaming PC with RTX 3080, Ryzen 7 5800X, 32GB RAM. Includes monitor, keyboard, and mouse. Ready to game!",
                category = ItemCategory.ELECTRONICS,
                condition = com.example.swoptrader.data.model.ItemCondition.LIKE_NEW,
                images = listOf("https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?w=400&h=300&fit=crop"),
                ownerId = "user_7",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -33.9628,
                    longitude = 18.4096,
                    address = "Table Mountain, Cape Town"
                ),
                desiredTrades = listOf("MacBook Pro", "PlayStation 5", "Camera Equipment"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 604800000,
                viewCount = 201,
                pitchCount = 15,
                distance = 18.9
            ),
            Item(
                id = "item_8",
                name = "Artisan Coffee Machine",
                description = "Professional espresso machine perfect for coffee enthusiasts. Includes grinder and all accessories. Makes cafe-quality coffee at home.",
                category = ItemCategory.HOME,
                condition = com.example.swoptrader.data.model.ItemCondition.GOOD,
                images = listOf("https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&h=300&fit=crop"),
                ownerId = "user_8",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.1076,
                    longitude = 28.0567,
                    address = "Sandton City, Johannesburg"
                ),
                desiredTrades = listOf("Kitchen Appliances", "Wine Collection", "Books"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 691200000,
                viewCount = 45,
                pitchCount = 3,
                distance = 2.1
            ),
            Item(
                id = "item_9",
                name = "Vintage Rolex Submariner",
                description = "Classic 1990s Rolex Submariner in excellent condition. Recently serviced by authorized dealer. Includes original box and papers.",
                category = ItemCategory.ACCESSORIES,
                condition = com.example.swoptrader.data.model.ItemCondition.LIKE_NEW,
                images = listOf("https://images.unsplash.com/photo-1523170335258-f5c6a6f1e0b1?w=400&h=300&fit=crop"),
                ownerId = "user_9",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -33.9046,
                    longitude = 18.4201,
                    address = "V&A Waterfront, Cape Town"
                ),
                desiredTrades = listOf("Omega Speedmaster", "Cartier Watch", "Luxury Handbag"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 777600000,
                viewCount = 312,
                pitchCount = 22,
                distance = 16.4
            ),
            Item(
                id = "item_10",
                name = "Mountain Bike - Specialized",
                description = "2021 Specialized Stumpjumper mountain bike. 29-inch wheels, full suspension. Perfect for trail riding and adventures.",
                category = ItemCategory.SPORTS,
                condition = com.example.swoptrader.data.model.ItemCondition.GOOD,
                images = listOf("https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=400&h=300&fit=crop"),
                ownerId = "user_10",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.1467,
                    longitude = 28.0433,
                    address = "Rosebank, Johannesburg"
                ),
                desiredTrades = listOf("Road Bike", "Surfboard", "Camping Gear"),
                isAvailable = true,
                createdAt = System.currentTimeMillis() - 864000000,
                viewCount = 123,
                pitchCount = 8,
                distance = 3.8
            )
        )
    }
    
    override suspend fun cleanupOrphanedItems(): Result<Int> {
        return try {
            // Use SyncManager to clean up orphaned items in Firebase
            syncManager.cleanupOrphanedItems()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}