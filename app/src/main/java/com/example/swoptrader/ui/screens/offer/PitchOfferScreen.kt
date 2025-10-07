package com.example.swoptrader.ui.screens.offer

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.swoptrader.data.model.*
import com.example.swoptrader.ui.components.AutoTranslatedText
import com.example.swoptrader.ui.components.AutoTranslatedTextBold
import com.example.swoptrader.ui.components.AutoTranslatedTextSmall
import com.example.swoptrader.ui.components.AutoTranslatedTextTitle
import com.example.swoptrader.ui.components.GlobalAutoTranslatedText
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextBold
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextTitle
import com.example.swoptrader.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitchOfferScreen(
    targetItemId: String,
    onOfferSent: (String) -> Unit,
    onBack: () -> Unit,
    onCreateItem: () -> Unit,
    viewModel: PitchOfferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(targetItemId) {
        viewModel.loadTargetItem(targetItemId)
        viewModel.loadUserItems()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GlobalAutoTranslatedTextTitle(
                        text = "Pitch Offer"
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
            // Target Item Section
            uiState.targetItem?.let { targetItem ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            GlobalAutoTranslatedTextBold(
                                text = "Trading for:"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = targetItem.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = targetItem.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Your Items Section
            item {
                GlobalAutoTranslatedTextBold(
                    text = "Select an item to trade:"
                )
            }
            
            if (uiState.userItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AutoTranslatedTextBold(
                                text = "No items to trade"
                            )
                            AutoTranslatedText(
                                text = "Create a new item to offer"
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onCreateItem() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                GlobalAutoTranslatedText("Create New Item")
                            }
                        }
                    }
                }
            } else {
                // Add "Create New Item" button even when items exist
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                GlobalAutoTranslatedTextBold(
                                    text = "Want to offer something else?"
                                )
                                GlobalAutoTranslatedText(
                                    text = "Create a new item to trade"
                                )
                            }
                            Button(
                                onClick = { onCreateItem() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Create")
                            }
                        }
                    }
                }
                
                // User items list
                items(uiState.userItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.selectItem(item) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.selectedItem?.id == item.id) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.selectedItem?.id == item.id) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Cash Difference Section
            item {
                OutlinedTextField(
                    value = uiState.cashDifference,
                    onValueChange = viewModel::updateCashDifference,
                    label = { GlobalAutoTranslatedText("Cash difference (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("R") },
                    placeholder = { Text("0") }
                )
            }
            
            // Message Section
            item {
                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = viewModel::updateMessage,
                    label = { GlobalAutoTranslatedText("Message to seller") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { GlobalAutoTranslatedText("Hi! I'm interested in trading...") }
                )
            }
            
            // Send Offer Button
            item {
                Button(
                    onClick = {
                        if (uiState.selectedItem != null) {
                            viewModel.sendOffer { offerId ->
                                onOfferSent(offerId)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedItem != null && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Send Offer")
                    }
                }
            }
            
            if (uiState.errorMessage.isNotEmpty()) {
                item {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (uiState.successMessage.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.successMessage,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Create Item Dialog
    if (uiState.showCreateItemDialog) {
        CreateItemDialog(
            onDismiss = { viewModel.hideCreateItemDialog() },
            onItemCreated = { item ->
                viewModel.createAndSelectItem(item)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemDialog(
    onDismiss: () -> Unit,
    onItemCreated: (Item) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ItemCategory?>(null) }
    var selectedCondition by remember { mutableStateOf<ItemCondition?>(null) }
    var selectedImage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val sampleImages = listOf(
        "https://images.unsplash.com/photo-1606983340126-99ab4feaa64a?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=300&fit=crop",
        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=300&fit=crop"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Item") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                
                // Category Dropdown
                var expandedCategory by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        ItemCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
                
                // Condition Dropdown
                var expandedCondition by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCondition,
                    onExpandedChange = { expandedCondition = !expandedCondition }
                ) {
                    OutlinedTextField(
                        value = selectedCondition?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Condition") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCondition) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCondition,
                        onDismissRequest = { expandedCondition = false }
                    ) {
                        ItemCondition.values().forEach { condition ->
                            DropdownMenuItem(
                                text = { Text(condition.name) },
                                onClick = {
                                    selectedCondition = condition
                                    expandedCondition = false
                                }
                            )
                        }
                    }
                }
                
                // Image Selection
                Text(
                    text = "Select an image:",
                    style = MaterialTheme.typography.labelMedium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleImages) { imageUrl ->
                        Card(
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { selectedImage = imageUrl },
                            border = if (selectedImage == imageUrl) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else null
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isBlank() || description.isBlank() || selectedCategory == null || selectedCondition == null || selectedImage.isBlank()) {
                        errorMessage = "Please fill in all fields and select an image"
                    } else {
                        isLoading = true
                        val newItem = Item(
                            id = "item_${System.currentTimeMillis()}",
                            name = itemName,
                            description = description,
                            category = selectedCategory!!,
                            condition = selectedCondition!!,
                            images = listOf(selectedImage),
                            ownerId = "", // Will be set by the ViewModel
                            location = Location(
                                latitude = -26.2041,
                                longitude = 28.0473,
                                address = "Johannesburg, South Africa"
                            ),
                            desiredTrades = emptyList(),
                            isAvailable = true,
                            createdAt = System.currentTimeMillis(),
                            viewCount = 0,
                            pitchCount = 0
                        )
                        onItemCreated(newItem)
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Item")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
