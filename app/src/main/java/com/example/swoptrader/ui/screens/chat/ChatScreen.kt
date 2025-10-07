package com.example.swoptrader.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swoptrader.data.model.ChatMessage
import com.example.swoptrader.data.model.MessageType
import com.example.swoptrader.data.model.Offer
import com.example.swoptrader.data.model.Item
import com.example.swoptrader.ui.components.AutoTranslatedText
import com.example.swoptrader.ui.components.AutoTranslatedTextBold
import com.example.swoptrader.ui.components.GlobalAutoTranslatedText
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextBold
import com.example.swoptrader.ui.components.TranslationButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMeetup: (String) -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }
    
    // Pass navigation callback to viewModel
    LaunchedEffect(onNavigateToMeetup) {
        viewModel.setNavigateToMeetupCallback(onNavigateToMeetup)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GlobalAutoTranslatedTextBold(
                        text = uiState.otherUser?.name ?: "Chat"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Trade Offer Card (if applicable)
            if (uiState.showOfferActions && uiState.offer != null) {
                TradeOfferCard(
                    offer = uiState.offer!!,
                    requestedItem = uiState.requestedItem,
                    offeredItems = uiState.offeredItems
                )
            }
            
            // Offer Actions (if applicable)
            if (uiState.showOfferActions) {
                OfferActionsCard(
                    onAccept = { viewModel.acceptOffer() },
                    onReject = { viewModel.rejectOffer() }
                )
            }
            
            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = rememberLazyListState(),
                reverseLayout = true
            ) {
                if (uiState.messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GlobalAutoTranslatedText(
                                text = "No messages yet"
                            )
                        }
                    }
                } else {
                    items(uiState.messages.reversed()) { message ->
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = message.senderId == uiState.currentUserId,
                            senderName = if (message.senderId == uiState.currentUserId) null else uiState.otherUser?.name,
                            senderProfilePicture = if (message.senderId == uiState.currentUserId) null else uiState.otherUser?.profileImageUrl
                        )
                    }
                }
            }
            
            // Message Input
            MessageInput(
                message = uiState.currentMessage,
                onMessageChange = viewModel::updateMessage,
                onSendMessage = viewModel::sendMessage,
                isSending = uiState.isLoading
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    senderName: String? = null,
    senderProfilePicture: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromCurrentUser) {
            // Profile picture for incoming messages
            AsyncImage(
                model = senderProfilePicture,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
        ) {
            // Sender name for incoming messages
            if (!isFromCurrentUser && senderName != null) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFromCurrentUser) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        color = if (isFromCurrentUser) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFromCurrentUser) 
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Profile picture for outgoing messages
            AsyncImage(
                model = null, // Current user profile picture - could be loaded from user data
                contentDescription = "Your profile picture",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                GlobalAutoTranslatedText(text = "Type a message...")
            },
            enabled = !isSending
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        FloatingActionButton(
            onClick = onSendMessage,
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun OfferActionsCard(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                GlobalAutoTranslatedTextBold(text = "Accept")
            }
            
            OutlinedButton(
                onClick = onReject,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                GlobalAutoTranslatedTextBold(text = "Reject")
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

@Composable
private fun TradeOfferCard(
    offer: Offer,
    requestedItem: Item?,
    offeredItems: List<Item>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            GlobalAutoTranslatedTextBold(
                text = "Trade Offer"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Requested Item
            if (requestedItem != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Requested item image
                    AsyncImage(
                        model = requestedItem.images.firstOrNull(),
                        contentDescription = "Requested item",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "Requesting:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = requestedItem.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Offered Items
            if (offeredItems.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Offered items images
                    if (offeredItems.size == 1) {
                        // Single item - show one image
                        AsyncImage(
                            model = offeredItems.first().images.firstOrNull(),
                            contentDescription = "Offered item",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Multiple items - show first two images
                        Row {
                            AsyncImage(
                                model = offeredItems.first().images.firstOrNull(),
                                contentDescription = "Offered item 1",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (offeredItems.size > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                AsyncImage(
                                    model = offeredItems[1].images.firstOrNull(),
                                    contentDescription = "Offered item 2",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "Offering:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = offeredItems.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Cash Amount
            if (offer.cashAmount != null && offer.cashAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cash to add: R${String.format("%.2f", offer.cashAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Offer Message
            if (!offer.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = offer.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}