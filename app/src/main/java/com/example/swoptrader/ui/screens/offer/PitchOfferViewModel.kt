package com.example.swoptrader.ui.screens.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.model.Offer
import com.example.swoptrader.data.model.OfferStatus
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.data.repository.OfferRepository
import com.example.swoptrader.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PitchOfferViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val authRepository: AuthRepository,
    private val offerRepository: OfferRepository,
    private val notificationService: NotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PitchOfferUiState())
    val uiState: StateFlow<PitchOfferUiState> = _uiState.asStateFlow()
    
    fun loadTargetItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = itemRepository.getItemById(itemId)
                result.fold(
                    onSuccess = { item ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            targetItem = item
                        )
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
    
    fun loadUserItems() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val result = itemRepository.getItemsByOwner(currentUser.id)
                    result.fold(
                        onSuccess = { items ->
                            _uiState.value = _uiState.value.copy(
                                userItems = items,
                                selectedItem = _uiState.value.selectedItem
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = error.message ?: "Failed to load your items"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to load your items"
                )
            }
        }
    }
    
    fun selectItem(item: Item) {
        _uiState.value = _uiState.value.copy(
            selectedItem = item,
            errorMessage = ""
        )
    }
    
    fun updateCashDifference(amount: String) {
        _uiState.value = _uiState.value.copy(cashDifference = amount)
    }
    
    fun updateMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }
    
    fun sendOffer(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val selectedItem = state.selectedItem
        val targetItem = state.targetItem
        
        if (selectedItem == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select an item to trade"
            )
            return
        }
        
        if (targetItem == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Target item not found. Please try again."
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingOffer = true, errorMessage = "")
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isSendingOffer = false,
                        errorMessage = "User not logged in"
                    )
                    return@launch
                }
                
                val offer = Offer(
                    id = "offer_${System.currentTimeMillis()}",
                    fromUserId = currentUser.id,
                    toUserId = targetItem.ownerId,
                    requestedItemId = targetItem.id,
                    offeredItemIds = listOf(selectedItem.id),
                    status = OfferStatus.PENDING,
                    message = state.message.ifBlank { "I'd like to trade my ${selectedItem.name} for your ${targetItem.name}" },
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                val result = offerRepository.createOffer(offer)
                result.fold(
                    onSuccess = {
                        viewModelScope.launch {
                            notificationService.sendOfferNotification(
                                offer = offer,
                                senderName = currentUser.name,
                                itemName = targetItem.name
                            )
                        }
                        _uiState.value = _uiState.value.copy(
                            isSendingOffer = false,
                            errorMessage = "",
                            successMessage = "Offer sent successfully! The seller will be notified."
                        )
                        onSuccess(offer.id)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSendingOffer = false,
                            errorMessage = error.message ?: "Failed to send offer"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSendingOffer = false,
                    errorMessage = e.message ?: "Failed to send offer"
                )
            }
        }
    }
    
    fun showCreateItemDialog() {
        _uiState.value = _uiState.value.copy(showCreateItemDialog = true)
    }
    
    fun hideCreateItemDialog() {
        _uiState.value = _uiState.value.copy(showCreateItemDialog = false)
    }
    
    fun createAndSelectItem(item: Item) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val itemWithOwner = item.copy(ownerId = currentUser.id)
                    
                    val result = itemRepository.createItem(itemWithOwner)
                    result.fold(
                        onSuccess = { savedItem ->
                            val currentItems = _uiState.value.userItems.toMutableList()
                            currentItems.add(savedItem)
                            
                            _uiState.value = _uiState.value.copy(
                                userItems = currentItems,
                                selectedItem = savedItem,
                                showCreateItemDialog = false
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = error.message ?: "Failed to create item"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to create item"
                )
            }
        }
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = "")
    }
}

data class PitchOfferUiState(
    val isLoading: Boolean = false,
    val targetItem: Item? = null,
    val userItems: List<Item> = emptyList(),
    val selectedItem: Item? = null,
    val cashDifference: String = "",
    val message: String = "",
    val isFormValid: Boolean = false,
    val isSendingOffer: Boolean = false,
    val offerSent: Boolean = false,
    val showCreateItemDialog: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)
