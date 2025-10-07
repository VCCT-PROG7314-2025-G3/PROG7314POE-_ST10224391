package com.example.swoptrader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val participantIds: String, // JSON string of list
    val offerId: String?,
    val lastMessageAt: Long,
    val createdAt: Long,
    val isActive: Boolean,
    val unreadCount: String // JSON string of map
)

fun Chat.toEntity(): ChatEntity {
    return ChatEntity(
        id = id,
        participantIds = participantIds.joinToString(","),
        offerId = offerId,
        lastMessageAt = lastMessageAt,
        createdAt = createdAt,
        isActive = isActive,
        unreadCount = unreadCount.entries.joinToString(",") { "${it.key}:${it.value}" }
    )
}

fun ChatEntity.toChat(): Chat {
    return Chat(
        id = id,
        participantIds = participantIds.split(",").filter { it.isNotBlank() },
        offerId = offerId,
        lastMessageAt = lastMessageAt,
        createdAt = createdAt,
        isActive = isActive,
        unreadCount = unreadCount.split(",").associate { 
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1].toInt() else "" to 0
        }
    )
}

