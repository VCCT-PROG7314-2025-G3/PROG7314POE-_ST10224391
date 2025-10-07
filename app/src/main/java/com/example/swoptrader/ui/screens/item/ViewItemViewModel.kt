package com.example.swoptrader.ui.screens.item

/**
 * ViewItemViewModel - MVVM Architecture Implementation
 * 
 * This ViewModel implements the MVVM (Model-View-ViewModel) architectural pattern for the
 * item viewing screen. It manages UI state, handles business logic, and coordinates data
 * operations between the UI and data layers.
 * 
 * Key Architectural Concepts:
 * - MVVM Pattern (Microsoft, 2020)
 * - State Management with StateFlow (Kotlin, 2023)
 * - Dependency Injection with Hilt (Google, 2022)
 * - Repository Pattern (Fowler, 2002)
 * - Reactive Programming (ReactiveX, 2023)
 * - Separation of Concerns (Martin, 2017)
 * 
 * State Management Features:
 * - Immutable UI state using data classes
 * - Reactive state updates with StateFlow
 * - Proper lifecycle management
 * - Error handling and loading states
 * 
 * References:
 * - Microsoft. (2020). MVVM Pattern. Microsoft Documentation.
 * - Kotlin. (2023). StateFlow and SharedFlow. Kotlin Coroutines Documentation.
 * - Google. (2022). Dependency Injection with Hilt. Android Developers Guide.
 * - Fowler, M. (2002). Patterns of Enterprise Application Architecture. Addison-Wesley.
 * - ReactiveX. (2023). Reactive Programming Principles. ReactiveX Documentation.
 * - Martin, R. C. (2017). Clean Architecture: A Craftsman's Guide to Software Structure and Design.
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.data.repository.UserRepository
import com.example.swoptrader.data.repository.CommentRepository
import com.example.swoptrader.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ViewItemUiState())
    val uiState: StateFlow<ViewItemUiState> = _uiState.asStateFlow()
    
    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get item from repository
                val result = itemRepository.getItemById(itemId)
                result.fold(
                    onSuccess = { item ->
                        if (item != null) {
                            // If item doesn't have owner info, fetch it separately
                            val itemWithOwner = if (item.owner == null && item.ownerId.isNotEmpty()) {
                                val ownerResult = userRepository.getUserById(item.ownerId)
                                ownerResult.fold(
                                    onSuccess = { owner -> item.copy(owner = owner) },
                                    onFailure = { item } // Keep original item if owner fetch fails
                                )
                            } else {
                                item
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                item = itemWithOwner
                            )
                            
                            // Load comments for this item
                            loadComments(itemId)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Item not found"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load item"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load item"
                )
            }
        }
    }
    
    /**
     * Comment Loading with Data Synchronization
     * 
     * This method implements data synchronization patterns for loading comments from
     * multiple data sources (local database and cloud Firestore). It follows the
     * offline-first architecture pattern and implements proper error handling.
     * 
     * Data Synchronization Patterns:
     * - Offline-First Architecture (Google, 2023)
     * - Data Consistency Patterns (Kleppmann, 2017)
     * - Error Handling with Result Types (Kotlin, 2023)
     * - Coroutine-based Asynchronous Operations (Kotlin, 2023)
     * 
     * References:
     * - Google. (2023). Offline-First Mobile Development. Firebase Documentation.
     * - Kleppmann, M. (2017). Designing Data-Intensive Applications. O'Reilly Media.
     * - Kotlin. (2023). Result Type and Error Handling. Kotlin Documentation.
     * - Kotlin. (2023). Coroutines and Asynchronous Programming. Kotlin Documentation.
     */
    private fun loadComments(itemId: String) {
        // Coroutine-based asynchronous operation for non-blocking UI (Kotlin, 2023)
        viewModelScope.launch {
            try {
                // Repository pattern with Result type for error handling (Fowler, 2002)
                val result = commentRepository.getCommentsByItem(itemId)
                result.fold(
                    onSuccess = { comments ->
                        // Immutable state update following reactive programming principles (ReactiveX, 2023)
                        _uiState.value = _uiState.value.copy(comments = comments)
                    },
                    onFailure = { error ->
                        // Graceful error handling without crashing the UI
                        println("Failed to load comments: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                // Exception handling for unexpected errors
                println("Exception loading comments: ${e.message}")
            }
        }
    }
    
    fun addComment(content: String) {
        viewModelScope.launch {
            try {
                val currentItem = _uiState.value.item
                val currentUser = authRepository.getCurrentUser()
                
                if (currentItem != null && currentUser != null) {
                    // Create a new comment
                    val newComment = com.example.swoptrader.data.model.Comment(
                        id = "comment_${System.currentTimeMillis()}",
                        itemId = currentItem.id,
                        authorId = currentUser.id,
                        authorName = currentUser.name,
                        authorProfileImageUrl = currentUser.profileImageUrl,
                        content = content,
                        createdAt = System.currentTimeMillis()
                    )
                    
                    // Save comment to repository/database
                    val result = commentRepository.saveComment(newComment)
                    result.fold(
                        onSuccess = {
                            // Reload comments to get the latest from database
                            loadComments(currentItem.id)
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Failed to save comment: ${error.message}"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Unable to add comment: item or user not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add comment: ${e.message}"
                )
            }
        }
    }
    
    private fun createMockItem(itemId: String): Item {
        return Item(
            id = itemId,
            name = "Vintage Camera",
            description = "Canon EOS 5D Mark III in excellent condition. Perfect for photography enthusiasts. Comes with original box and accessories.",
            category = com.example.swoptrader.data.model.ItemCategory.ELECTRONICS,
            condition = com.example.swoptrader.data.model.ItemCondition.GOOD,
            images = listOf(
                "https://via.placeholder.com/400x300?text=Camera+1",
                "https://via.placeholder.com/400x300?text=Camera+2",
                "https://via.placeholder.com/400x300?text=Camera+3"
            ),
            ownerId = "owner_123",
            owner = com.example.swoptrader.data.model.User(
                id = "owner_123",
                name = "John Doe",
                email = "john.doe@example.com",
                profileImageUrl = "https://via.placeholder.com/100",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Johannesburg, South Africa"
                ),
                tradeScore = 150,
                level = 3,
                carbonSaved = 25.5,
                isVerified = true
            ),
            location = com.example.swoptrader.data.model.Location(
                latitude = -26.2041,
                longitude = 28.0473,
                address = "Johannesburg, South Africa"
            ),
            desiredTrades = listOf("Laptop", "Guitar", "Books", "Furniture"),
            isAvailable = true,
            viewCount = 45,
            pitchCount = 3
        )
    }
}

data class ViewItemUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val comments: List<com.example.swoptrader.data.model.Comment> = emptyList(),
    val errorMessage: String = ""
)
