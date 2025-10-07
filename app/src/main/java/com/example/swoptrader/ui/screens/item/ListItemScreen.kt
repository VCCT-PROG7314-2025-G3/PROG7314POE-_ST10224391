package com.example.swoptrader.ui.screens.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.swoptrader.data.model.ItemCategory
import com.example.swoptrader.data.model.ItemCondition
import com.example.swoptrader.ui.components.AutoTranslatedText
import com.example.swoptrader.ui.components.AutoTranslatedTextBold
import com.example.swoptrader.ui.components.AutoTranslatedTextSmall
import com.example.swoptrader.ui.components.GlobalAutoTranslatedText
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextBold
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextTitle
import com.example.swoptrader.ui.components.AutoTranslatedTextTitle
import com.example.swoptrader.ui.components.ImagePickerDialog
import com.example.swoptrader.ui.components.LocationPermissionDialog
import com.example.swoptrader.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItemScreen(
    onItemCreated: () -> Unit,
    viewModel: ListItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.addImage(it.toString())
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
        }
    }
    
    LaunchedEffect(uiState.showLocationPermissionDialog) {
        if (uiState.showLocationPermissionDialog) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    // Refresh location permission when screen is focused
    LaunchedEffect(Unit) {
        viewModel.refreshLocationPermission()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GlobalAutoTranslatedTextTitle(
                        text = "List New Item"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onItemCreated() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Item Name
            OutlinedTextField(
                value = uiState.itemName,
                onValueChange = viewModel::updateItemName,
                label = { GlobalAutoTranslatedText("Item Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.itemNameError.isNotEmpty()
            )
            if (uiState.itemNameError.isNotEmpty()) {
                Text(
                    text = uiState.itemNameError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { GlobalAutoTranslatedText("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                isError = uiState.descriptionError.isNotEmpty()
            )
            if (uiState.descriptionError.isNotEmpty()) {
                Text(
                    text = uiState.descriptionError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Category Selection
            CategorySelection(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )
            
            // Condition Selection
            ConditionSelection(
                selectedCondition = uiState.selectedCondition,
                onConditionSelected = viewModel::selectCondition
            )
            
            // Image Selection
            ImageSelection(
                images = uiState.selectedImages,
                onAddImage = viewModel::showImagePicker,
                onRemoveImage = { index -> 
                    if (index < uiState.selectedImages.size) {
                        viewModel.removeImage(uiState.selectedImages[index])
                    }
                }
            )
            
            // Desired Trades
            DesiredTradesSection(
                desiredTrades = uiState.desiredTrades,
                onAddTrade = viewModel::addDesiredTrade,
                onRemoveTrade = viewModel::removeDesiredTrade
            )
            
            // Location Settings
            LocationSettings(
                useCurrentLocation = uiState.useCurrentLocation,
                hasLocationPermission = uiState.hasLocationPermission,
                onLocationToggle = viewModel::toggleLocation,
                customLocation = uiState.customLocation,
                onLocationChange = viewModel::updateCustomLocation
            )
            
            // Submit Button
            Button(
                onClick = {
                    viewModel.createItem {
                        onItemCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.isFormValid && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    GlobalAutoTranslatedTextBold(
                        text = "List Item"
                    )
                }
            }
            
            // Error Message
            if (uiState.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Location Permission Dialog
        LocationPermissionDialog(
            isVisible = uiState.showLocationPermissionDialog,
            onRequestPermission = {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            },
            onDismiss = viewModel::onLocationPermissionDenied
        )
        
        // Image Picker Dialog
        ImagePickerDialog(
            isVisible = uiState.showImagePicker,
            selectedImages = uiState.selectedImages,
            onAddFromGallery = {
                imagePickerLauncher.launch("image/*")
            },
            onAddFromCamera = {
                imagePickerLauncher.launch("image/*")
            },
            onRemoveImage = viewModel::removeImage,
            onDismiss = viewModel::hideImagePicker
        )
    }
}

@Composable
private fun CategorySelection(
    selectedCategory: ItemCategory?,
    onCategorySelected: (ItemCategory) -> Unit
) {
    Column {
        GlobalAutoTranslatedTextBold(
            text = "Category",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ItemCategory.values()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { GlobalAutoTranslatedText(category.displayName) }
                )
            }
        }
    }
}

@Composable
private fun ConditionSelection(
    selectedCondition: ItemCondition?,
    onConditionSelected: (ItemCondition) -> Unit
) {
    Column {
        GlobalAutoTranslatedTextBold(
            text = "Condition",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ItemCondition.values()) { condition ->
                FilterChip(
                    selected = selectedCondition == condition,
                    onClick = { onConditionSelected(condition) },
                    label = { GlobalAutoTranslatedText(condition.displayName) }
                )
            }
        }
    }
}

@Composable
private fun ImageSelection(
    images: List<String>,
    onAddImage: () -> Unit,
    onRemoveImage: (Int) -> Unit
) {
    Column {
        GlobalAutoTranslatedTextBold(
            text = "Images (${images.size}/5)",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add Image Button
            if (images.size < 5) {
                item {
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { onAddImage() },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Image",
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Add Photo",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            
            // Image Items
            items(images.size) { index ->
                Card(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = images[index],
                            contentDescription = "Item Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        IconButton(
                            onClick = { onRemoveImage(index) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesiredTradesSection(
    desiredTrades: List<String>,
    onAddTrade: (String) -> Unit,
    onRemoveTrade: (Int) -> Unit
) {
    var newTrade by remember { mutableStateOf("") }
    
    Column {
        Text(
            text = "Desired Trades",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTrade,
                onValueChange = { newTrade = it },
                label = { Text("Add desired trade") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    if (newTrade.isNotBlank()) {
                        onAddTrade(newTrade.trim())
                        newTrade = ""
                    }
                },
                enabled = newTrade.isNotBlank()
            ) {
                Text("Add")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(desiredTrades.size) { index ->
                AssistChip(
                    onClick = { onRemoveTrade(index) },
                    label = { Text(desiredTrades[index]) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove"
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun LocationSettings(
    useCurrentLocation: Boolean,
    hasLocationPermission: Boolean,
    onLocationToggle: (Boolean) -> Unit,
    customLocation: String,
    onLocationChange: (String) -> Unit
) {
    Column {
        GlobalAutoTranslatedTextBold(
            text = "Location",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Location permission warning
        if (!hasLocationPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GlobalAutoTranslatedText(
                        text = "Location permission is required to list items. Please grant permission to continue.",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = useCurrentLocation,
                onCheckedChange = onLocationToggle,
                enabled = hasLocationPermission,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                GlobalAutoTranslatedText(
                    text = "Use current location"
                )
                if (!hasLocationPermission) {
                    GlobalAutoTranslatedText(
                        text = "Location permission required"
                    )
                }
            }
        }
        
        if (!useCurrentLocation) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customLocation,
                onValueChange = onLocationChange,
                label = { GlobalAutoTranslatedText("Enter location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = hasLocationPermission
            )
        }
    }
}

