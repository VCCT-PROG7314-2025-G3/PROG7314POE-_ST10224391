package com.example.swoptrader.data.model

import com.google.gson.annotations.SerializedName

data class Offer(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("fromUserId")
    val fromUserId: String,
    
    @SerializedName("toUserId")
    val toUserId: String,
    
    @SerializedName("requestedItemId")
    val requestedItemId: String,
    
    @SerializedName("offeredItemIds")
    val offeredItemIds: List<String>,
    
    @SerializedName("status")
    val status: OfferStatus,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("cashAmount")
    val cashAmount: Double? = null,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("expiresAt")
    val expiresAt: Long? = null,
    
    @SerializedName("meetup")
    val meetup: Meetup? = null,
    
    // Relations - excluded from Room entity
    @SerializedName("fromUser")
    val fromUser: User? = null,
    
    @SerializedName("toUser")
    val toUser: User? = null,
    
    @SerializedName("requestedItem")
    val requestedItem: Item? = null,
    
    @SerializedName("offeredItem")
    val offeredItem: Item? = null
)

enum class OfferStatus(
    @SerializedName("value")
    val value: String,
    @SerializedName("displayName")
    val displayName: String
) {
    PENDING("pending", "Pending"),
    ACCEPTED("accepted", "Accepted"),
    REJECTED("rejected", "Rejected"),
    COUNTERED("countered", "Countered"),
    EXPIRED("expired", "Expired"),
    CANCELLED("cancelled", "Cancelled")
}