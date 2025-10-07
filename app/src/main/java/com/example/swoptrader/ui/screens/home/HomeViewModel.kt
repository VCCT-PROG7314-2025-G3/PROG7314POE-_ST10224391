package com.example.swoptrader.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.*
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.data.repository.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = itemRepository.getAllItems()
                result.fold(
                    onSuccess = { items ->
                        // Load comments count for each item
                        val itemsWithComments = items.map { item ->
                            val commentsResult = commentRepository.getCommentsByItem(item.id)
                            val commentsCount = commentsResult.getOrNull()?.size ?: 0
                            item.copy(commentsCount = commentsCount)
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            items = itemsWithComments,
                            suggestedMatches = itemsWithComments.take(3)
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load items"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load items"
                )
            }
        }
    }
    
    fun refreshItems() {
        loadItems()
    }
    
    fun refreshCommentsCount() {
        viewModelScope.launch {
            val currentItems = _uiState.value.items
            val itemsWithUpdatedComments = currentItems.map { item ->
                val commentsResult = commentRepository.getCommentsByItem(item.id)
                val commentsCount = commentsResult.getOrNull()?.size ?: 0
                item.copy(commentsCount = commentsCount)
            }
            _uiState.value = _uiState.value.copy(items = itemsWithUpdatedComments)
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun searchItems() {
        val query = _uiState.value.searchQuery
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val filteredItems = _uiState.value.items.filter { item ->
                        item.name.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.category.displayName.contains(query, ignoreCase = true)
                    }
                    _uiState.value = _uiState.value.copy(items = filteredItems)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message ?: "Search failed"
                    )
                }
            }
        } else {
            loadItems()
        }
    }
    
    fun selectCategory(category: ItemCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        
        viewModelScope.launch {
            try {
                val result = itemRepository.getAllItems()
                result.fold(
                    onSuccess = { allItems ->
                        val filteredItems = if (category != null) {
                            allItems.filter { it.category == category }
                        } else {
                            allItems
                        }
                        _uiState.value = _uiState.value.copy(items = filteredItems)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message ?: "Failed to filter items"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to filter items"
                )
            }
        }
    }
    
    fun toggleTradeRadius() {
        _uiState.value = _uiState.value.copy(
            tradeRadiusEnabled = !_uiState.value.tradeRadiusEnabled
        )
        applyFilters()
    }
    
    fun updateTradeRadius(radiusKm: Double) {
        _uiState.value = _uiState.value.copy(tradeRadiusKm = radiusKm)
        if (_uiState.value.tradeRadiusEnabled) {
            applyFilters()
        }
    }
    
    private fun applyFilters() {
        viewModelScope.launch {
            try {
                val result = itemRepository.getAllItems()
                result.fold(
                    onSuccess = { allItems ->
                        var filteredItems = allItems
                        
                        // Apply category filter
                        if (_uiState.value.selectedCategory != null) {
                            filteredItems = filteredItems.filter { 
                                it.category == _uiState.value.selectedCategory 
                            }
                        }
                        
                        // Apply trade radius filter
                        if (_uiState.value.tradeRadiusEnabled) {
                            filteredItems = filteredItems.filter { item ->
                                item.distance == null || item.distance <= _uiState.value.tradeRadiusKm
                            }
                        }
                        
                        _uiState.value = _uiState.value.copy(items = filteredItems)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message ?: "Failed to apply filters"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to apply filters"
                )
            }
        }
    }
    
    private fun generateMockItems(): List<Item> {
        return listOf(
            Item(
                id = "1",
                name = "Vintage Camera",
                description = "Canon EOS 5D Mark III in excellent condition. Perfect for photography enthusiasts.",
                category = ItemCategory.ELECTRONICS,
                condition = ItemCondition.GOOD,
                images = listOf("https://via.placeholder.com/300x200"),
                ownerId = "user1",
                location = Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Johannesburg, South Africa"
                ),
                desiredTrades = listOf("Laptop", "Guitar", "Books"),
                viewCount = 45,
                pitchCount = 3
            ),
            Item(
                id = "2",
                name = "Designer Handbag",
                description = "Louis Vuitton handbag, barely used. Looking for high-quality electronics.",
                category = ItemCategory.CLOTHING,
                condition = ItemCondition.LIKE_NEW,
                images = listOf("https://via.placeholder.com/300x200"),
                ownerId = "user2",
                location = Location(
                    latitude = -33.9249,
                    longitude = 18.4241,
                    address = "Cape Town, South Africa"
                ),
                desiredTrades = listOf("iPhone", "MacBook", "Camera"),
                viewCount = 78,
                pitchCount = 5
            ),
            Item(
                id = "3",
                name = "Programming Books",
                description = "Collection of programming books: Clean Code, Design Patterns, and more.",
                category = ItemCategory.BOOKS,
                condition = ItemCondition.GOOD,
                images = listOf("https://via.placeholder.com/300x200"),
                ownerId = "user3",
                location = Location(
                    latitude = -29.8587,
                    longitude = 31.0218,
                    address = "Durban, South Africa"
                ),
                desiredTrades = listOf("Electronics", "Tools", "Art"),
                viewCount = 23,
                pitchCount = 1
            ),
            Item(
                id = "4",
                name = "Mountain Bike",
                description = "Trek mountain bike, perfect for outdoor adventures. Well maintained.",
                category = ItemCategory.SPORTS,
                condition = ItemCondition.GOOD,
                images = listOf("https://via.placeholder.com/300x200"),
                ownerId = "user4",
                location = Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Johannesburg, South Africa"
                ),
                desiredTrades = listOf("Guitar", "Camera", "Furniture"),
                viewCount = 67,
                pitchCount = 4
            ),
            Item(
                id = "5",
                name = "Wooden Dining Table",
                description = "Solid oak dining table with 6 chairs. Perfect for family gatherings.",
                category = ItemCategory.FURNITURE,
                condition = ItemCondition.FAIR,
                images = listOf("https://via.placeholder.com/300x200"),
                ownerId = "user5",
                location = Location(
                    latitude = -33.9249,
                    longitude = 18.4241,
                    address = "Cape Town, South Africa"
                ),
                desiredTrades = listOf("Electronics", "Books", "Tools"),
                viewCount = 34,
                pitchCount = 2
            ),
            Item(
                id = "6",
                name = "Electric Guitar",
                description = "Fender Stratocaster electric guitar with amplifier. Great for beginners.",
                category = ItemCategory.MUSIC,
                condition = ItemCondition.GOOD,
                images = listOf("https://via.placeholder.com/300x200"),
                ownerId = "user6",
                location = Location(
                    latitude = -29.8587,
                    longitude = 31.0218,
                    address = "Durban, South Africa"
                ),
                desiredTrades = listOf("Camera", "Bike", "Electronics"),
                viewCount = 89,
                pitchCount = 6
            )
        )
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val suggestedMatches: List<Item> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ItemCategory? = null,
    val tradeRadiusEnabled: Boolean = false,
    val tradeRadiusKm: Double = 10.0,
    val errorMessage: String = ""
)
