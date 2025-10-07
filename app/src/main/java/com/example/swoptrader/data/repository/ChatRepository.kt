package com.example.swoptrader.data.repository

import com.example.swoptrader.data.local.dao.ChatDao
import com.example.swoptrader.data.model.Chat
import com.example.swoptrader.data.model.ChatMessage
import com.example.swoptrader.data.model.toEntity
import com.example.swoptrader.data.model.toChat
import com.example.swoptrader.data.model.toChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ChatRepository {
    suspend fun getChatById(chatId: String): Result<Chat?>
    suspend fun getChatsByUser(userId: String): Result<List<Chat>>
    suspend fun getMessagesForChat(chatId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(chatId: String, message: String, senderId: String): Result<ChatMessage>
    suspend fun createChat(participantIds: List<String>, offerId: String? = null): Result<Chat>
    fun getMessagesForChatFlow(chatId: String): Flow<List<ChatMessage>>
    suspend fun syncChatsWithRemote(userId: String): Result<Unit>
    suspend fun syncChatMessagesWithRemote(chatId: String): Result<Unit>
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val firestoreRepository: FirestoreRepository
) : ChatRepository {
    
    override suspend fun getChatsByUser(userId: String): Result<List<Chat>> {
        return try {
            val result = firestoreRepository.getChatsByUser(userId)
            result.fold(
                onSuccess = { chats ->
                    Result.success(chats)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getChatById(chatId: String): Result<Chat?> {
        return try {
            // First try to get from local database
            val localChat = chatDao.getChatById(chatId)
            if (localChat != null) {
                Result.success(localChat.toChat())
            } else {
                // If not found locally, try to sync from Firestore
                val firestoreResult = firestoreRepository.getChat(chatId)
                firestoreResult.fold(
                    onSuccess = { chat ->
                        chat?.let {
                            // Save to local database
                            chatDao.insertChat(it.toEntity())
                        }
                        Result.success(chat)
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getMessagesForChat(chatId: String): Result<List<ChatMessage>> {
        return try {
            // First sync with remote to get latest messages
            syncChatMessagesWithRemote(chatId)
            
            // Then get from local database
            val messages = chatDao.getMessagesByChatOnce(chatId).map { it.toChatMessage() }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendMessage(chatId: String, message: String, senderId: String): Result<ChatMessage> {
        return try {
            println("ChatRepository: Sending message to chatId: $chatId, message: $message, senderId: $senderId")
            
            val newMessage = ChatMessage(
                id = "msg_${System.currentTimeMillis()}",
                chatId = chatId,
                tradeId = "",
                receiverId = "",
                senderId = senderId,
                message = message,
                type = com.example.swoptrader.data.model.MessageType.TEXT,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            
            println("ChatRepository: Created message with id: ${newMessage.id}")
            
            // Save to local database first
            chatDao.insertMessage(newMessage.toEntity())
            println("ChatRepository: Saved message to local database")
            
            // Then save to Firestore
            val firestoreResult = firestoreRepository.saveChatMessage(newMessage)
            firestoreResult.fold(
                onSuccess = {
                    println("ChatRepository: Successfully saved message to Firestore")
                    Result.success(newMessage)
                },
                onFailure = { error ->
                    println("ChatRepository: Failed to save message to Firestore: ${error.message}")
                    Result.success(newMessage)
                }
            )
        } catch (e: Exception) {
            println("ChatRepository: Exception in sendMessage: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun createChat(participantIds: List<String>, offerId: String?): Result<Chat> {
        return try {
            // Check if a chat already exists between these participants
            val existingChatsResult = firestoreRepository.getChatsByUser(participantIds.first())
            existingChatsResult.fold(
                onSuccess = { existingChats ->
                    // Look for a chat with the same participants
                    val existingChat = existingChats.find { chat ->
                        chat.participantIds.containsAll(participantIds) && 
                        participantIds.containsAll(chat.participantIds)
                    }
                    
                    if (existingChat != null) {
                        // Chat already exists, return it
                        println("Chat already exists: ${existingChat.id}")
                        Result.success(existingChat)
                    } else {
                        // Create new chat
                        createNewChat(participantIds, offerId)
                    }
                },
                onFailure = { error ->
                    // If we can't check existing chats, create a new one
                    println("Failed to check existing chats: ${error.message}")
                    createNewChat(participantIds, offerId)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun createNewChat(participantIds: List<String>, offerId: String?): Result<Chat> {
        return try {
            val chatId = "chat_${System.currentTimeMillis()}"
            val chat = Chat(
                id = chatId,
                participantIds = participantIds,
                offerId = offerId,
                lastMessageAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                isActive = true,
                unreadCount = emptyMap()
            )
            
            // Save to local database first
            chatDao.insertChat(chat.toEntity())
            
            // Then save to Firestore
            val firestoreResult = firestoreRepository.saveChat(chat)
            firestoreResult.fold(
                onSuccess = {
                    Result.success(chat)
                },
                onFailure = { error ->
                    println("Failed to save chat to Firestore: ${error.message}")
                    Result.success(chat)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getMessagesForChatFlow(chatId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesByChat(chatId).map { entities ->
            entities.map { it.toChatMessage() }
        }
    }
    
    override suspend fun syncChatsWithRemote(userId: String): Result<Unit> {
        return try {
            val firestoreResult = firestoreRepository.getChatsByUser(userId)
            firestoreResult.fold(
                onSuccess = { chats ->
                    // Save to local database
                    val chatEntities = chats.map { it.toEntity() }
                    chatEntities.forEach { chatDao.insertChat(it) }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncChatMessagesWithRemote(chatId: String): Result<Unit> {
        return try {
            val firestoreResult = firestoreRepository.getChatMessages(chatId)
            firestoreResult.fold(
                onSuccess = { messages ->
                    // Save to local database
                    val messageEntities = messages.map { it.toEntity() }
                    chatDao.insertMessages(messageEntities)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
