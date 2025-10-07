package com.example.swoptrader.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.model.TradeHistory
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
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onItemClick: (String) -> Unit,
    onOfferClick: (String) -> Unit,
    onChatClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }
    
    // Delete confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { GlobalAutoTranslatedTextBold("Delete Item") },
            text = { 
                GlobalAutoTranslatedText(
                    "Are you sure you want to delete \"${uiState.itemToDelete?.name}\"? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteItem() },
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        GlobalAutoTranslatedText("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteConfirmation() },
                    enabled = !uiState.isDeleting
                ) {
                    GlobalAutoTranslatedText("Cancel")
                }
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Profile Header
        item {
            ProfileHeader(
                user = uiState.user,
                onEditProfile = onEditProfile,
                onRefresh = { viewModel.refreshOffers() }
            )
        }
        
        // Stats Section
        item {
            StatsSection(
                tradeScore = uiState.user?.tradeScore ?: 0,
                level = uiState.user?.level ?: 1,
                carbonSaved = uiState.user?.carbonSaved ?: 0.0
            )
        }
        
        // Messages Section
        item {
            MessagesSection(
                onChatClick = onChatClick
            )
        }
        
        // My Listings Section
        item {
            MyListingsSection(
                items = uiState.myItems,
                onItemClick = onItemClick,
                onDeleteItem = { item -> viewModel.showDeleteConfirmation(item) }
            )
        }
        
        // Sent Offers Section
        item {
            SentOffersSection(
                offers = uiState.sentOffers,
                onOfferClick = onOfferClick
            )
        }
        
        // Received Offers Section
        item {
            ReceivedOffersSection(
                offers = uiState.receivedOffers,
                onOfferClick = onOfferClick
            )
        }
        
        // Trades in Progress Section
        item {
            TradesInProgressSection(
                offers = uiState.sentOffers + uiState.receivedOffers,
                onOfferClick = { offer -> onOfferClick(offer.id) }
            )
        }
        
        // Trade History Section
        item {
            TradeHistorySection(
                tradeHistory = uiState.tradeHistory
            )
        }
        
        // Community Groups Section
        item {
            CommunityGroupsSection()
        }
    }
}

@Composable
private fun ProfileHeader(
    user: com.example.swoptrader.data.model.User?,
    onEditProfile: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = user?.profileImageUrl ?: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face",
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                GlobalAutoTranslatedTextBold(
                    text = user?.name ?: "Loading..."
                )
                        Text(
                            text = user?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (user?.isVerified == true) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                        GlobalAutoTranslatedText(
                            text = "Verified"
                        )
                            }
                        }
                    }
                }
                
                Row {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = onEditProfile) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location
            user?.location?.let { location ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location.address ?: "Unknown location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsSection(
    tradeScore: Int,
    level: Int,
    carbonSaved: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Star,
                label = "Trade Score",
                value = tradeScore.toString()
            )
            StatItem(
                icon = Icons.Default.TrendingUp,
                label = "Level",
                value = level.toString()
            )
            StatItem(
                icon = Icons.Default.Eco,
                label = "Carbon Saved",
                value = "${carbonSaved.toInt()}kg"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MyListingsSection(
    items: List<Item>,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
                GlobalAutoTranslatedTextBold(
                    text = "My Listings"
                )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                GlobalAutoTranslatedTextBold(
                    text = "No items listed yet"
                )
                    GlobalAutoTranslatedText(
                        text = "Tap the + button to list your first item"
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
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
private fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            AsyncImage(
                model = item.images.firstOrNull() ?: "https://images.unsplash.com/photo-1560472354-b33ff0c44a43?w=400&h=300&fit=crop",
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun SentOffersSection(
    offers: List<com.example.swoptrader.data.model.Offer>,
    onOfferClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        GlobalAutoTranslatedTextBold(
            text = "Sent Offers"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (offers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No sent offers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Make offers on items you want to trade",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                offers.forEach { offer ->
                    OfferCard(
                        offer = offer,
                        onClick = { onOfferClick(offer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceivedOffersSection(
    offers: List<com.example.swoptrader.data.model.Offer>,
    onOfferClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        GlobalAutoTranslatedTextBold(
            text = "Received Offers"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (offers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No received offers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Offers from other users will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                offers.forEach { offer ->
                    OfferCard(
                        offer = offer,
                        onClick = { onOfferClick(offer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OfferCard(
    offer: com.example.swoptrader.data.model.Offer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when (offer.status) {
                com.example.swoptrader.data.model.OfferStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                com.example.swoptrader.data.model.OfferStatus.ACCEPTED -> MaterialTheme.colorScheme.primaryContainer
                com.example.swoptrader.data.model.OfferStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                com.example.swoptrader.data.model.OfferStatus.COUNTERED -> MaterialTheme.colorScheme.secondaryContainer
                com.example.swoptrader.data.model.OfferStatus.EXPIRED -> MaterialTheme.colorScheme.surfaceVariant
                com.example.swoptrader.data.model.OfferStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlobalAutoTranslatedTextBold(
                    text = "Offer #${offer.id.replace("offer_", "")}"
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress icon for in-progress trades
                    if (offer.meetup != null && offer.meetup.status == com.example.swoptrader.data.model.MeetupStatus.IN_PROGRESS) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Trade in progress",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    GlobalAutoTranslatedText(
                        text = offer.status.displayName
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GlobalAutoTranslatedText(
                text = offer.message ?: "No message"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GlobalAutoTranslatedText(
                text = "Created: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(offer.createdAt))}"
            )
        }
    }
}

@Composable
private fun TradeHistorySection(
    tradeHistory: List<TradeHistory>
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
                GlobalAutoTranslatedTextBold(
                    text = "Trade History"
                )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (tradeHistory.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                GlobalAutoTranslatedTextBold(
                    text = "No trades yet"
                )
                    GlobalAutoTranslatedText(
                        text = "Your completed trades will appear here"
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tradeHistory.forEach { trade ->
                    TradeHistoryCard(trade = trade)
                }
            }
        }
    }
}

@Composable
private fun TradeHistoryCard(
    trade: TradeHistory
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            GlobalAutoTranslatedTextBold(
                text = "Trade #${trade.id.replace("trade_", "")}"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GlobalAutoTranslatedText(
                text = "Completed: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(trade.completedAt))}"
            )
        }
    }
}

@Composable
private fun CommunityGroupsSection() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        GlobalAutoTranslatedTextBold(
            text = "Community Groups"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val groups = listOf(
            "Electronics Traders",
            "Book Lovers",
            "Fashion Exchange",
            "Local Traders"
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { group ->
                FilterChip(
                    onClick = { /* TODO: Join group */ },
                    label = { GlobalAutoTranslatedText(group) },
                    selected = false
                )
            }
        }
    }
}

@Composable
private fun MyListingsSection(
    items: List<Item>,
    onItemClick: (String) -> Unit,
    onDeleteItem: (Item) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        GlobalAutoTranslatedTextBold(
            text = "My Listings"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlobalAutoTranslatedText(
                        text = "No items listed yet"
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    MyItemCard(
                        item = item,
                        onItemClick = { onItemClick(item.id) },
                        onDeleteClick = { onDeleteItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyItemCard(
    item: Item,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Item Image
            AsyncImage(
                model = item.images.firstOrNull(),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                GlobalAutoTranslatedTextBold(
                    text = item.name
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                GlobalAutoTranslatedText(
                    text = item.category.displayName
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Delete Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagesSection(
    onChatClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        GlobalAutoTranslatedTextBold(
            text = "Messages"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChatClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Messages",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    GlobalAutoTranslatedTextBold(
                        text = "View Messages"
                    )
                    
                    GlobalAutoTranslatedText(
                        text = "Check your conversations and reply to messages"
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go to Messages",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TradesInProgressSection(
    offers: List<com.example.swoptrader.data.model.Offer>,
    onOfferClick: (com.example.swoptrader.data.model.Offer) -> Unit
) {
    // Filter offers that have meetups in progress
    val tradesInProgress = offers.filter { offer ->
        offer.meetup != null && offer.meetup.status == com.example.swoptrader.data.model.MeetupStatus.IN_PROGRESS
    }
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            GlobalAutoTranslatedTextBold(
                text = "Trades in Progress"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (tradesInProgress.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GlobalAutoTranslatedText(
                        text = "No trades in progress"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "When you arrange meetups, they'll appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            tradesInProgress.forEach { offer ->
                TradeInProgressCard(
                    offer = offer,
                    onClick = { onOfferClick(offer) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TradeInProgressCard(
    offer: com.example.swoptrader.data.model.Offer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Trade in progress",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GlobalAutoTranslatedTextBold(
                        text = "Trade #${offer.id.replace("offer_", "")}"
                    )
                }
                
                Text(
                    text = "In Progress",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Meetup details
            offer.meetup?.let { meetup ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = meetup.location.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault()).format(java.util.Date(meetup.scheduledAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = offer.message ?: "No message",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}