package com.example.swoptrader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_history")
data class TradeHistoryEntity(
    @PrimaryKey
    val id: String,
    val offerId: String,
    val participantIds: String, // JSON string of list
    val itemsTraded: String, // JSON string of list
    val completedAt: Long,
    val meetupId: String?,
    val rating: String?, // JSON string of TradeRating
    val carbonSaved: Double,
    val tradeScoreEarned: Int
)

fun TradeHistory.toEntity(): TradeHistoryEntity {
    return TradeHistoryEntity(
        id = id,
        offerId = offerId,
        participantIds = participantIds.joinToString(","),
        itemsTraded = itemsTraded.joinToString("|") { "${it.itemId},${it.userId},${it.itemName},${it.itemImage ?: ""}" },
        completedAt = completedAt,
        meetupId = meetupId,
        rating = rating?.let { "${it.rating},${it.comment ?: ""},${it.ratedBy},${it.ratedAt}" },
        carbonSaved = carbonSaved,
        tradeScoreEarned = tradeScoreEarned
    )
}

fun TradeHistoryEntity.toTradeHistory(): TradeHistory {
    return TradeHistory(
        id = id,
        offerId = offerId,
        participantIds = participantIds.split(",").filter { it.isNotBlank() },
        itemsTraded = itemsTraded.split("|").mapNotNull { itemString ->
            if (itemString.isNotBlank()) {
                val parts = itemString.split(",")
                if (parts.size >= 3) {
                    TradedItem(
                        itemId = parts[0],
                        userId = parts[1],
                        itemName = parts[2],
                        itemImage = if (parts.size > 3 && parts[3].isNotBlank()) parts[3] else null
                    )
                } else null
            } else null
        },
        completedAt = completedAt,
        meetupId = meetupId,
        rating = rating?.let { ratingString ->
            if (ratingString.isNotBlank()) {
                val parts = ratingString.split(",")
                if (parts.size >= 3) {
                    TradeRating(
                        rating = parts[0].toIntOrNull() ?: 0,
                        comment = if (parts[1].isNotBlank()) parts[1] else null,
                        ratedBy = parts[2],
                        ratedAt = if (parts.size > 3) parts[3].toLongOrNull() ?: System.currentTimeMillis() else System.currentTimeMillis()
                    )
                } else null
            } else null
        },
        carbonSaved = carbonSaved,
        tradeScoreEarned = tradeScoreEarned
    )
}

