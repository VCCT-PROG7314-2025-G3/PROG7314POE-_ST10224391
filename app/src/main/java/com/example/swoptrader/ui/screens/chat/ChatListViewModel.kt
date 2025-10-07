package com.example.swoptrader.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.Chat
import com.example.swoptrader.data.model.User
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ChatRepository
import com.example.swoptrader.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()
    
    fun loadChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val result = chatRepository.getChatsByUser(currentUser.id)
                    result.fold(
                        onSuccess = { chats ->
                            // Load other user data and last message for each chat
                            val chatsWithOtherUsers = chats.map { chat ->
                                val otherUserId = chat.participantIds.firstOrNull { it != currentUser.id }
                                val otherUser = if (otherUserId != null) {
                                    // Load other user data
                                    val userResult = firestoreRepository.getUser(otherUserId)
                                    userResult.getOrNull()
                                } else {
                                    null
                                }
                                
                                // Load last message for this chat
                                val lastMessageResult = firestoreRepository.getChatMessages(chat.id)
                                val lastMessage = lastMessageResult.getOrNull()?.lastOrNull()
                                
                                chat.copy(
                                    otherUser = otherUser,
                                    lastMessage = lastMessage
                                )
                            }
                            
                            // Sort chats by last message time
                            val sortedChats = chatsWithOtherUsers.sortedByDescending { 
                                it.lastMessage?.timestamp ?: it.lastMessageAt
                            }
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                chats = sortedChats
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Failed to load chats"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load chats"
                )
            }
        }
    }
    
    fun refreshChats() {
        loadChats()
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}

data class ChatListUiState(
    val isLoading: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val errorMessage: String = ""
)

