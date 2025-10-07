package com.example.swoptrader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: String, // Store as string
    val condition: String, // Store as string
    val images: String, // Store as JSON string
    val ownerId: String,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val locationAddress: String? = null,
    val desiredTrades: String, // Store as JSON string
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val viewCount: Int = 0,
    val pitchCount: Int = 0,
    val distance: Double? = null
)

// Extension functions to convert between Item and ItemEntity
fun Item.toEntity(): ItemEntity {
    return ItemEntity(
        id = id,
        name = name,
        description = description,
        category = category.name,
        condition = condition.name,
        images = com.google.gson.Gson().toJson(images),
        ownerId = ownerId,
        locationLatitude = location?.latitude,
        locationLongitude = location?.longitude,
        locationAddress = location?.address,
        desiredTrades = com.google.gson.Gson().toJson(desiredTrades),
        isAvailable = isAvailable,
        createdAt = createdAt,
        updatedAt = updatedAt,
        viewCount = viewCount,
        pitchCount = pitchCount,
        distance = distance
    )
}

fun ItemEntity.toItem(): Item {
    return Item(
        id = id,
        name = name,
        description = description,
        category = ItemCategory.valueOf(category),
        condition = ItemCondition.valueOf(condition),
        images = com.google.gson.Gson().fromJson(images, Array<String>::class.java).toList(),
        ownerId = ownerId,
        location = if (locationLatitude != null && locationLongitude != null) {
            Location(
                latitude = locationLatitude,
                longitude = locationLongitude,
                address = locationAddress ?: ""
            )
        } else null,
        desiredTrades = com.google.gson.Gson().fromJson(desiredTrades, Array<String>::class.java).toList(),
        isAvailable = isAvailable,
        createdAt = createdAt,
        updatedAt = updatedAt,
        viewCount = viewCount,
        pitchCount = pitchCount,
        distance = distance
    )
}

