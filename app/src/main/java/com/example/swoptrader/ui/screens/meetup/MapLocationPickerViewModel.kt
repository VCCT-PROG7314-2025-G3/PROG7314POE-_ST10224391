package com.example.swoptrader.ui.screens.meetup

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.service.LocationService
import com.example.swoptrader.service.PlacesService
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapLocationPickerViewModel @Inject constructor(
    private val locationService: LocationService,
    private val placesService: PlacesService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapLocationPickerUiState())
    val uiState: StateFlow<MapLocationPickerUiState> = _uiState.asStateFlow()
    
    init {
        // Initialize with a default location so the map always shows something
        val defaultLocation = LatLng(-26.2041, 28.0473) // Johannesburg
        _uiState.value = _uiState.value.copy(
            currentLocation = defaultLocation,
            cameraPosition = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
        )
    }
    
    fun checkLocationPermission() {
        _uiState.value = _uiState.value.copy(
            hasLocationPermission = locationService.hasLocationPermission()
        )
    }
    
    fun requestLocation() {
        if (!locationService.hasLocationPermission()) {
            _uiState.value = _uiState.value.copy(
                showPermissionRequest = true
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocation = true)
            
            try {
                // Try to get current location first with a timeout
                var location = locationService.getCurrentLocation()
                
                // If current location fails, try last known location
                if (location == null) {
                    location = locationService.getLastKnownLocation()
                }
                
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    _uiState.value = _uiState.value.copy(
                        currentLocation = latLng,
                        cameraPosition = CameraPosition.fromLatLngZoom(latLng, 15f),
                        isLoadingLocation = false,
                        errorMessage = "" // Clear any previous errors
                    )
                } else {
                    // Set a default location (Johannesburg) when user location cannot be obtained
                    val defaultLocation = LatLng(-26.2041, 28.0473) // Johannesburg
                    _uiState.value = _uiState.value.copy(
                        currentLocation = defaultLocation,
                        cameraPosition = CameraPosition.fromLatLngZoom(defaultLocation, 15f),
                        isLoadingLocation = false,
                        errorMessage = "Unable to get your location. Showing default location. Please check your location settings."
                    )
                }
            } catch (e: Exception) {
                // Set a default location (Johannesburg) when there's an error
                val defaultLocation = LatLng(-26.2041, 28.0473) // Johannesburg
                _uiState.value = _uiState.value.copy(
                    currentLocation = defaultLocation,
                    cameraPosition = CameraPosition.fromLatLngZoom(defaultLocation, 15f),
                    isLoadingLocation = false,
                    errorMessage = "Error getting location: ${e.message}. Showing default location. Please check your location settings."
                )
            }
        }
    }
    
    fun onLocationSelected(latLng: LatLng) {
        _uiState.value = _uiState.value.copy(
            selectedLocation = latLng
        )
    }
    
    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            hasLocationPermission = true,
            showPermissionRequest = false
        )
        requestLocation()
    }
    
    fun onPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            showPermissionRequest = false,
            errorMessage = "Location permission denied. You can still search for locations manually."
        )
        // Don't set a default location - let user search manually
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
    
    fun searchPlaces(query: String) {
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(placePredictions = emptyList())
            return
        }
        
        viewModelScope.launch {
            try {
                println("Searching places for: $query")
                val predictions = placesService.getAutocompletePredictions(
                    query = query,
                    location = _uiState.value.currentLocation
                )
                println("Found ${predictions.size} predictions")
                _uiState.value = _uiState.value.copy(placePredictions = predictions)
            } catch (e: Exception) {
                println("Places search error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    placePredictions = emptyList(),
                    errorMessage = "Error searching places: ${e.message}"
                )
            }
        }
    }
    
    fun selectPlace(prediction: AutocompletePrediction) {
        viewModelScope.launch {
            try {
                val place = placesService.getPlaceDetails(prediction.placeId)
                place?.latLng?.let { latLng ->
                    _uiState.value = _uiState.value.copy(
                        selectedLocation = latLng,
                        cameraPosition = CameraPosition.fromLatLngZoom(latLng, 15f),
                        placePredictions = emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error getting place details: ${e.message}"
                )
            }
        }
    }
    
    fun updateCameraPosition(latLng: LatLng) {
        _uiState.value = _uiState.value.copy(
            cameraPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
        )
    }
    
    fun clearPlacePredictions() {
        _uiState.value = _uiState.value.copy(placePredictions = emptyList())
    }
}

data class MapLocationPickerUiState(
    val isLoadingLocation: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val showPermissionRequest: Boolean = false,
    val currentLocation: LatLng? = null,
    val selectedLocation: LatLng? = null,
    val cameraPosition: CameraPosition? = null,
    val placePredictions: List<AutocompletePrediction> = emptyList(),
    val errorMessage: String = ""
)
