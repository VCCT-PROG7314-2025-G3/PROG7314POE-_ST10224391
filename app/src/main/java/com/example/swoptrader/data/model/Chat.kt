package com.example.swoptrader.data.model

import com.google.gson.annotations.SerializedName

data class Chat(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("participantIds")
    val participantIds: List<String>,
    
    @SerializedName("offerId")
    val offerId: String? = null,
    
    @SerializedName("itemId")
    val itemId: String? = null,
    
    @SerializedName("itemName")
    val itemName: String? = null,
    
    @SerializedName("otherUser")
    val otherUser: User? = null,
    
    @SerializedName("lastMessageAt")
    val lastMessageAt: Long = System.currentTimeMillis(),
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("unreadCount")
    val unreadCount: Map<String, Int> = emptyMap(),
    
    // Excluded from Room entity
    @SerializedName("lastMessage")
    val lastMessage: ChatMessage? = null
)

data class ChatMessage(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("chatId")
    val chatId: String,
    
    @SerializedName("tradeId")
    val tradeId: String,
    
    @SerializedName("senderId")
    val senderId: String,
    
    @SerializedName("receiverId")
    val receiverId: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("content")
    val content: String = message,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @SerializedName("createdAt")
    val createdAt: Long = timestamp,
    
    @SerializedName("isRead")
    val isRead: Boolean = false,
    
    @SerializedName("type")
    val type: MessageType = MessageType.TEXT
)

enum class MessageType(
    @SerializedName("value")
    val value: String
) {
    TEXT("text"),
    IMAGE("image"),
    OFFER("offer"),
    MEETUP("meetup")
}