package com.example.swoptrader.data.repository

/**
 * Firestore Repository Implementation
 * 
 * This class implements the Repository pattern for Firebase Firestore operations in the SwopTrader
 * application. It provides a clean abstraction layer for NoSQL database operations, following
 * modern cloud database design principles and best practices for scalable applications.
 * 
 * Key Database Concepts and Patterns:
 * - NoSQL Document Database Design (MongoDB, 2023)
 * - Repository Pattern (Fowler, 2002)
 * - Cloud Database Integration (Google, 2023)
 * - Data Synchronization Patterns (Kleppmann, 2017)
 * - Offline-First Architecture (Google, 2023)
 * - Real-time Data Updates (Firebase, 2023)
 * 
 * Database Design Principles:
 * - Document-based data modeling
 * - Denormalization for performance
 * - Real-time synchronization
 * - Offline capability with local caching
 * - Scalable query patterns
 * 
 * References:
 * - MongoDB. (2023). NoSQL Database Design Patterns. MongoDB Documentation.
 * - Fowler, M. (2002). Patterns of Enterprise Application Architecture. Addison-Wesley.
 * - Google. (2023). Cloud Firestore Best Practices. Firebase Documentation.
 * - Kleppmann, M. (2017). Designing Data-Intensive Applications. O'Reilly Media.
 * - Google. (2023). Offline-First Mobile Development. Firebase Documentation.
 * - Firebase. (2023). Real-time Database Synchronization. Firebase Documentation.
 */

import android.util.Log
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.model.ItemCategory
import com.example.swoptrader.data.model.ItemCondition
import com.example.swoptrader.data.model.User
import com.example.swoptrader.data.model.Comment
import com.example.swoptrader.data.model.Chat
import com.example.swoptrader.data.model.ChatMessage
import com.example.swoptrader.data.model.Offer
import com.example.swoptrader.data.model.TradeHistory
import com.example.swoptrader.data.model.TradedItem
import com.example.swoptrader.data.model.TradeRating
import com.example.swoptrader.data.model.Meetup
import com.example.swoptrader.data.model.MeetupLocation
import com.example.swoptrader.data.model.LocationType
import com.example.swoptrader.data.model.MeetupType
import com.example.swoptrader.data.model.MeetupStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val TAG = "FirestoreRepository"
    
    // Collection names
    private val ITEMS_COLLECTION = "items"
    private val USERS_COLLECTION = "users"
    private val COMMENTS_COLLECTION = "comments"
    private val CHATS_COLLECTION = "chats"
    private val CHAT_MESSAGES_COLLECTION = "chat_messages"
    private val OFFERS_COLLECTION = "offers"
    private val MEETUPS_COLLECTION = "meetups"
    private val TRADE_HISTORY_COLLECTION = "trade_history"
    
    // Items operations
    suspend fun saveItem(item: Item): Result<Item> {
        return try {
            Log.d(TAG, "Saving item to Firestore: ${item.id}")
            val itemMap = itemToMap(item)
            firestore.collection(ITEMS_COLLECTION)
                .document(item.id)
                .set(itemMap)
                .await()
            
            Log.d(TAG, "Item saved successfully: ${item.id}")
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save item: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getItem(itemId: String): Result<Item?> {
        return try {
            Log.d(TAG, "Getting item from Firestore: $itemId")
            val document = firestore.collection(ITEMS_COLLECTION)
                .document(itemId)
                .get()
                .await()
            
            if (document.exists()) {
                val item = mapToItem(document.data ?: emptyMap())
                Log.d(TAG, "Item retrieved successfully: $itemId")
                Result.success(item)
            } else {
                Log.d(TAG, "Item not found: $itemId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get item: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAllItems(): Result<List<Item>> {
        return try {
            Log.d(TAG, "Getting all items from Firestore")
            val snapshot = firestore.collection(ITEMS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { document ->
                try {
                    mapToItem(document.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse item ${document.id}: ${e.message}")
                    null
                }
            }
            
            Log.d(TAG, "Retrieved ${items.size} items from Firestore")
            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all items: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getItemsByOwner(ownerId: String): Result<List<Item>> {
        return try {
            Log.d(TAG, "Getting items by owner: $ownerId")
            val snapshot = firestore.collection(ITEMS_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { document ->
                try {
                    mapToItem(document.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse item ${document.id}: ${e.message}")
                    null
                }
            }
            
            Log.d(TAG, "Retrieved ${items.size} items for owner: $ownerId")
            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get items by owner: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateItem(item: Item): Result<Item> {
        return try {
            Log.d(TAG, "Updating item in Firestore: ${item.id}")
            val itemMap = itemToMap(item)
            firestore.collection(ITEMS_COLLECTION)
                .document(item.id)
                .update(itemMap)
                .await()
            
            Log.d(TAG, "Item updated successfully: ${item.id}")
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update item: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteItem(itemId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting item from Firestore: $itemId")
            firestore.collection(ITEMS_COLLECTION)
                .document(itemId)
                .delete()
                .await()
            
            Log.d(TAG, "Item deleted successfully: $itemId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete item: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // User operations
    suspend fun saveUser(user: User): Result<User> {
        return try {
            Log.d(TAG, "Saving user to Firestore: ${user.id}")
            val userMap = userToMap(user)
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(userMap)
                .await()
            
            Log.d(TAG, "User saved successfully: ${user.id}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUser(userId: String): Result<User?> {
        return try {
            Log.d(TAG, "Getting user from Firestore: $userId")
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val user = mapToUser(document.data ?: emptyMap())
                Log.d(TAG, "User retrieved successfully: $userId")
                Result.success(user)
            } else {
                Log.d(TAG, "User not found: $userId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            Log.d(TAG, "Getting user by email from Firestore: $email")
            val querySnapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                val user = mapToUser(document.data ?: emptyMap())
                Log.d(TAG, "User retrieved successfully by email: $email")
                Result.success(user)
            } else {
                Log.d(TAG, "User not found by email: $email")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user by email: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Comment operations
    suspend fun saveComment(comment: Comment): Result<Comment> {
        return try {
            Log.d(TAG, "Saving comment to Firestore: ${comment.id}")
            val commentMap = commentToMap(comment)
            firestore.collection(COMMENTS_COLLECTION)
                .document(comment.id)
                .set(commentMap)
                .await()
            
            Log.d(TAG, "Comment saved successfully: ${comment.id}")
            Result.success(comment)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save comment: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCommentsByItem(itemId: String): Result<List<Comment>> {
        return try {
            Log.d(TAG, "Getting comments for item: $itemId")
            val snapshot = firestore.collection(COMMENTS_COLLECTION)
                .whereEqualTo("itemId", itemId)
                .get()
                .await()
            
            val allComments = snapshot.documents.mapNotNull { document ->
                try {
                    mapToComment(document.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse comment ${document.id}: ${e.message}")
                    null
                }
            }
            
            val comments = allComments
                .filter { it.parentCommentId == null }
                .sortedBy { it.createdAt }
            
            Log.d(TAG, "Retrieved ${comments.size} comments for item: $itemId")
            Result.success(comments)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get comments for item: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting comment from Firestore: $commentId")
            firestore.collection(COMMENTS_COLLECTION)
                .document(commentId)
                .delete()
                .await()
            
            Log.d(TAG, "Comment deleted successfully: $commentId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete comment: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Helper functions to convert between data models and Firestore maps
    private fun itemToMap(item: Item): Map<String, Any?> {
        return mapOf(
            "id" to item.id,
            "name" to item.name,
            "description" to item.description,
            "category" to item.category.name,
            "condition" to item.condition.name,
            "images" to item.images,
            "ownerId" to item.ownerId,
            "location" to (item.location?.let { location ->
                mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "address" to (location.address ?: ""),
                    "city" to (location.city ?: ""),
                    "country" to (location.country ?: "")
                )
            } ?: mapOf<String, Any?>()),
            "desiredTrades" to item.desiredTrades,
            "isAvailable" to item.isAvailable,
            "createdAt" to item.createdAt,
            "updatedAt" to item.updatedAt,
            "viewCount" to item.viewCount,
            "pitchCount" to item.pitchCount,
            "distance" to (item.distance ?: 0.0)
        )
    }
    
    private fun mapToItem(data: Map<String, Any>): Item {
        val locationData = data["location"] as? Map<String, Any>
        val location = locationData?.let { loc ->
            com.example.swoptrader.data.model.Location(
                latitude = (loc["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (loc["longitude"] as? Number)?.toDouble() ?: 0.0,
                address = loc["address"] as? String,
                city = loc["city"] as? String,
                country = loc["country"] as? String
            )
        }
        
        return Item(
            id = data["id"] as? String ?: "",
            name = data["name"] as? String ?: "",
            description = data["description"] as? String ?: "",
            category = com.example.swoptrader.data.model.ItemCategory.valueOf(
                data["category"] as? String ?: "OTHER"
            ),
            condition = com.example.swoptrader.data.model.ItemCondition.valueOf(
                data["condition"] as? String ?: "FAIR"
            ),
            images = (data["images"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            ownerId = data["ownerId"] as? String ?: "",
            location = location,
            desiredTrades = (data["desiredTrades"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            isAvailable = data["isAvailable"] as? Boolean ?: true,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            viewCount = (data["viewCount"] as? Number)?.toInt() ?: 0,
            pitchCount = (data["pitchCount"] as? Number)?.toInt() ?: 0,
            distance = (data["distance"] as? Number)?.toDouble()
        )
    }
    
    private fun userToMap(user: User): Map<String, Any?> {
        return mapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "profileImageUrl" to user.profileImageUrl,
            "location" to (user.location?.let { location ->
                mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "address" to (location.address ?: ""),
                    "city" to (location.city ?: ""),
                    "country" to (location.country ?: "")
                )
            } ?: mapOf<String, Any?>()),
            "tradeScore" to user.tradeScore,
            "level" to user.level,
            "carbonSaved" to user.carbonSaved,
            "isVerified" to user.isVerified
        )
    }
    
    private fun mapToUser(data: Map<String, Any>): User {
        val locationData = data["location"] as? Map<String, Any> ?: emptyMap()
        val location = com.example.swoptrader.data.model.Location(
            latitude = (locationData["latitude"] as? Number)?.toDouble() ?: 0.0,
            longitude = (locationData["longitude"] as? Number)?.toDouble() ?: 0.0,
            address = locationData["address"] as? String,
            city = locationData["city"] as? String,
            country = locationData["country"] as? String
        )
        
        return User(
            id = data["id"] as? String ?: "",
            name = data["name"] as? String ?: "",
            email = data["email"] as? String ?: "",
            profileImageUrl = data["profileImageUrl"] as? String ?: "",
            location = location,
            tradeScore = (data["tradeScore"] as? Number)?.toInt() ?: 0,
            level = (data["level"] as? Number)?.toInt() ?: 1,
            carbonSaved = (data["carbonSaved"] as? Number)?.toDouble() ?: 0.0,
            isVerified = data["isVerified"] as? Boolean ?: false
        )
    }
    
    private fun commentToMap(comment: Comment): Map<String, Any?> {
        return mapOf(
            "id" to comment.id,
            "itemId" to comment.itemId,
            "authorId" to comment.authorId,
            "authorName" to comment.authorName,
            "authorProfileImageUrl" to comment.authorProfileImageUrl,
            "content" to comment.content,
            "createdAt" to comment.createdAt,
            "updatedAt" to comment.updatedAt,
            "isEdited" to comment.isEdited,
            "likes" to comment.likes,
            "parentCommentId" to comment.parentCommentId
        )
    }
    
    private fun mapToComment(data: Map<String, Any>): Comment {
        return Comment(
            id = data["id"] as? String ?: "",
            itemId = data["itemId"] as? String ?: "",
            authorId = data["authorId"] as? String ?: "",
            authorName = data["authorName"] as? String ?: "",
            authorProfileImageUrl = data["authorProfileImageUrl"] as? String,
            content = data["content"] as? String ?: "",
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isEdited = data["isEdited"] as? Boolean ?: false,
            likes = (data["likes"] as? Number)?.toInt() ?: 0,
            parentCommentId = data["parentCommentId"] as? String
        )
    }
    
    // Chat operations
    suspend fun saveChat(chat: Chat): Result<Chat> {
        return try {
            Log.d(TAG, "Saving chat to Firestore: ${chat.id}")
            val chatMap = chatToMap(chat)
            firestore.collection(CHATS_COLLECTION)
                .document(chat.id)
                .set(chatMap)
                .await()
            
            Log.d(TAG, "Chat saved successfully: ${chat.id}")
            Result.success(chat)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save chat: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getChat(chatId: String): Result<Chat?> {
        return try {
            Log.d(TAG, "Getting chat from Firestore: $chatId")
            val document = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .get()
                .await()
            
            if (document.exists()) {
                val chat = mapToChat(document.data ?: emptyMap())
                Log.d(TAG, "Chat retrieved successfully: $chatId")
                Result.success(chat)
            } else {
                Log.d(TAG, "Chat not found: $chatId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get chat: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getChatsByUser(userId: String): Result<List<Chat>> {
        return try {
            Log.d(TAG, "Getting chats for user: $userId")
            val querySnapshot = firestore.collection(CHATS_COLLECTION)
                .whereArrayContains("participantIds", userId)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val chats = querySnapshot.documents.mapNotNull { document ->
                try {
                    mapToChat(document.data ?: emptyMap(), document.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse chat document: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Retrieved ${chats.size} chats for user: $userId")
            Result.success(chats)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get chats for user: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Chat message operations
    suspend fun saveChatMessage(message: ChatMessage): Result<ChatMessage> {
        return try {
            Log.d(TAG, "Saving chat message to Firestore: ${message.id}")
            println("FirestoreRepository: Saving message with id: ${message.id}, chatId: ${message.chatId}, content: ${message.message}")
            
            val messageMap = chatMessageToMap(message)
            println("FirestoreRepository: Message map: $messageMap")
            
            firestore.collection(CHAT_MESSAGES_COLLECTION)
                .document(message.id)
                .set(messageMap)
                .await()
            
            Log.d(TAG, "Chat message saved successfully: ${message.id}")
            println("FirestoreRepository: Message saved successfully to Firestore")
            Result.success(message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save chat message: ${e.message}", e)
            println("FirestoreRepository: Failed to save message: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getChatMessages(chatId: String): Result<List<ChatMessage>> {
        return try {
            Log.d(TAG, "Getting chat messages for chat: $chatId")
            println("FirestoreRepository: Getting messages for chatId: $chatId")
            
            val querySnapshot = firestore.collection(CHAT_MESSAGES_COLLECTION)
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            
            println("FirestoreRepository: Found ${querySnapshot.documents.size} documents for chatId: $chatId")
            
            val messages = querySnapshot.documents.mapNotNull { document ->
                try {
                    println("FirestoreRepository: Processing document: ${document.id}, data: ${document.data}")
                    mapToChatMessage(document.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse chat message document: ${document.id}", e)
                    println("FirestoreRepository: Failed to parse document ${document.id}: ${e.message}")
                    null
                }
            }
            
            Log.d(TAG, "Retrieved ${messages.size} messages for chat: $chatId")
            println("FirestoreRepository: Successfully retrieved ${messages.size} messages for chatId: $chatId")
            Result.success(messages)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get chat messages: ${e.message}", e)
            println("FirestoreRepository: Failed to get messages for chatId $chatId: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Helper functions for chat
    private fun chatToMap(chat: Chat): Map<String, Any?> {
        return mapOf(
            "id" to chat.id,
            "participantIds" to chat.participantIds,
            "offerId" to chat.offerId,
            "lastMessageAt" to chat.lastMessageAt,
            "createdAt" to chat.createdAt,
            "isActive" to chat.isActive,
            "unreadCount" to chat.unreadCount
        )
    }
    
    private fun mapToChat(data: Map<String, Any>): Chat {
        return Chat(
            id = data["id"] as? String ?: "",
            participantIds = (data["participantIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            offerId = data["offerId"] as? String,
            lastMessageAt = (data["lastMessageAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isActive = data["isActive"] as? Boolean ?: true,
            unreadCount = (data["unreadCount"] as? Map<String, Any>)?.mapValues { 
                (it.value as? Number)?.toInt() ?: 0 
            } ?: emptyMap()
        )
    }
    
    private fun chatMessageToMap(message: ChatMessage): Map<String, Any?> {
        return mapOf(
            "id" to message.id,
            "chatId" to message.chatId,
            "tradeId" to message.tradeId,
            "senderId" to message.senderId,
            "receiverId" to message.receiverId,
            "message" to message.message,
            "type" to message.type.name,
            "timestamp" to message.timestamp,
            "isRead" to message.isRead
        )
    }
    
    private fun mapToChatMessage(data: Map<String, Any>): ChatMessage {
        return ChatMessage(
            id = data["id"] as? String ?: "",
            chatId = data["chatId"] as? String ?: "",
            tradeId = data["tradeId"] as? String ?: "",
            senderId = data["senderId"] as? String ?: "",
            receiverId = data["receiverId"] as? String ?: "",
            message = data["message"] as? String ?: "",
            type = com.example.swoptrader.data.model.MessageType.valueOf(data["type"] as? String ?: "TEXT"),
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isRead = data["isRead"] as? Boolean ?: false
        )
    }
    
    // Helper functions for meetups
    private fun meetupToMap(meetup: Meetup): Map<String, Any?> {
        return mapOf(
            "id" to meetup.id,
            "offerId" to meetup.offerId,
            "participantIds" to meetup.participantIds,
            "location" to meetup.location?.let { location ->
                mapOf(
                    "name" to location.name,
                    "address" to location.address,
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "type" to location.type.name
                )
            },
            "scheduledAt" to meetup.scheduledAt,
            "meetupType" to meetup.meetupType.name,
            "status" to meetup.status.name,
            "notes" to meetup.notes,
            "createdAt" to meetup.createdAt,
            "updatedAt" to meetup.updatedAt
        )
    }
    
    private fun mapToMeetup(data: Map<String, Any>): Meetup? {
        return try {
            Meetup(
                id = data["id"] as? String ?: "",
                offerId = data["offerId"] as? String ?: "",
                participantIds = (data["participantIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                location = (data["location"] as? Map<String, Any>)?.let { locationData ->
                    MeetupLocation(
                        name = locationData["name"] as? String ?: "",
                        address = locationData["address"] as? String ?: "",
                        latitude = (locationData["latitude"] as? Number)?.toDouble() ?: 0.0,
                        longitude = (locationData["longitude"] as? Number)?.toDouble() ?: 0.0,
                        type = LocationType.valueOf(locationData["type"] as? String ?: "PUBLIC_PLACE")
                    )
                } ?: MeetupLocation(
                    name = "Unknown Location",
                    address = "Unknown Address",
                    latitude = 0.0,
                    longitude = 0.0,
                    type = LocationType.PUBLIC_PLACE
                ),
                scheduledAt = (data["scheduledAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                meetupType = MeetupType.valueOf(data["meetupType"] as? String ?: "PICKUP"),
                status = MeetupStatus.valueOf(data["status"] as? String ?: "PENDING"),
                notes = data["notes"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to map meetup data: ${e.message}", e)
            null
        }
    }
    
    // Offer operations
    suspend fun saveOffer(offer: Offer): Result<Offer> {
        return try {
            Log.d(TAG, "Saving offer to Firestore: ${offer.id}")
            val offerMap = offerToMap(offer)
            firestore.collection(OFFERS_COLLECTION)
                .document(offer.id)
                .set(offerMap)
                .await()
            
            Log.d(TAG, "Offer saved successfully: ${offer.id}")
            Result.success(offer)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save offer: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getOffer(offerId: String): Result<Offer?> {
        return try {
            Log.d(TAG, "Getting offer from Firestore: $offerId")
            val document = firestore.collection(OFFERS_COLLECTION)
                .document(offerId)
                .get()
                .await()
            
            if (document.exists()) {
                val offer = mapToOffer(document.data ?: emptyMap())
                Log.d(TAG, "Offer retrieved successfully: $offerId")
                Result.success(offer)
            } else {
                Log.d(TAG, "Offer not found: $offerId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get offer: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getOffersByUser(userId: String): Result<List<Offer>> {
        return try {
            Log.d(TAG, "Getting offers for user: $userId")
            val querySnapshot = firestore.collection(OFFERS_COLLECTION)
                .whereEqualTo("fromUserId", userId)
                .get()
                .await()
            
            val querySnapshot2 = firestore.collection(OFFERS_COLLECTION)
                .whereEqualTo("toUserId", userId)
                .get()
                .await()
            
            val allOffers = mutableListOf<Offer>()
            
            // Process offers where user is the sender
            querySnapshot.documents.forEach { document ->
                try {
                    mapToOffer(document.data ?: emptyMap())?.let { allOffers.add(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse offer document: ${document.id}", e)
                }
            }
            
            // Process offers where user is the receiver
            querySnapshot2.documents.forEach { document ->
                try {
                    mapToOffer(document.data ?: emptyMap())?.let { 
                        if (!allOffers.any { existing -> existing.id == it.id }) {
                            allOffers.add(it)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse offer document: ${document.id}", e)
                }
            }
            
            // Sort by creation date
            allOffers.sortByDescending { it.createdAt }
            
            Log.d(TAG, "Retrieved ${allOffers.size} offers for user: $userId")
            Result.success(allOffers)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get offers for user: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getOffersForItem(itemId: String): Result<List<Offer>> {
        return try {
            Log.d(TAG, "Getting offers for item: $itemId")
            val querySnapshot = firestore.collection(OFFERS_COLLECTION)
                .whereEqualTo("requestedItemId", itemId)
                .get()
                .await()
            
            val offers = querySnapshot.documents.mapNotNull { document ->
                try {
                    mapToOffer(document.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse offer document: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Retrieved ${offers.size} offers for item: $itemId")
            Result.success(offers)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get offers for item: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Helper functions for offers
    private fun offerToMap(offer: Offer): Map<String, Any?> {
        return mapOf(
            "id" to offer.id,
            "fromUserId" to offer.fromUserId,
            "toUserId" to offer.toUserId,
            "requestedItemId" to offer.requestedItemId,
            "offeredItemIds" to offer.offeredItemIds,
            "status" to offer.status.name,
            "message" to offer.message,
            "cashAmount" to offer.cashAmount,
            "createdAt" to offer.createdAt,
            "updatedAt" to offer.updatedAt,
            "meetup" to offer.meetup?.let { meetup ->
                mapOf(
                    "id" to meetup.id,
                    "offerId" to meetup.offerId,
                    "participantIds" to meetup.participantIds,
                    "location" to meetup.location?.let { location ->
                        mapOf(
                            "name" to location.name,
                            "address" to location.address,
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                            "type" to location.type.name
                        )
                    },
                    "scheduledAt" to meetup.scheduledAt,
                    "meetupType" to meetup.meetupType.name,
                    "status" to meetup.status.name,
                    "notes" to meetup.notes,
                    "createdAt" to meetup.createdAt,
                    "updatedAt" to meetup.updatedAt
                )
            }
        )
    }
    
    private fun mapToOffer(data: Map<String, Any>): Offer? {
        return try {
            Offer(
                id = data["id"] as? String ?: "",
                fromUserId = data["fromUserId"] as? String ?: "",
                toUserId = data["toUserId"] as? String ?: "",
                requestedItemId = data["requestedItemId"] as? String ?: "",
                offeredItemIds = (data["offeredItemIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                status = com.example.swoptrader.data.model.OfferStatus.valueOf(data["status"] as? String ?: "PENDING"),
                message = data["message"] as? String ?: "",
                cashAmount = (data["cashAmount"] as? Number)?.toDouble(),
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                meetup = (data["meetup"] as? Map<String, Any>)?.let { meetupData ->
                    com.example.swoptrader.data.model.Meetup(
                        id = meetupData["id"] as? String ?: "",
                        offerId = meetupData["offerId"] as? String ?: "",
                        participantIds = (meetupData["participantIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        location = (meetupData["location"] as? Map<String, Any>)?.let { locationData ->
                            com.example.swoptrader.data.model.MeetupLocation(
                                name = locationData["name"] as? String ?: "",
                                address = locationData["address"] as? String ?: "",
                                latitude = (locationData["latitude"] as? Number)?.toDouble() ?: 0.0,
                                longitude = (locationData["longitude"] as? Number)?.toDouble() ?: 0.0,
                                type = com.example.swoptrader.data.model.LocationType.valueOf(locationData["type"] as? String ?: "PUBLIC_PLACE")
                            )
                        } ?: com.example.swoptrader.data.model.MeetupLocation(
                            name = "Unknown Location",
                            address = "Unknown Address",
                            latitude = 0.0,
                            longitude = 0.0,
                            type = com.example.swoptrader.data.model.LocationType.PUBLIC_PLACE
                        ),
                        scheduledAt = (meetupData["scheduledAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        meetupType = com.example.swoptrader.data.model.MeetupType.valueOf(meetupData["meetupType"] as? String ?: "PICKUP"),
                        status = com.example.swoptrader.data.model.MeetupStatus.valueOf(meetupData["status"] as? String ?: "PENDING"),
                        notes = meetupData["notes"] as? String ?: "",
                        createdAt = (meetupData["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (meetupData["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to map offer data: ${e.message}", e)
            null
        }
    }
    
    // Meetup operations
    suspend fun saveMeetup(meetup: Meetup): Result<Meetup> {
        return try {
            Log.d(TAG, "Saving meetup to Firestore: ${meetup.id}")
            val meetupMap = meetupToMap(meetup)
            firestore.collection(MEETUPS_COLLECTION)
                .document(meetup.id)
                .set(meetupMap)
                .await()
            
            Log.d(TAG, "Meetup saved successfully: ${meetup.id}")
            Result.success(meetup)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save meetup: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMeetup(meetupId: String): Result<Meetup?> {
        return try {
            Log.d(TAG, "Getting meetup from Firestore: $meetupId")
            val document = firestore.collection(MEETUPS_COLLECTION)
                .document(meetupId)
                .get()
                .await()
            
            if (document.exists()) {
                val meetup = mapToMeetup(document.data ?: emptyMap())
                Log.d(TAG, "Meetup retrieved successfully: $meetupId")
                Result.success(meetup)
            } else {
                Log.d(TAG, "Meetup not found: $meetupId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get meetup: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMeetupByOfferId(offerId: String): Result<Meetup?> {
        return try {
            Log.d(TAG, "Getting meetup by offer ID from Firestore: $offerId")
            val querySnapshot = firestore.collection(MEETUPS_COLLECTION)
                .whereEqualTo("offerId", offerId)
                .limit(1)
                .get()
                .await()
            
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                val meetup = mapToMeetup(document.data ?: emptyMap())
                Log.d(TAG, "Meetup retrieved by offer ID successfully: $offerId")
                Result.success(meetup)
            } else {
                Log.d(TAG, "No meetup found for offer ID: $offerId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get meetup by offer ID: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteMeetup(meetupId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting meetup from Firestore: $meetupId")
            firestore.collection(MEETUPS_COLLECTION)
                .document(meetupId)
                .delete()
                .await()
            
            Log.d(TAG, "Meetup deleted successfully: $meetupId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete meetup: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Trade history operations
    suspend fun saveTradeHistory(tradeHistory: TradeHistory): Result<TradeHistory> {
        return try {
            Log.d(TAG, "Saving trade history to Firestore: ${tradeHistory.id}")
            val tradeMap = tradeHistoryToMap(tradeHistory)
            firestore.collection(TRADE_HISTORY_COLLECTION)
                .document(tradeHistory.id)
                .set(tradeMap)
                .await()
            
            Log.d(TAG, "Trade history saved successfully: ${tradeHistory.id}")
            Result.success(tradeHistory)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save trade history: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTradeHistory(tradeId: String): Result<TradeHistory?> {
        return try {
            Log.d(TAG, "Getting trade history from Firestore: $tradeId")
            val document = firestore.collection(TRADE_HISTORY_COLLECTION)
                .document(tradeId)
                .get()
                .await()
            
            if (document.exists()) {
                val tradeHistory = mapToTradeHistory(document.data ?: emptyMap())
                Log.d(TAG, "Trade history retrieved successfully: $tradeId")
                Result.success(tradeHistory)
            } else {
                Log.d(TAG, "Trade history not found: $tradeId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trade history: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTradeHistoryByUser(userId: String): Result<List<TradeHistory>> {
        return try {
            Log.d(TAG, "Getting trade history for user: $userId")
            val querySnapshot = firestore.collection(TRADE_HISTORY_COLLECTION)
                .whereArrayContains("participantIds", userId)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val tradeHistory = querySnapshot.documents.mapNotNull { document ->
                try {
                    mapToTradeHistory(document.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse trade history document: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Retrieved ${tradeHistory.size} trade history entries for user: $userId")
            Result.success(tradeHistory)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trade history for user: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTradeHistoryByOfferId(offerId: String): Result<TradeHistory?> {
        return try {
            Log.d(TAG, "Getting trade history for offer: $offerId")
            val querySnapshot = firestore.collection(TRADE_HISTORY_COLLECTION)
                .whereEqualTo("offerId", offerId)
                .limit(1)
                .get()
                .await()
            
            if (querySnapshot.documents.isNotEmpty()) {
                val tradeHistory = mapToTradeHistory(querySnapshot.documents[0].data ?: emptyMap())
                Log.d(TAG, "Trade history retrieved successfully for offer: $offerId")
                Result.success(tradeHistory)
            } else {
                Log.d(TAG, "Trade history not found for offer: $offerId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get trade history for offer: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Helper functions for trade history
    private fun tradeHistoryToMap(tradeHistory: TradeHistory): Map<String, Any?> {
        return mapOf(
            "id" to tradeHistory.id,
            "offerId" to tradeHistory.offerId,
            "participantIds" to tradeHistory.participantIds,
            "itemsTraded" to tradeHistory.itemsTraded.map { item ->
                mapOf(
                    "itemId" to item.itemId,
                    "userId" to item.userId,
                    "itemName" to item.itemName,
                    "itemImage" to item.itemImage
                )
            },
            "completedAt" to tradeHistory.completedAt,
            "meetupId" to tradeHistory.meetupId,
            "rating" to tradeHistory.rating?.let { rating ->
                mapOf(
                    "rating" to rating.rating,
                    "comment" to rating.comment,
                    "ratedBy" to rating.ratedBy,
                    "ratedAt" to rating.ratedAt
                )
            },
            "carbonSaved" to tradeHistory.carbonSaved,
            "tradeScoreEarned" to tradeHistory.tradeScoreEarned
        )
    }
    
    private fun mapToTradeHistory(data: Map<String, Any>): TradeHistory? {
        return try {
            TradeHistory(
                id = data["id"] as? String ?: "",
                offerId = data["offerId"] as? String ?: "",
                participantIds = (data["participantIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                itemsTraded = (data["itemsTraded"] as? List<*>)?.mapNotNull { itemData ->
                    if (itemData is Map<*, *>) {
                        TradedItem(
                            itemId = itemData["itemId"] as? String ?: "",
                            userId = itemData["userId"] as? String ?: "",
                            itemName = itemData["itemName"] as? String ?: "",
                            itemImage = itemData["itemImage"] as? String
                        )
                    } else null
                } ?: emptyList(),
                completedAt = (data["completedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                meetupId = data["meetupId"] as? String,
                rating = (data["rating"] as? Map<String, Any>)?.let { ratingData ->
                    TradeRating(
                        rating = (ratingData["rating"] as? Number)?.toInt() ?: 0,
                        comment = ratingData["comment"] as? String,
                        ratedBy = ratingData["ratedBy"] as? String ?: "",
                        ratedAt = (ratingData["ratedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                },
                carbonSaved = (data["carbonSaved"] as? Number)?.toDouble() ?: 0.0,
                tradeScoreEarned = (data["tradeScoreEarned"] as? Number)?.toInt() ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to map trade history data: ${e.message}", e)
            null
        }
    }
    
    suspend fun updateUserFCMToken(userId: String, fcmToken: String): Result<Unit> {
        return try {
            Log.d(TAG, "Updating FCM token for user: $userId")
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("fcmToken", fcmToken)
                .await()
            
            Log.d(TAG, "FCM token updated successfully for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update FCM token: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun mapToChat(data: Map<String, Any>, documentId: String): Chat? {
        return try {
            Chat(
                id = documentId,
                participantIds = (data["participantIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                offerId = data["offerId"] as? String,
                itemId = data["itemId"] as? String,
                itemName = data["itemName"] as? String,
                otherUser = null, // Will be populated separately if needed
                lastMessageAt = (data["lastMessageAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isActive = data["isActive"] as? Boolean ?: true,
                unreadCount = (data["unreadCount"] as? Map<String, Any>)?.mapValues { 
                    (it.value as? Number)?.toInt() ?: 0 
                } ?: emptyMap(),
                lastMessage = null // Will be populated separately if needed
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to map chat data: ${e.message}", e)
            null
        }
    }
    
    // Admin function to clean up orphaned items
    suspend fun cleanupOrphanedItems(): Result<Int> {
        return try {
            Log.d(TAG, "Starting cleanup of orphaned items in Firebase")
            
            // Get all items
            val itemsSnapshot = firestore.collection(ITEMS_COLLECTION).get().await()
            val allItems = itemsSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Item(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        category = ItemCategory.valueOf(data["category"] as? String ?: "OTHER"),
                        condition = ItemCondition.valueOf(data["condition"] as? String ?: "FAIR"),
                        images = (data["images"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        ownerId = data["ownerId"] as? String ?: "",
                        location = null, // Simplified for cleanup
                        desiredTrades = (data["desiredTrades"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        isAvailable = data["isAvailable"] as? Boolean ?: true,
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        viewCount = (data["viewCount"] as? Number)?.toInt() ?: 0,
                        pitchCount = (data["pitchCount"] as? Number)?.toInt() ?: 0,
                        commentsCount = (data["commentsCount"] as? Number)?.toInt() ?: 0,
                        distance = (data["distance"] as? Number)?.toDouble(),
                        owner = null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse item ${doc.id}: ${e.message}")
                    null
                }
            }
            
            // Get all user IDs
            val usersSnapshot = firestore.collection(USERS_COLLECTION).get().await()
            val allUserIds = usersSnapshot.documents.map { it.id }.toSet()
            
            // Find orphaned items
            val orphanedItems = allItems.filter { item ->
                item.ownerId.isNotEmpty() && !allUserIds.contains(item.ownerId)
            }
            
            Log.d(TAG, "Found ${orphanedItems.size} orphaned items out of ${allItems.size} total items")
            
            // Delete orphaned items
            var deletedCount = 0
            for (item in orphanedItems) {
                try {
                    firestore.collection(ITEMS_COLLECTION).document(item.id).delete().await()
                    deletedCount++
                    Log.d(TAG, "Deleted orphaned item: ${item.id} (owner: ${item.ownerId})")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete orphaned item ${item.id}: ${e.message}")
                }
            }
            
            Log.d(TAG, "Cleanup completed. Deleted $deletedCount orphaned items")
            Result.success(deletedCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup orphaned items: ${e.message}", e)
            Result.failure(e)
        }
    }
    
}
