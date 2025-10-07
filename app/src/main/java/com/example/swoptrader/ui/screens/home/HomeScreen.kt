package com.example.swoptrader.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.swoptrader.R
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.model.ItemCategory
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
fun HomeScreen(
    onItemClick: (String) -> Unit,
    onListItemClick: () -> Unit,
    onChatClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadItems()
    }
    
    LaunchedEffect(Unit) {
        viewModel.refreshItems()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.swoptrader),
                            contentDescription = "SwopTrader Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        
                        // Thin and light SwopTrader text
                        Text(
                            text = "SwopTrader",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Light
                            ),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                },
                actions = {
                    // Chat icon for messages
                    IconButton(onClick = onChatClick) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Messages",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // Add item icon
                    IconButton(onClick = onListItemClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "List Item",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                modifier = Modifier.height(64.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                        placeholder = { GlobalAutoTranslatedText("Search items...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // Trade Radius Slider
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlobalAutoTranslatedTextBold(
                            text = "Trade Radius: ${uiState.tradeRadiusKm.toInt()}km"
                        )
                        
                        Switch(
                            checked = uiState.tradeRadiusEnabled,
                            onCheckedChange = { viewModel.toggleTradeRadius() }
                        )
                    }
                    
                    if (uiState.tradeRadiusEnabled) {
                        Slider(
                            value = uiState.tradeRadiusKm.toFloat(),
                            onValueChange = { viewModel.updateTradeRadius(it.toDouble()) },
                            valueRange = 1f..50f,
                            steps = 48,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Category Filter
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryChip(
                            category = null,
                            isSelected = uiState.selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) }
                        )
                    }
                    items(ItemCategory.values()) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = uiState.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }
            }
            
            // Suggested Matches
            if (uiState.suggestedMatches.isNotEmpty()) {
                item {
                GlobalAutoTranslatedTextBold(
                    text = "Suggested for You"
                )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.suggestedMatches) { item ->
                            SuggestedItemCard(
                                item = item,
                                onClick = { onItemClick(item.id) }
                            )
                        }
                    }
                }
            }
            
            // All Items
            item {
                GlobalAutoTranslatedTextBold(
                    text = "All Items (${uiState.items.size})"
                )
            }
            
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.errorMessage.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (uiState.items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                GlobalAutoTranslatedTextBold(
                    text = "No items found"
                )
                            GlobalAutoTranslatedText(
                                text = "Be the first to list an item!"
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onListItemClick) {
                                GlobalAutoTranslatedText("List an Item")
                            }
                        }
                    }
                }
            } else {
                items(uiState.items) { item ->
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: ItemCategory?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            GlobalAutoTranslatedText(
                text = category?.displayName ?: "All"
            )
        },
        selected = isSelected,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SuggestedItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = item.images.firstOrNull() ?: "https://via.placeholder.com/200x150",
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                GlobalAutoTranslatedTextBold(
                    text = item.name
                )
                
                GlobalAutoTranslatedText(
                    text = item.category.displayName
                )
                
                GlobalAutoTranslatedText(
                    text = item.condition.displayName
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Distance",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.distance?.let { "%.1f".format(it) } ?: "N/A"}km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = item.images.firstOrNull() ?: "https://via.placeholder.com/100x100",
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                GlobalAutoTranslatedTextBold(
                    text = item.name
                )
                
                GlobalAutoTranslatedText(
                    text = item.description
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlobalAutoTranslatedText(
                        text = item.category.displayName,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    GlobalAutoTranslatedText(
                        text = item.condition.displayName
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Views",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.viewCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Offers",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.pitchCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Distance",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.distance?.let { "%.1f".format(it) } ?: "N/A"}km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}