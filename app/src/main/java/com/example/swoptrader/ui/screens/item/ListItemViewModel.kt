package com.example.swoptrader.ui.screens.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.*
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ItemRepository
import com.example.swoptrader.service.LocationService
import com.example.swoptrader.service.GeocodingService
import com.example.swoptrader.service.ImagePickerService
import com.example.swoptrader.util.DistanceCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val authRepository: AuthRepository,
    private val locationService: LocationService,
    private val geocodingService: GeocodingService,
    private val imagePickerService: ImagePickerService,
    private val firebaseStorageService: com.example.swoptrader.service.FirebaseStorageService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListItemUiState())
    val uiState: StateFlow<ListItemUiState> = _uiState.asStateFlow()
    
    init {
        // Check location permission status on initialization
        checkLocationPermission()
    }
    
    private fun checkLocationPermission() {
        val hasPermission = locationService.hasLocationPermission()
        _uiState.value = _uiState.value.copy(
            hasLocationPermission = hasPermission,
            useCurrentLocation = hasPermission // Auto-enable if permission is granted
        )
    }
    
    fun refreshLocationPermission() {
        checkLocationPermission()
    }
    
    fun updateItemName(name: String) {
        _uiState.value = _uiState.value.copy(
            itemName = name,
            itemNameError = if (name.isBlank()) "Item name is required" else ""
        )
        validateForm()
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            descriptionError = ""
        )
        validateForm()
    }
    
    fun selectCategory(category: ItemCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        validateForm()
    }
    
    fun selectCondition(condition: ItemCondition) {
        _uiState.value = _uiState.value.copy(selectedCondition = condition)
        validateForm()
    }
    
    fun addImage() {
        val currentImages = _uiState.value.selectedImages
        if (currentImages.size < 5) {
            _uiState.value = _uiState.value.copy(
                showImagePicker = true
            )
        }
    }
    
    fun selectImage(imageUrl: String) {
        val currentImages = _uiState.value.selectedImages
        _uiState.value = _uiState.value.copy(
            selectedImages = currentImages + imageUrl,
            showImagePicker = false
        )
    }
    
    fun dismissImagePicker() {
        _uiState.value = _uiState.value.copy(
            showImagePicker = false
        )
    }
    
    fun removeImage(index: Int) {
        val currentImages = _uiState.value.selectedImages.toMutableList()
        if (index in currentImages.indices) {
            currentImages.removeAt(index)
            _uiState.value = _uiState.value.copy(selectedImages = currentImages)
        }
    }
    
    fun addDesiredTrade(trade: String) {
        val currentTrades = _uiState.value.desiredTrades
        if (trade.isNotBlank() && !currentTrades.contains(trade)) {
            _uiState.value = _uiState.value.copy(
                desiredTrades = currentTrades + trade
            )
        }
    }
    
    fun removeDesiredTrade(index: Int) {
        val currentTrades = _uiState.value.desiredTrades.toMutableList()
        if (index in currentTrades.indices) {
            currentTrades.removeAt(index)
            _uiState.value = _uiState.value.copy(desiredTrades = currentTrades)
        }
    }
    
    fun toggleLocation(useCurrent: Boolean) {
        if (useCurrent && !_uiState.value.hasLocationPermission) {
            // Request location permission
            _uiState.value = _uiState.value.copy(showLocationPermissionDialog = true)
        } else {
            _uiState.value = _uiState.value.copy(useCurrentLocation = useCurrent)
        }
    }
    
    fun onLocationPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            hasLocationPermission = true,
            useCurrentLocation = true,
            showLocationPermissionDialog = false
        )
    }
    
    fun onLocationPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            showLocationPermissionDialog = false,
            useCurrentLocation = false
        )
    }
    
    fun dismissLocationPermissionDialog() {
        _uiState.value = _uiState.value.copy(showLocationPermissionDialog = false)
    }
    
    fun updateCustomLocation(location: String) {
        _uiState.value = _uiState.value.copy(customLocation = location)
    }
    
    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.itemName.isNotBlank() &&
                state.selectedCategory != null &&
                state.selectedCondition != null
        
        _uiState.value = _uiState.value.copy(isFormValid = isValid)
    }
    
    fun createItem(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isFormValid) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            
            try {
                val itemLocation = if (state.useCurrentLocation) {
                    // Get current location with geocoding
                    geocodingService.getCurrentLocationWithGeocodingOrFallback()
                } else if (state.customLocation.isNotBlank()) {
                    com.example.swoptrader.data.model.Location(
                        latitude = -33.9249,
                        longitude = 18.4241,
                        address = state.customLocation,
                        city = "Cape Town",
                        country = "South Africa"
                    )
                } else null
                
                val userLocation = geocodingService.getCurrentLocationWithGeocoding()
                val distance = DistanceCalculator.calculateDistanceBetweenLocations(userLocation, itemLocation)
                
                val uploadedImageUrls = mutableListOf<String>()
                for (imageUri in state.selectedImages) {
                    try {
                        val uri = android.net.Uri.parse(imageUri)
                        val uploadResult = firebaseStorageService.uploadImage(uri, "items")
                        uploadResult.fold(
                            onSuccess = { url -> uploadedImageUrls.add(url) },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Failed to upload image: ${error.message}"
                                )
                                return@launch
                            }
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Invalid image URI: ${e.message}"
                        )
                        return@launch
                    }
                }
                
                val newItem = Item(
                    id = "item_${System.currentTimeMillis()}",
                    name = state.itemName,
                    description = state.description,
                    category = state.selectedCategory!!,
                    condition = state.selectedCondition!!,
                    images = uploadedImageUrls, // Use uploaded URLs instead of local URIs
                    ownerId = authRepository.getCurrentUser()?.id ?: "admin_user_001",
                    location = itemLocation,
                    distance = distance,
                    desiredTrades = state.desiredTrades
                )
                
                val result = itemRepository.createItem(newItem)
                result.fold(
                    onSuccess = { item ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = ""
                        )
                        onSuccess()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to create item"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to create item"
                )
            }
        }
    }
    
    fun showImagePicker() {
        _uiState.value = _uiState.value.copy(showImagePicker = true)
    }
    
    fun hideImagePicker() {
        _uiState.value = _uiState.value.copy(showImagePicker = false)
    }
    
    fun addImage(imageUrl: String) {
        val currentImages = _uiState.value.selectedImages
        if (!currentImages.contains(imageUrl)) {
            _uiState.value = _uiState.value.copy(
                selectedImages = currentImages + imageUrl
            )
        }
    }
    
    fun removeImage(imageUrl: String) {
        val currentImages = _uiState.value.selectedImages
        _uiState.value = _uiState.value.copy(
            selectedImages = currentImages.filter { it != imageUrl }
        )
    }
    
    fun canAddMoreImages(): Boolean {
        return _uiState.value.selectedImages.size < 5 // Limit to 5 images
    }
}

data class ListItemUiState(
    val itemName: String = "",
    val itemNameError: String = "",
    val description: String = "",
    val descriptionError: String = "",
    val selectedCategory: ItemCategory? = null,
    val selectedCondition: ItemCondition? = null,
    val selectedImages: List<String> = emptyList(),
    val desiredTrades: List<String> = emptyList(),
    val useCurrentLocation: Boolean = false,
    val customLocation: String = "",
    val hasLocationPermission: Boolean = false,
    val showLocationPermissionDialog: Boolean = false,
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val showImagePicker: Boolean = false
)
