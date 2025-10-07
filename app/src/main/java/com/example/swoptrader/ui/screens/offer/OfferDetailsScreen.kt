package com.example.swoptrader.ui.screens.offer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.swoptrader.data.model.*
import com.example.swoptrader.ui.components.GlobalAutoTranslatedText
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextBold
import com.example.swoptrader.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailsScreen(
    offerId: String,
    onBack: () -> Unit,
    onChat: (String) -> Unit,
    onUserProfile: (String) -> Unit,
    onMeetup: (String) -> Unit,
    viewModel: OfferDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(offerId) {
        viewModel.loadOfferDetails(offerId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GlobalAutoTranslatedTextBold(
                        text = "Offer Details"
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                uiState.offer?.let { offer ->
                    // Offer Status
                    item {
                        OfferStatusCard(offer = offer)
                    }
                    
                    // Other User Profile
                    item {
                        OtherUserProfileCard(
                            user = uiState.otherUser,
                            onProfileClick = { onUserProfile(offer.fromUserId) }
                        )
                    }
                    
                    // Items Being Traded
                    item {
                        ItemsTradingCard(
                            requestedItem = uiState.requestedItem,
                            offeredItems = uiState.offeredItems,
                            isReceivedOffer = uiState.isReceivedOffer
                        )
                    }
                    
                    // Offer Message
                    if (!offer.message.isNullOrEmpty()) {
                        item {
                            MessageCard(message = offer.message)
                        }
                    }
                    
                    // Meetup Details (if offer is accepted)
                    if (offer.status == OfferStatus.ACCEPTED) {
                        item {
                            MeetupDetailsCard(offer = offer)
                        }
                        
                        // Complete Trade Button (if meetup is in progress)
                        if (offer.meetup?.status == MeetupStatus.IN_PROGRESS) {
                            item {
                                CompleteTradeCard(
                                    onCompleteTrade = { viewModel.completeTrade() }
                                )
                            }
                        }
                    }
                    
                    // Action Buttons (only for received offers)
                    if (uiState.isReceivedOffer) {
                        item {
                            ActionButtonsCard(
                                offer = offer,
                                onAccept = { viewModel.acceptOffer { onMeetup(offer.id) } },
                                onReject = { viewModel.rejectOffer() },
                                onCounter = { viewModel.showCounterDialog() },
                                onChat = { chatId -> onChat(chatId) },
                                onCreateChat = { offerId -> 
                                    viewModel.createChatWithUser { chatId ->
                                        onChat(chatId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Counter Offer Dialog
    if (uiState.showCounterDialog) {
        CounterOfferDialog(
            onDismiss = { viewModel.hideCounterDialog() },
            onCounter = { message -> viewModel.sendCounterOffer(message) }
        )
    }
}

@Composable
private fun OfferStatusCard(offer: Offer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (offer.status) {
                OfferStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                OfferStatus.ACCEPTED -> MaterialTheme.colorScheme.primaryContainer
                OfferStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                OfferStatus.COUNTERED -> MaterialTheme.colorScheme.secondaryContainer
                OfferStatus.EXPIRED -> MaterialTheme.colorScheme.surfaceVariant
                OfferStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (offer.status) {
                    OfferStatus.PENDING -> Icons.Default.Schedule
                    OfferStatus.ACCEPTED -> Icons.Default.CheckCircle
                    OfferStatus.REJECTED -> Icons.Default.Cancel
                    OfferStatus.COUNTERED -> Icons.Default.SwapHoriz
                    OfferStatus.EXPIRED -> Icons.Default.Schedule
                    OfferStatus.CANCELLED -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = when (offer.status) {
                    OfferStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                    OfferStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
                    OfferStatus.REJECTED -> MaterialTheme.colorScheme.error
                    OfferStatus.COUNTERED -> MaterialTheme.colorScheme.secondary
                    OfferStatus.EXPIRED -> MaterialTheme.colorScheme.onSurfaceVariant
                    OfferStatus.CANCELLED -> MaterialTheme.colorScheme.error
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                GlobalAutoTranslatedTextBold(
                    text = "Status: ${offer.status.name}"
                )
                GlobalAutoTranslatedText(
                    text = "Created: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(offer.createdAt))}"
                )
            }
        }
    }
}

@Composable
private fun OtherUserProfileCard(
    user: User?,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.name?.take(1)?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Trade Score: ${user?.tradeScore ?: 0} | Level: ${user?.level ?: 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap to view profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ItemsTradingCard(
    requestedItem: Item?,
    offeredItems: List<Item>,
    isReceivedOffer: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            GlobalAutoTranslatedTextBold(
                text = "Items Being Traded"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Requested Item (what they want)
            GlobalAutoTranslatedTextBold(
                text = if (isReceivedOffer) "They want:" else "You want:"
            )
            Spacer(modifier = Modifier.height(4.dp))
            requestedItem?.let { item ->
                ItemPreviewCard(item = item)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Offered Items (what they're offering)
            GlobalAutoTranslatedTextBold(
                text = if (isReceivedOffer) "They're offering:" else "You're offering:"
            )
            Spacer(modifier = Modifier.height(4.dp))
            offeredItems.forEach { item ->
                ItemPreviewCard(item = item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ItemPreviewCard(item: Item) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Image
            if (item.images.isNotEmpty()) {
                AsyncImage(
                    model = item.images.first(),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
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
private fun MessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            GlobalAutoTranslatedTextBold(
                text = "Message"
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlobalAutoTranslatedText(
                text = message
            )
        }
    }
}

@Composable
private fun ActionButtonsCard(
    offer: Offer,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCounter: () -> Unit,
    onChat: (String) -> Unit,
    onCreateChat: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Chat Button
            Button(
                onClick = {
                    onCreateChat(offer.id)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat with User")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Accept Button
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
                
                // Counter Button
                OutlinedButton(
                    onClick = onCounter,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Counter")
                }
                
                // Reject Button
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
private fun CounterOfferDialog(
    onDismiss: () -> Unit,
    onCounter: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Counter Offer") },
        text = {
            Column {
                Text("Send a counter offer message:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onCounter(message)
                    onDismiss()
                },
                enabled = message.isNotBlank()
            ) {
                Text("Send Counter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MeetupDetailsCard(offer: Offer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlobalAutoTranslatedTextBold(
                    text = "Meetup Details"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Meetup Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlobalAutoTranslatedText(
                    text = "Status: ${offer.meetup?.status?.displayName ?: "Not specified"}"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlobalAutoTranslatedText(
                    text = "Location: ${offer.meetup?.location?.address ?: "Not specified"}"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date and Time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlobalAutoTranslatedText(
                    text = "When: ${offer.meetup?.scheduledAt?.let { 
                        java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(it))
                    } ?: "Not specified"}"
                )
            }
            
            // Show map if location is available
            offer.meetup?.location?.let { location ->
                Spacer(modifier = Modifier.height(12.dp))
                
                // Real Google Map
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    }
                    
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = latLng),
                            title = location.name,
                            snippet = location.address
                        )
                    }
                }
            }
            
            // Notes
            offer.meetup?.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notes: $notes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompleteTradeCard(
    onCompleteTrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Trade in Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Once you've completed the trade, mark it as done",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCompleteTrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark Trade as Complete")
            }
        }
    }
}
