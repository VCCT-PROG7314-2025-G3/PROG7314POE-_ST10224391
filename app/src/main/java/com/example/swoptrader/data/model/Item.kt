package com.example.swoptrader.data.model

import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("category")
    val category: ItemCategory,
    
    @SerializedName("condition")
    val condition: ItemCondition,
    
    @SerializedName("images")
    val images: List<String> = emptyList(),
    
    @SerializedName("ownerId")
    val ownerId: String,
    
    @SerializedName("location")
    val location: Location? = null,
    
    @SerializedName("desiredTrades")
    val desiredTrades: List<String> = emptyList(),
    
    @SerializedName("isAvailable")
    val isAvailable: Boolean = true,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("viewCount")
    val viewCount: Int = 0,
    
    @SerializedName("pitchCount")
    val pitchCount: Int = 0,
    
    @SerializedName("commentsCount")
    val commentsCount: Int = 0,
    
    @SerializedName("distance")
    val distance: Double? = null, // Distance in kilometers from user's location
    
    // Add owner field for API responses
    @SerializedName("owner")
    val owner: User? = null
)

enum class ItemCategory(
    @SerializedName("value")
    val value: String,
    @SerializedName("displayName")
    val displayName: String
) {
    ELECTRONICS("electronics", "Electronics"),
    CLOTHING("clothing", "Clothing"),
    BOOKS("books", "Books"),
    FURNITURE("furniture", "Furniture"),
    SPORTS("sports", "Sports & Recreation"),
    TOOLS("tools", "Tools & Hardware"),
    ART("art", "Art & Collectibles"),
    MUSIC("music", "Musical Instruments"),
    GARDEN("garden", "Garden & Outdoor"),
    AUTOMOTIVE("automotive", "Automotive"),
    HOME("home", "Home & Kitchen"),
    ACCESSORIES("accessories", "Accessories"),
    OTHER("other", "Other")
}

enum class ItemCondition(
    @SerializedName("value")
    val value: String,
    @SerializedName("displayName")
    val displayName: String
) {
    NEW("new", "New"),
    LIKE_NEW("like_new", "Like New"),
    GOOD("good", "Good"),
    FAIR("fair", "Fair"),
    POOR("poor", "Poor")
}

// Location class is defined in User.kt