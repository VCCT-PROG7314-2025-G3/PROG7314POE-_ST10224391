package com.example.swoptrader.data.model

// import androidx.room.Entity
// import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// @Entity(tableName = "trade_history")
data class TradeHistory(
    // @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("offerId")
    val offerId: String,
    
    @SerializedName("participantIds")
    val participantIds: List<String>,
    
    @SerializedName("itemsTraded")
    val itemsTraded: List<TradedItem>,
    
    @SerializedName("completedAt")
    val completedAt: Long,
    
    @SerializedName("meetupId")
    val meetupId: String? = null,
    
    @SerializedName("rating")
    val rating: TradeRating? = null,
    
    @SerializedName("carbonSaved")
    val carbonSaved: Double = 0.0,
    
    @SerializedName("tradeScoreEarned")
    val tradeScoreEarned: Int = 0
)

data class TradedItem(
    @SerializedName("itemId")
    val itemId: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("itemName")
    val itemName: String,
    
    @SerializedName("itemImage")
    val itemImage: String? = null
)

data class TradeRating(
    @SerializedName("rating")
    val rating: Int, // 1-5 stars
    
    @SerializedName("comment")
    val comment: String? = null,
    
    @SerializedName("ratedBy")
    val ratedBy: String,
    
    @SerializedName("ratedAt")
    val ratedAt: Long = System.currentTimeMillis()
)

data class TradeStats(
    @SerializedName("totalTrades")
    val totalTrades: Int = 0,
    
    @SerializedName("successfulTrades")
    val successfulTrades: Int = 0,
    
    @SerializedName("averageRating")
    val averageRating: Double = 0.0,
    
    @SerializedName("totalCarbonSaved")
    val totalCarbonSaved: Double = 0.0,
    
    @SerializedName("currentLevel")
    val currentLevel: Int = 1,
    
    @SerializedName("pointsToNextLevel")
    val pointsToNextLevel: Int = 100
)