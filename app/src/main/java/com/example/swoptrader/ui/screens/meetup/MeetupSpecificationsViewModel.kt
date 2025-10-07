package com.example.swoptrader.ui.screens.meetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.*
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.data.repository.OfferRepository
import com.example.swoptrader.data.repository.UserRepository
import com.example.swoptrader.data.repository.MeetupRepository
import com.example.swoptrader.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetupSpecificationsViewModel @Inject constructor(
    private val offerRepository: OfferRepository,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val meetupRepository: MeetupRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MeetupSpecificationsUiState())
    val uiState: StateFlow<MeetupSpecificationsUiState> = _uiState.asStateFlow()
    
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
    
    fun selectMeetupType(type: com.example.swoptrader.data.model.MeetupType) {
        _uiState.value = _uiState.value.copy(meetupType = type)
        updateCanProceed()
    }
    
    fun selectLocation(location: String) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
        updateCanProceed()
    }
    
    fun selectDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        updateCanProceed()
    }
    
    fun selectTime(time: String) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
        updateCanProceed()
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }
    
    fun hideDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }
    
    fun showTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = true)
    }
    
    fun hideTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = false)
    }
    
    fun showMapPicker() {
        _uiState.value = _uiState.value.copy(showMapPicker = true)
    }
    
    fun hideMapPicker() {
        _uiState.value = _uiState.value.copy(showMapPicker = false)
    }
    
    fun confirmMeetup(onMeetupConfirmed: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentState = _uiState.value
                val offer = currentState.offer ?: throw Exception("No offer found")
                
                // Parse date and time to create scheduledAt timestamp
                val scheduledAt = parseDateTimeToTimestamp(currentState.selectedDate, currentState.selectedTime)
                
                // Create MeetupLocation from selected location string
                val meetupLocation = MeetupLocation(
                    name = currentState.selectedLocation,
                    address = currentState.selectedLocation,
                    latitude = 0.0, // Will be updated when location is properly selected
                    longitude = 0.0, // Will be updated when location is properly selected
                    type = LocationType.OTHER
                )
                
                // Create meetup record using the proper data model
                val meetup = com.example.swoptrader.data.model.Meetup(
                    id = "meetup_${System.currentTimeMillis()}",
                    offerId = offer.id,
                    participantIds = listOf(offer.fromUserId, offer.toUserId),
                    location = meetupLocation,
                    scheduledAt = scheduledAt,
                    meetupType = currentState.meetupType,
                    status = MeetupStatus.IN_PROGRESS,
                    notes = currentState.notes,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                // Save meetup to repository
                val meetupResult = meetupRepository.createMeetup(meetup)
                meetupResult.fold(
                    onSuccess = { savedMeetup ->
                        // Update the offer with the meetup
                        val updatedOffer = offer.copy(
                            meetup = savedMeetup,
                            status = OfferStatus.ACCEPTED,
                            updatedAt = System.currentTimeMillis()
                        )
                        
                        // Save updated offer
                        val offerResult = offerRepository.updateOffer(updatedOffer)
                        offerResult.fold(
                            onSuccess = {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    successMessage = "Meetup arranged successfully! The other user will be notified."
                                )
                                
                                // Trigger callback after a short delay
                                kotlinx.coroutines.delay(2000)
                                onMeetupConfirmed()
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
                            errorMessage = "Failed to create meetup: ${error.message}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to arrange meetup"
                )
            }
        }
    }
    
    private fun parseDateTimeToTimestamp(date: String, time: String): Long {
        return try {
            // Parse date and time strings to create proper timestamp
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            
            val parsedDate = dateFormat.parse(date)
            val parsedTime = timeFormat.parse(time)
            
            if (parsedDate != null && parsedTime != null) {
                val calendar = java.util.Calendar.getInstance()
                calendar.time = parsedDate
                
                val timeCalendar = java.util.Calendar.getInstance()
                timeCalendar.time = parsedTime
                
                calendar.set(java.util.Calendar.HOUR_OF_DAY, timeCalendar.get(java.util.Calendar.HOUR_OF_DAY))
                calendar.set(java.util.Calendar.MINUTE, timeCalendar.get(java.util.Calendar.MINUTE))
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                
                calendar.timeInMillis
            } else {
                // Fallback to current time + 1 day if parsing fails
                System.currentTimeMillis() + (24 * 60 * 60 * 1000)
            }
        } catch (e: Exception) {
            // Fallback to current time + 1 day if parsing fails
            System.currentTimeMillis() + (24 * 60 * 60 * 1000)
        }
    }
    
    private fun updateCanProceed() {
        val state = _uiState.value
        val canProceed = state.selectedLocation.isNotEmpty() && 
                        state.selectedDate.isNotEmpty() && 
                        state.selectedTime.isNotEmpty()
        _uiState.value = _uiState.value.copy(canProceed = canProceed)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = "")
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}

data class MeetupSpecificationsUiState(
    val isLoading: Boolean = false,
    val offer: Offer? = null,
    val otherUser: User? = null,
    val requestedItem: Item? = null,
    val offeredItems: List<Item> = emptyList(),
    val meetupType: com.example.swoptrader.data.model.MeetupType = com.example.swoptrader.data.model.MeetupType.PICKUP,
    val selectedLocation: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val notes: String = "",
    val canProceed: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showMapPicker: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = ""
)

// Data models for meetup - using the proper data models from com.example.swoptrader.data.model
