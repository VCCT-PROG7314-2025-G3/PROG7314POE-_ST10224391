package com.example.swoptrader.ui.screens.meetup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swoptrader.ui.components.AutoTranslatedText
import com.example.swoptrader.ui.components.AutoTranslatedTextBold
import com.example.swoptrader.ui.components.AutoTranslatedTextSmall
import com.example.swoptrader.ui.components.AutoTranslatedTextTitle
import com.example.swoptrader.ui.components.GlobalAutoTranslatedText
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextBold
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextTitle
import com.example.swoptrader.ui.components.LocationPermissionHandler
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

// Simple location data class
data class SimpleLocation(
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLocationPickerScreen(
    initialLocation: SimpleLocation = SimpleLocation(-26.2041, 28.0473), // Johannesburg default
    onLocationSelected: (String, SimpleLocation) -> Unit,
    onBack: () -> Unit,
    viewModel: MapLocationPickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var locationName by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Initialize location when screen loads
    LaunchedEffect(Unit) {
        viewModel.checkLocationPermission()
        viewModel.requestLocation()
    }
    
    // Trigger places search when user types
    LaunchedEffect(locationName) {
        if (locationName.length >= 2) {
            viewModel.searchPlaces(locationName)
        } else {
            viewModel.clearPlacePredictions()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GlobalAutoTranslatedTextTitle(
                        text = "Select Location"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Map Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (uiState.isLoadingLocation) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                GlobalAutoTranslatedText(
                                    text = "Getting your location..."
                                )
                            }
                        }
                    } else if (uiState.cameraPosition != null) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = uiState.cameraPosition!!
                        }
                        
                        // Update camera position when state changes
                        LaunchedEffect(uiState.cameraPosition) {
                            uiState.cameraPosition?.let { position ->
                                cameraPositionState.position = position
                            }
                        }
                        
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng ->
                                viewModel.onLocationSelected(latLng)
                                locationName = "Selected Location"
                            }
                        ) {
                            // Show current location marker
                            uiState.currentLocation?.let { location ->
                                Marker(
                                    state = MarkerState(position = location),
                                    title = "Your Location",
                                    snippet = "Current position"
                                )
                            }
                            
                            // Show selected location marker
                            uiState.selectedLocation?.let { location ->
                                Marker(
                                    state = MarkerState(position = location),
                                    title = "Selected Location",
                                    snippet = "Tap to confirm"
                                )
                            }
                        }
                    } else {
                        // Show map with default location when no user location available
                        val defaultLocation = LatLng(-26.2041, 28.0473) // Johannesburg
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
                        }
                        
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng ->
                                viewModel.onLocationSelected(latLng)
                                locationName = "Selected Location"
                            }
                        ) {
                            // Show selected location marker
                            uiState.selectedLocation?.let { location ->
                                Marker(
                                    state = MarkerState(position = location),
                                    title = "Selected Location",
                                    snippet = "Tap to confirm"
                                )
                            }
                        }
                    }
                }
            }
            
            // Location Input Section
            item {
                Column {
                    Text(
                        text = "Search Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { GlobalAutoTranslatedText("Enter location name") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { GlobalAutoTranslatedText("e.g., Central Park, Coffee Shop") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (locationName.isNotEmpty()) {
                                IconButton(onClick = { 
                                    locationName = ""
                                    viewModel.clearPlacePredictions()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        }
                    )
                    
                    // Google Places Suggestions
                    if (uiState.placePredictions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Suggestions:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        uiState.placePredictions.take(5).forEach { prediction ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable { 
                                        viewModel.selectPlace(prediction)
                                        locationName = prediction.getPrimaryText(null).toString()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = prediction.getPrimaryText(null).toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = prediction.getSecondaryText(null).toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else if (locationName.length >= 2) {
                        // Show fallback message when no predictions found
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No suggestions found. Try typing a more specific location or tap on the map to select a location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Quick Select Section
            item {
                Column {
                    Text(
                        text = "Quick Select",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Generate location-based quick select options
                    val quickLocations = remember(uiState.currentLocation) {
                        uiState.currentLocation?.let { location ->
                            // Determine city based on location
                            val isSouthAfrica = location.latitude in -35.0..-22.0 && location.longitude in 16.0..33.0
                            val isCapeTown = location.latitude in -34.5..-33.0 && location.longitude in 18.0..19.0
                            val isJohannesburg = location.latitude in -26.5..-25.5 && location.longitude in 27.5..28.5
                            
                            when {
                                isCapeTown -> listOf(
                                    "V&A Waterfront, Cape Town" to SimpleLocation(-33.9046, 18.4201),
                                    "Table Mountain, Cape Town" to SimpleLocation(-33.9628, 18.4096),
                                    "Canal Walk, Cape Town" to SimpleLocation(-33.8847, 18.5042),
                                    "Cape Town International Airport" to SimpleLocation(-33.9648, 18.6017)
                                )
                                isJohannesburg -> listOf(
                                    "Sandton City Mall, Johannesburg" to SimpleLocation(-26.1076, 28.0567),
                                    "Melrose Arch, Johannesburg" to SimpleLocation(-26.1167, 28.0500),
                                    "Rosebank Mall, Johannesburg" to SimpleLocation(-26.1467, 28.0433),
                                    "OR Tambo International Airport" to SimpleLocation(-26.1367, 28.2411)
                                )
                                isSouthAfrica -> listOf(
                                    "Nearest Shopping Mall" to SimpleLocation(location.latitude + 0.01, location.longitude + 0.01),
                                    "Nearest Coffee Shop" to SimpleLocation(location.latitude - 0.01, location.longitude + 0.01),
                                    "Nearest Train Station" to SimpleLocation(location.latitude + 0.01, location.longitude - 0.01),
                                    "Nearest Park" to SimpleLocation(location.latitude - 0.01, location.longitude - 0.01)
                                )
                                else -> listOf(
                                    "Central Park" to SimpleLocation(location.latitude + 0.01, location.longitude + 0.01),
                                    "Shopping Mall" to SimpleLocation(location.latitude - 0.01, location.longitude + 0.01),
                                    "Coffee Shop" to SimpleLocation(location.latitude + 0.01, location.longitude - 0.01),
                                    "Train Station" to SimpleLocation(location.latitude - 0.01, location.longitude - 0.01)
                                )
                            }
                        } ?: listOf(
                            "Central Park, Johannesburg" to SimpleLocation(-26.2041, 28.0473),
                            "Sandton City Mall" to SimpleLocation(-26.1076, 28.0567),
                            "Coffee Shop, Rosebank" to SimpleLocation(-26.1467, 28.0433),
                            "Park Station" to SimpleLocation(-26.2041, 28.0473)
                        )
                    }
                    
                    quickLocations.forEach { (name, location) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { 
                                    viewModel.onLocationSelected(LatLng(location.latitude, location.longitude))
                                    locationName = name
                                    viewModel.clearPlacePredictions()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.selectedLocation?.latitude == location.latitude && 
                                    uiState.selectedLocation?.longitude == location.longitude) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (uiState.selectedLocation?.latitude == location.latitude && 
                                        uiState.selectedLocation?.longitude == location.longitude) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (uiState.selectedLocation?.latitude == location.latitude && 
                                        uiState.selectedLocation?.longitude == location.longitude) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Confirm Button
            item {
                if (locationName.isNotEmpty()) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        GlobalAutoTranslatedText("Confirm Location")
                    }
                }
            }
            
            // Add some bottom padding for better scrolling
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Permission Handler
    if (uiState.showPermissionRequest) {
        LocationPermissionHandler(
            onPermissionGranted = { viewModel.onPermissionGranted() },
            onPermissionDenied = { viewModel.onPermissionDenied() }
        )
    }
    
    // Error Message
    if (uiState.errorMessage.isNotEmpty()) {
        LaunchedEffect(uiState.errorMessage) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
        
        // Show error message as a snackbar or card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { GlobalAutoTranslatedTextBold("Confirm Location") },
            text = {
                Column {
                    GlobalAutoTranslatedText("You have selected:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = locationName,
                        fontWeight = FontWeight.Bold
                    )
                    uiState.selectedLocation?.let { location ->
                        Text(
                            text = "Lat: ${String.format("%.6f", location.latitude)}, " +
                                    "Lng: ${String.format("%.6f", location.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        uiState.selectedLocation?.let { location ->
                            onLocationSelected(locationName, SimpleLocation(location.latitude, location.longitude))
                        }
                        showConfirmDialog = false
                    }
                ) {
                    GlobalAutoTranslatedText("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    GlobalAutoTranslatedText("Cancel")
                }
            }
        )
    }
}
