package com.example.swoptrader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val tradeId: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val type: String, // MessageType as string
    val timestamp: Long,
    val isRead: Boolean = false
)

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        chatId = chatId,
        tradeId = tradeId,
        senderId = senderId,
        receiverId = receiverId,
        message = message,
        type = type.name,
        timestamp = timestamp,
        isRead = isRead
    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        tradeId = tradeId,
        senderId = senderId,
        receiverId = receiverId,
        message = message,
        type = MessageType.valueOf(type),
        timestamp = timestamp,
        isRead = isRead
    )
}

