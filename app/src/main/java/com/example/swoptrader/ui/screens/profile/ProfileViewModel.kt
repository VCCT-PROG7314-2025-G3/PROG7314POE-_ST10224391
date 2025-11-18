package com.example.swoptrader.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.*
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.data.repository.OfferRepository
import com.example.swoptrader.data.repository.TradeHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val itemRepository: ItemRepository,
    private val offerRepository: OfferRepository,
    private val tradeHistoryRepository: TradeHistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get current user
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _uiState.value = _uiState.value.copy(user = user)
                    
                    // Load user's items
                    loadUserItems(user.id)
                    
                    // Load user's offers
                    loadUserOffers(user.id)
                    
                    // Load trade history
                    loadTradeHistory()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }
    
    private fun loadUserItems(userId: String) {
        viewModelScope.launch {
            try {
                // Get user's items from repository
                val result = itemRepository.getItemsByOwner(userId)
                result.fold(
                    onSuccess = { items ->
                        _uiState.value = _uiState.value.copy(myItems = items)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message ?: "Failed to load items"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to load items"
                )
            }
        }
    }
    
    private fun loadUserOffers(userId: String) {
        viewModelScope.launch {
            try {
                // Sync with Firestore first to get latest offers
                offerRepository.syncOffersWithRemote(userId)
                
                // Use Flow for real-time updates
                offerRepository.getOffersByUserFlow(userId).collect { offers ->
                    val sentOffers = offers.filter { it.fromUserId == userId }
                    val receivedOffers = offers.filter { it.toUserId == userId }
                    
                    _uiState.value = _uiState.value.copy(
                        offers = offers,
                        sentOffers = sentOffers,
                        receivedOffers = receivedOffers,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to load offers",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadTradeHistory() {
        viewModelScope.launch {
            try {
                val user = _uiState.value.user
                if (user != null) {
                    val result = tradeHistoryRepository.getTradeHistoryByUser(user.id)
                    result.fold(
                        onSuccess = { tradeHistory ->
                            _uiState.value = _uiState.value.copy(
                                tradeHistory = tradeHistory,
                                isLoading = false
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Failed to load trade history"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load trade history"
                )
            }
        }
    }
    
    fun refreshProfile() {
        loadProfile()
    }
    
    fun showDeleteConfirmation(item: Item) {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = true,
            itemToDelete = item
        )
    }
    
    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = false,
            itemToDelete = null
        )
    }
    
    fun deleteItem() {
        val itemToDelete = _uiState.value.itemToDelete ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            
            try {
                val result = itemRepository.deleteItem(itemToDelete.id)
                result.fold(
                    onSuccess = {
                        // Remove item from local list
                        val updatedItems = _uiState.value.myItems.filter { it.id != itemToDelete.id }
                        _uiState.value = _uiState.value.copy(
                            myItems = updatedItems,
                            isDeleting = false,
                            showDeleteConfirmation = false,
                            itemToDelete = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            errorMessage = "Failed to delete item: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Failed to delete item: ${e.message}"
                )
            }
        }
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
    
    fun refreshOffers() {
        val user = _uiState.value.user
        if (user != null) {
            viewModelScope.launch {
                try {
                    // Sync offers with Firestore
                    offerRepository.syncOffersWithRemote(user.id)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to refresh offers: ${e.message}"
                    )
                }
            }
        }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val myItems: List<Item> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val sentOffers: List<Offer> = emptyList(),
    val receivedOffers: List<Offer> = emptyList(),
    val tradeHistory: List<TradeHistory> = emptyList(),
    val errorMessage: String = "",
    val showDeleteConfirmation: Boolean = false,
    val itemToDelete: Item? = null,
    val isDeleting: Boolean = false
)
