package com.example.swoptrader.ui.screens.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.*
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.data.repository.OfferRepository
import com.example.swoptrader.data.repository.UserRepository
import com.example.swoptrader.data.repository.MeetupRepository
import com.example.swoptrader.data.repository.FirestoreRepository
import com.example.swoptrader.data.repository.ChatRepository
import com.example.swoptrader.data.repository.TradeHistoryRepository
import com.example.swoptrader.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferDetailsViewModel @Inject constructor(
    private val offerRepository: OfferRepository,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val meetupRepository: MeetupRepository,
    private val firestoreRepository: FirestoreRepository,
    private val chatRepository: ChatRepository,
    private val tradeHistoryRepository: TradeHistoryRepository,
    private val notificationService: NotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OfferDetailsUiState())
    val uiState: StateFlow<OfferDetailsUiState> = _uiState.asStateFlow()
    
    fun loadOfferDetails(offerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            
            try {
                // Load the offer
                val offerResult = offerRepository.getOfferById(offerId)
                offerResult.fold(
                    onSuccess = { offer ->
                        if (offer != null) {
                            _uiState.value = _uiState.value.copy(offer = offer)
                            
                            // Load the other user
                            loadOtherUser(offer)
                            
                            // Load items
                            loadItems(offer)
                            
                            // Load meetup data if offer has one
                            if (offer.meetup != null) {
                                _uiState.value = _uiState.value.copy(offer = offer)
                            } else {
                                // load meetup from repository
                                val meetupResult = meetupRepository.getMeetupByOfferId(offer.id)
                                meetupResult.fold(
                                    onSuccess = { meetup ->
                                        if (meetup != null) {
                                            val updatedOffer = offer.copy(meetup = meetup)
                                            _uiState.value = _uiState.value.copy(offer = updatedOffer)
                                        }
                                    },
                                    onFailure = { /* Ignore meetup loading errors */ }
                                )
                            }
                            
                            // Check if this is a received offer
                            val currentUser = authRepository.getCurrentUser()
                            val isReceivedOffer = currentUser?.id == offer.toUserId
                            _uiState.value = _uiState.value.copy(isReceivedOffer = isReceivedOffer)
                            
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Offer not found"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load offer"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load offer"
                )
            }
        }
    }
    
    private suspend fun loadOtherUser(offer: Offer) {
        val currentUser = authRepository.getCurrentUser()
        val otherUserId = if (currentUser?.id == offer.fromUserId) {
            offer.toUserId
        } else {
            offer.fromUserId
        }
        
        val userResult = firestoreRepository.getUser(otherUserId)
        userResult.fold(
            onSuccess = { user ->
                _uiState.value = _uiState.value.copy(otherUser = user)
            },
            onFailure = { error ->
                // Don't fail the whole screen if user loading fails
                println("Failed to load user: ${error.message}")
            }
        )
    }
    
    private suspend fun loadItems(offer: Offer) {
        // Load requested item
        val requestedItemResult = itemRepository.getItemById(offer.requestedItemId)
        requestedItemResult.fold(
            onSuccess = { item ->
                _uiState.value = _uiState.value.copy(requestedItem = item)
            },
            onFailure = { error ->
                println("Failed to load requested item: ${error.message}")
            }
        )
        
        // Load offered items
        val offeredItems = mutableListOf<Item>()
        for (itemId in offer.offeredItemIds) {
            val itemResult = itemRepository.getItemById(itemId)
            itemResult.fold(
                onSuccess = { item ->
                    if (item != null) {
                        offeredItems.add(item)
                    }
                },
                onFailure = { error ->
                    println("Failed to load offered item: ${error.message}")
                }
            )
        }
        _uiState.value = _uiState.value.copy(offeredItems = offeredItems)
    }
    
    fun acceptOffer(onMeetupRequested: () -> Unit) {
        val offer = _uiState.value.offer ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val updatedOffer = offer.copy(
                    status = OfferStatus.ACCEPTED,
                    updatedAt = System.currentTimeMillis()
                )
                
                val result = offerRepository.updateOffer(updatedOffer)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            offer = updatedOffer,
                            successMessage = "Offer accepted! Let's arrange the meetup."
                        )
                        // Trigger meetup flow after a short delay
                        kotlinx.coroutines.delay(2000)
                        onMeetupRequested()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to accept offer"
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
        val offer = _uiState.value.offer ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val updatedOffer = offer.copy(
                    status = OfferStatus.REJECTED,
                    updatedAt = System.currentTimeMillis()
                )
                
                val result = offerRepository.updateOffer(updatedOffer)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            offer = updatedOffer,
                            successMessage = "Offer rejected"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to reject offer"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to reject offer"
                )
            }
        }
    }
    
    fun showCounterDialog() {
        _uiState.value = _uiState.value.copy(showCounterDialog = true)
    }
    
    fun hideCounterDialog() {
        _uiState.value = _uiState.value.copy(showCounterDialog = false)
    }
    
    fun sendCounterOffer(message: String) {
        val offer = _uiState.value.offer ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                val counterOffer = Offer(
                    id = "counter_${System.currentTimeMillis()}",
                    fromUserId = offer.toUserId, // Current user becomes the sender
                    toUserId = offer.fromUserId, // Original sender becomes receiver
                    requestedItemId = offer.requestedItemId,
                    offeredItemIds = offer.offeredItemIds,
                    status = OfferStatus.PENDING,
                    message = message,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                val result = offerRepository.createOffer(counterOffer)
                result.fold(
                    onSuccess = {
                        viewModelScope.launch {
                            notificationService.sendOfferNotification(
                                offer = counterOffer,
                                senderName = currentUser?.name ?: "SwopTrader user",
                                itemName = offer.requestedItem?.name
                            )
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Counter offer sent successfully!"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to send counter offer"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to send counter offer"
                )
            }
        }
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = "")
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
    
    fun completeTrade() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentOffer = _uiState.value.offer ?: throw Exception("No offer found")
                val currentMeetup = currentOffer.meetup ?: throw Exception("No meetup found")
                
                // Update meetup status to COMPLETED
                val updatedMeetup = currentMeetup.copy(
                    status = MeetupStatus.COMPLETED,
                    completedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                // Update meetup in repository
                val meetupResult = meetupRepository.updateMeetup(updatedMeetup)
                meetupResult.fold(
                    onSuccess = { savedMeetup ->
                        // Update the offer with the completed meetup
                        val updatedOffer = currentOffer.copy(
                            meetup = savedMeetup,
                            updatedAt = System.currentTimeMillis()
                        )
                        
                        // Save updated offer
                        val offerResult = offerRepository.updateOffer(updatedOffer)
                        offerResult.fold(
                            onSuccess = {
                                // Create trade history entry
                                createTradeHistoryEntry(updatedOffer, savedMeetup)
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Failed to update offer: ${error.message}"
                                )
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to complete meetup: ${error.message}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to complete trade"
                )
            }
        }
    }
    
    private suspend fun createTradeHistoryEntry(offer: Offer, meetup: Meetup) {
        try {
            // Get items being traded
            val requestedItemResult = itemRepository.getItemById(offer.requestedItemId)
            val requestedItem = requestedItemResult.getOrNull()
            
            // Get offered items one by one
            val offeredItems = mutableListOf<Item>()
            for (itemId in offer.offeredItemIds) {
                val itemResult = itemRepository.getItemById(itemId)
                itemResult.getOrNull()?.let { item ->
                    offeredItems.add(item)
                }
            }
            
            if (requestedItem != null) {
                // Create traded items list
                val itemsTraded = mutableListOf<TradedItem>()
                
                // Add requested item (going to the offer sender)
                itemsTraded.add(
                    TradedItem(
                        itemId = requestedItem.id,
                        userId = offer.fromUserId,
                        itemName = requestedItem.name,
                        itemImage = requestedItem.images.firstOrNull()
                    )
                )
                
                // Add offered items (going to the offer receiver)
                offeredItems.forEach { item ->
                    itemsTraded.add(
                        TradedItem(
                            itemId = item.id,
                            userId = offer.toUserId,
                            itemName = item.name,
                            itemImage = item.images.firstOrNull()
                        )
                    )
                }
                
                // Create trade history entry
                val tradeHistory = TradeHistory(
                    id = "trade_${System.currentTimeMillis()}",
                    offerId = offer.id,
                    participantIds = listOf(offer.fromUserId, offer.toUserId),
                    itemsTraded = itemsTraded,
                    completedAt = meetup.completedAt ?: System.currentTimeMillis(),
                    meetupId = meetup.id,
                    carbonSaved = calculateCarbonSaved(requestedItem, offeredItems),
                    tradeScoreEarned = calculateTradeScore(requestedItem, offeredItems)
                )
                
                // Save trade history
                val tradeHistoryResult = tradeHistoryRepository.createTradeHistory(tradeHistory)
                tradeHistoryResult.fold(
                    onSuccess = {
                        // Update user levels and scores
                        updateUserScoresAndLevels(offer.fromUserId, offer.toUserId, tradeHistory)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Trade completed but failed to save history: ${error.message}"
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load item details for trade history"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Failed to create trade history: ${e.message}"
            )
        }
    }
    
    private fun calculateCarbonSaved(requestedItem: Item, offeredItems: List<Item>): Double {
        // Enhanced carbon calculation based on item categories and conditions
        var totalCarbonSaved = 0.0
        
        // Base carbon savings for each item
        val baseCarbonPerItem = 3.0
        
        // Category multipliers for carbon impact
        val categoryMultipliers = mapOf(
            "electronics" to 1.5,    // Electronics have higher carbon footprint
            "furniture" to 1.3,      // Furniture manufacturing is carbon intensive
            "clothing" to 1.2,       // Fast fashion has high carbon cost
            "books" to 0.8,          // Books have lower carbon footprint
            "sports" to 1.1,         // Sports equipment moderate impact
            "tools" to 1.4,          // Tools manufacturing is carbon intensive
            "art" to 0.9,            // Art items moderate impact
            "music" to 1.2,          // Musical instruments moderate impact
            "garden" to 0.7,         // Garden items often sustainable
            "automotive" to 2.0,     // Automotive parts very high impact
            "home" to 1.1,           // Home items moderate impact
            "accessories" to 1.0,    // Accessories standard impact
            "other" to 1.0           // Default multiplier
        )
        
        // Condition multipliers (better condition = more carbon saved)
        val conditionMultipliers = mapOf(
            "new" to 1.0,
            "like_new" to 0.9,
            "good" to 0.8,
            "fair" to 0.6,
            "poor" to 0.4
        )
        
        // Calculate for requested item
        val requestedCategory = requestedItem.category.name.lowercase()
        val requestedCondition = requestedItem.condition.name.lowercase()
        val requestedMultiplier = categoryMultipliers[requestedCategory] ?: 1.0
        val requestedConditionMultiplier = conditionMultipliers[requestedCondition] ?: 1.0
        totalCarbonSaved += baseCarbonPerItem * requestedMultiplier * requestedConditionMultiplier
        
        // Calculate for offered items
        offeredItems.forEach { item ->
            val category = item.category.name.lowercase()
            val condition = item.condition.name.lowercase()
            val categoryMultiplier = categoryMultipliers[category] ?: 1.0
            val conditionMultiplier = conditionMultipliers[condition] ?: 1.0
            totalCarbonSaved += baseCarbonPerItem * categoryMultiplier * conditionMultiplier
        }
        
        // Bonus for multiple items (encouraging bulk trades)
        if (offeredItems.size > 1) {
            totalCarbonSaved *= 1.1 // 10% bonus for multiple items
        }
        
        return totalCarbonSaved
    }
    
    private fun calculateTradeScore(requestedItem: Item, offeredItems: List<Item>): Int {
        // Enhanced trade score calculation
        var baseScore = 15 // Base score for completing a trade
        
        // Category bonuses
        val categoryBonuses = mapOf(
            "electronics" to 5,      // Electronics are valuable
            "furniture" to 4,        // Furniture is substantial
            "clothing" to 2,         // Clothing is common
            "books" to 3,            // Books have educational value
            "sports" to 4,           // Sports equipment is valuable
            "tools" to 5,            // Tools are practical
            "art" to 6,              // Art has cultural value
            "music" to 5,            // Musical instruments are valuable
            "garden" to 3,           // Garden items are useful
            "automotive" to 8,       // Automotive parts are very valuable
            "home" to 3,             // Home items are useful
            "accessories" to 2,      // Accessories are common
            "other" to 2             // Default bonus
        )
        
        // Condition bonuses
        val conditionBonuses = mapOf(
            "new" to 5,
            "like_new" to 4,
            "good" to 3,
            "fair" to 2,
            "poor" to 1
        )
        
        // Calculate score for requested item
        val requestedCategory = requestedItem.category.name.lowercase()
        val requestedCondition = requestedItem.condition.name.lowercase()
        val requestedCategoryBonus = categoryBonuses[requestedCategory] ?: 2
        val requestedConditionBonus = conditionBonuses[requestedCondition] ?: 2
        baseScore += requestedCategoryBonus + requestedConditionBonus
        
        // Calculate score for offered items
        offeredItems.forEach { item ->
            val category = item.category.name.lowercase()
            val condition = item.condition.name.lowercase()
            val categoryBonus = categoryBonuses[category] ?: 2
            val conditionBonus = conditionBonuses[condition] ?: 2
            baseScore += categoryBonus + conditionBonus
        }
        
        // Bonus for multiple items
        if (offeredItems.size > 1) {
            baseScore += offeredItems.size * 2 // 2 points per additional item
        }
        
        // Bonus for high-value trades (items with multiple images suggest quality)
        if (requestedItem.images.size > 2) baseScore += 2
        offeredItems.forEach { item ->
            if (item.images.size > 2) baseScore += 1
        }
        
        return baseScore
    }
    
    private suspend fun updateUserScoresAndLevels(fromUserId: String, toUserId: String, tradeHistory: TradeHistory) {
        try {
            // Get both users
            val fromUserResult = userRepository.getUserById(fromUserId)
            val toUserResult = userRepository.getUserById(toUserId)
            
            val fromUser = fromUserResult.getOrNull()
            val toUser = toUserResult.getOrNull()
            
            if (fromUser != null && toUser != null) {
                // Calculate new scores and levels for both users
                val updatedFromUser = calculateNewUserStats(fromUser, tradeHistory)
                val updatedToUser = calculateNewUserStats(toUser, tradeHistory)
                
                // Update both users
                val fromUserUpdateResult = userRepository.updateUser(updatedFromUser)
                val toUserUpdateResult = userRepository.updateUser(updatedToUser)
                
                fromUserUpdateResult.fold(
                    onSuccess = {
                        toUserUpdateResult.fold(
                            onSuccess = {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    offer = _uiState.value.offer,
                                    successMessage = "Trade completed successfully! 🎉 Users leveled up!"
                                )
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Trade completed but failed to update user stats: ${error.message}"
                                )
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Trade completed but failed to update user stats: ${error.message}"
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Trade completed but failed to load user information"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Trade completed but failed to update user stats: ${e.message}"
            )
        }
    }
    
    private fun calculateNewUserStats(user: User, tradeHistory: TradeHistory): User {
        val newCarbonSaved = user.carbonSaved + tradeHistory.carbonSaved
        val newTradeScore = user.tradeScore + tradeHistory.tradeScoreEarned
        
        // Calculate new level based on trade score
        val newLevel = calculateUserLevel(newTradeScore)
        
        return user.copy(
            tradeScore = newTradeScore,
            level = newLevel,
            carbonSaved = newCarbonSaved,
            lastActive = System.currentTimeMillis()
        )
    }
    
    private fun calculateUserLevel(tradeScore: Int): Int {
        // Level calculation based on trade score
        return when {
            tradeScore >= 1000 -> 10  // Master Trader
            tradeScore >= 800 -> 9    // Expert Trader
            tradeScore >= 600 -> 8    // Advanced Trader
            tradeScore >= 450 -> 7    // Skilled Trader
            tradeScore >= 350 -> 6    // Experienced Trader
            tradeScore >= 250 -> 5    // Competent Trader
            tradeScore >= 150 -> 4    // Intermediate Trader
            tradeScore >= 100 -> 3    // Novice Trader
            tradeScore >= 50 -> 2     // Beginner Trader
            else -> 1                 // New Trader
        }
    }
    
    fun createChatWithUser(onChatCreated: (String) -> Unit) {
        val offer = _uiState.value.offer
        
        if (offer == null) {
            return
        }
        
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    return@launch
                }
                
                // Determine the other user ID
                val otherUserId = if (currentUser.id == offer.fromUserId) {
                    offer.toUserId
                } else {
                    offer.fromUserId
                }
                
                // Create the chat using ChatRepository
                val participantIds = listOf(currentUser.id, otherUserId)
                val result = chatRepository.createChat(
                    participantIds = participantIds,
                    offerId = offer.id
                )
                
                result.fold(
                    onSuccess = { chat ->
                        // Chat created successfully
                        println("Chat created successfully: ${chat.id}")
                        onChatCreated(chat.id) // Use the actual chat ID from the repository
                    },
                    onFailure = { error ->
                        println("Failed to create chat: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                println("Exception creating chat: ${e.message}")
            }
        }
    }
}

data class OfferDetailsUiState(
    val isLoading: Boolean = false,
    val offer: Offer? = null,
    val otherUser: User? = null,
    val requestedItem: Item? = null,
    val offeredItems: List<Item> = emptyList(),
    val isReceivedOffer: Boolean = false,
    val showCounterDialog: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = ""
)
