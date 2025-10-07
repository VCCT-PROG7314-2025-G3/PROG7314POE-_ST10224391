package com.example.swoptrader.data.model

// import androidx.room.Entity
// import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// @Entity(tableName = "meetups")
data class Meetup(
    // @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("offerId")
    val offerId: String,
    
    @SerializedName("participantIds")
    val participantIds: List<String>,
    
    @SerializedName("location")
    val location: MeetupLocation,
    
    @SerializedName("scheduledAt")
    val scheduledAt: Long,
    
    @SerializedName("meetupType")
    val meetupType: MeetupType,
    
    @SerializedName("status")
    val status: MeetupStatus,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("completedAt")
    val completedAt: Long? = null,
    
    @SerializedName("cancelledAt")
    val cancelledAt: Long? = null,
    
    @SerializedName("cancelledBy")
    val cancelledBy: String? = null,
    
    @SerializedName("cancellationReason")
    val cancellationReason: String? = null
)

data class MeetupLocation(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("type")
    val type: LocationType = LocationType.PUBLIC_PLACE
)

enum class LocationType(
    @SerializedName("value")
    val value: String,
    @SerializedName("displayName")
    val displayName: String
) {
    PUBLIC_PLACE("public_place", "Public Place"),
    SHOPPING_CENTER("shopping_center", "Shopping Center"),
    PARK("park", "Park"),
    CAFE("cafe", "Cafe"),
    LIBRARY("library", "Library"),
    OTHER("other", "Other")
}

enum class MeetupType(
    @SerializedName("value")
    val value: String,
    @SerializedName("displayName")
    val displayName: String
) {
    PICKUP("pickup", "Pickup"),
    DELIVERY("delivery", "Delivery")
}

enum class MeetupStatus(
    @SerializedName("value")
    val value: String,
    @SerializedName("displayName")
    val displayName: String
) {
    SCHEDULED("scheduled", "Scheduled"),
    CONFIRMED("confirmed", "Confirmed"),
    IN_PROGRESS("in_progress", "In Progress"),
    COMPLETED("completed", "Completed"),
    CANCELLED("cancelled", "Cancelled"),
    NO_SHOW("no_show", "No Show")
}