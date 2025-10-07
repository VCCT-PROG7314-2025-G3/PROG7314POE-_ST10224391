package com.example.swoptrader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val requestedItemId: String,
    val offeredItemIds: String, // JSON string of list
    val status: String, // OfferStatus as string
    val message: String,
    val cashAmount: Double?, // Cash amount for the offer
    val meetup: String?, // JSON string of Meetup object
    val createdAt: Long,
    val updatedAt: Long
)

fun Offer.toEntity(): OfferEntity {
    return OfferEntity(
        id = id,
        fromUserId = fromUserId,
        toUserId = toUserId,
        requestedItemId = requestedItemId,
        offeredItemIds = offeredItemIds.joinToString(","),
        status = status.name,
        message = message ?: "",
        cashAmount = cashAmount,
        meetup = meetup?.let { Gson().toJson(it) },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun OfferEntity.toOffer(): Offer {
    return Offer(
        id = id,
        fromUserId = fromUserId,
        toUserId = toUserId,
        requestedItemId = requestedItemId,
        offeredItemIds = offeredItemIds.split(",").filter { it.isNotBlank() },
        status = OfferStatus.valueOf(status),
        message = message,
        cashAmount = cashAmount,
        meetup = meetup?.let { 
            try {
                Gson().fromJson(it, Meetup::class.java)
            } catch (e: Exception) {
                null
            }
        },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
