package com.example.swoptrader.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.ChatMessage
import com.example.swoptrader.data.model.MessageType
import com.example.swoptrader.data.model.Offer
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.model.User
import com.example.swoptrader.data.repository.ChatRepository
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.OfferRepository
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val offerRepository: OfferRepository,
    private val itemRepository: ItemRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var navigateToMeetupCallback: ((String) -> Unit)? = null
    
    fun setNavigateToMeetupCallback(callback: (String) -> Unit) {
        navigateToMeetupCallback = callback
    }
    
    fun loadChat(chatId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                    return@launch
                }
                
                // Load chat and messages from repository
                val chatResult = chatRepository.getChatById(chatId)
                val messagesResult = chatRepository.getMessagesForChat(chatId)
                
                chatResult.fold(
                    onSuccess = { chat ->
                        messagesResult.fold(
                            onSuccess = { messages ->
                                // Load other user information
                                val otherUserId = chat?.participantIds?.firstOrNull { it != currentUser.id }
                                val otherUser = if (otherUserId != null) {
                                    val userResult = firestoreRepository.getUser(otherUserId)
                                    userResult.getOrNull()
                                } else null
                                
                                // Load offer and item details if offerId exists
                                if (chat?.offerId != null) {
                                    loadOfferAndItems(chat.offerId!!, currentUser.id)
                                }
                                
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    messages = messages,
                                    showOfferActions = chat?.offerId != null,
                                    chatId = chatId,
                                    currentUserId = currentUser.id,
                                    otherUser = otherUser
                                )
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = error.message ?: "Failed to load messages"
                                )
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load chat"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load chat"
                )
            }
        }
    }
    
    private fun loadOfferAndItems(offerId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                // Load offer details
                val offerResult = offerRepository.getOfferById(offerId)
                offerResult.fold(
                    onSuccess = { offer ->
                        if (offer != null) {
                            // Load requested item
                            val requestedItemResult = itemRepository.getItemById(offer.requestedItemId)
                            val requestedItem = requestedItemResult.getOrNull()
                            
                            // Load offered items
                            val offeredItemsResult = offer.offeredItemIds.mapNotNull { itemId ->
                                itemRepository.getItemById(itemId).getOrNull()
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                offer = offer,
                                requestedItem = requestedItem,
                                offeredItems = offeredItemsResult
                            )
                        }
                    },
                    onFailure = { error ->
                        // Don't fail the whole screen if offer loading fails
                        println("Failed to load offer: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                println("Exception loading offer and items: ${e.message}")
            }
        }
    }
    
    fun updateMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }
    
    fun sendMessage() {
        val message = _uiState.value.currentMessage
        val chatId = _uiState.value.chatId
        
        if (message.isNotBlank() && chatId.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val currentUser = authRepository.getCurrentUser()
                    if (currentUser == null) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "User not logged in"
                        )
                        return@launch
                    }
                    
                    // Send message through repository
                    val result = chatRepository.sendMessage(
                        chatId = chatId,
                        message = message,
                        senderId = currentUser.id
                    )
                    
                    result.fold(
                        onSuccess = { newMessage ->
                            // Add the new message to the current list immediately for better UX
                            val currentMessages = _uiState.value.messages.toMutableList()
                            currentMessages.add(newMessage)
                            
                            _uiState.value = _uiState.value.copy(
                                messages = currentMessages,
                                currentMessage = ""
                            )
                            
                            // Also sync with Firestore
                            chatRepository.syncChatMessagesWithRemote(chatId)
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = error.message ?: "Failed to send message"
                            )
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message ?: "Failed to send message"
                    )
                }
            }
        }
    }
    
    fun acceptOffer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentOffer = _uiState.value.offer
                if (currentOffer == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No offer found to accept"
                    )
                    return@launch
                }
                
                // Update offer status to ACCEPTED
                val updatedOffer = currentOffer.copy(
                    status = com.example.swoptrader.data.model.OfferStatus.ACCEPTED,
                    updatedAt = System.currentTimeMillis()
                )
                
                // Save updated offer
                val offerResult = offerRepository.updateOffer(updatedOffer)
                offerResult.fold(
                    onSuccess = {
                        // Send system message
                        val systemMessage = ChatMessage(
                            id = "msg_${System.currentTimeMillis()}",
                            chatId = _uiState.value.chatId,
                            tradeId = currentOffer.id,
                            senderId = "system",
                            receiverId = "",
                            message = "Offer accepted! Proceeding to meetup scheduling.",
                            type = MessageType.TEXT,
                            timestamp = System.currentTimeMillis()
                        )
                        
                        // Save message to chat
                        chatRepository.sendMessage(_uiState.value.chatId, systemMessage.message, "system")
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            messages = _uiState.value.messages + systemMessage,
                            showOfferActions = false,
                            offer = updatedOffer
                        )
                        
                        // Navigate to meetup screen
                        navigateToMeetupCallback?.invoke(currentOffer.id)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to accept offer: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to accept offer"
                )
            }
        }
    }
    
    fun rejectOffer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Mock API call
                kotlinx.coroutines.delay(1000)
                
                val systemMessage = ChatMessage(
                    id = "msg_${System.currentTimeMillis()}",
                    chatId = "chat_123",
                    tradeId = "trade_123",
                    senderId = "system",
                    receiverId = "other_user",
                    message = "Offer rejected.",
                    type = MessageType.MEETUP,
                    timestamp = System.currentTimeMillis()
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = _uiState.value.messages + systemMessage,
                    showOfferActions = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to reject offer"
                )
            }
        }
    }
    
    fun counterOffer() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "Counter offer functionality coming soon"
        )
    }
    
    private fun generateMockMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "msg_1",
                chatId = "chat_123",
                tradeId = "trade_123",
                senderId = "other_user",
                receiverId = "current_user",
                message = "Hi! I'm interested in your vintage camera. Would you be interested in trading for my MacBook Pro?",
                type = MessageType.TEXT,
                timestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
            ),
            ChatMessage(
                id = "msg_2",
                chatId = "chat_123",
                tradeId = "trade_123",
                senderId = "other_user",
                receiverId = "current_user",
                message = "",
                type = MessageType.OFFER,
                timestamp = System.currentTimeMillis() - 3500000 // 58 minutes ago
            ),
            ChatMessage(
                id = "msg_3",
                chatId = "chat_123",
                tradeId = "trade_123",
                senderId = "current_user",
                receiverId = "other_user",
                message = "That sounds interesting! Can you tell me more about the MacBook's condition?",
                type = MessageType.TEXT,
                timestamp = System.currentTimeMillis() - 3400000 // 57 minutes ago
            ),
            ChatMessage(
                id = "msg_4",
                chatId = "chat_123",
                tradeId = "trade_123",
                senderId = "other_user",
                receiverId = "current_user",
                message = "It's in excellent condition, barely used. I have all the original accessories and the box. What about the camera?",
                type = MessageType.TEXT,
                timestamp = System.currentTimeMillis() - 3300000 // 55 minutes ago
            ),
            ChatMessage(
                id = "msg_5",
                chatId = "chat_123",
                tradeId = "trade_123",
                senderId = "current_user",
                receiverId = "other_user",
                message = "The camera is in great working condition. I've taken excellent care of it. I can send you some sample photos if you'd like.",
                type = MessageType.TEXT,
                timestamp = System.currentTimeMillis() - 3200000 // 53 minutes ago
            )
        )
    }
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val showOfferActions: Boolean = false,
    val errorMessage: String = "",
    val chatId: String = "",
    val currentUserId: String = "",
    val otherUser: User? = null,
    val offer: Offer? = null,
    val requestedItem: Item? = null,
    val offeredItems: List<Item> = emptyList()
)
