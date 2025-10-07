package com.example.swoptrader.ui.screens.meetup

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*
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
fun MeetupSpecificationsScreen(
    offerId: String,
    onBack: () -> Unit,
    onMeetupConfirmed: () -> Unit,
    viewModel: MeetupSpecificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(offerId) {
        viewModel.loadOfferDetails(offerId)
    }
    
    // Date Picker Dialog
    DatePickerDialog(
        isVisible = uiState.showDatePicker,
        onDateSelected = viewModel::selectDate,
        onDismiss = viewModel::hideDatePicker
    )
    
    // Time Picker Dialog
    TimePickerDialog(
        isVisible = uiState.showTimePicker,
        onTimeSelected = viewModel::selectTime,
        onDismiss = viewModel::hideTimePicker
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GlobalAutoTranslatedTextTitle(
                        text = "Arrange Meetup"
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
                    // Success Message
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
                                GlobalAutoTranslatedText(
                                    text = "Offer Accepted! Now let's arrange the meetup."
                                )
                            }
                        }
                    }
                    
                    // Trade Summary
                    item {
                        TradeSummaryCard(
                            requestedItem = uiState.requestedItem,
                            offeredItems = uiState.offeredItems,
                            otherUser = uiState.otherUser
                        )
                    }
                    
                    // Meetup Type Selection
                    item {
                        MeetupTypeCard(
                            selectedType = uiState.meetupType,
                            onTypeSelected = viewModel::selectMeetupType
                        )
                    }
                    
                    // Location Selection
                    item {
                        LocationSelectionCard(
                            selectedLocation = uiState.selectedLocation,
                            onLocationSelected = viewModel::selectLocation,
                            meetupType = uiState.meetupType,
                            onOpenMap = { viewModel.showMapPicker() }
                        )
                    }
                    
                    // Date & Time Selection
                    item {
                        DateTimeSelectionCard(
                            selectedDate = uiState.selectedDate,
                            selectedTime = uiState.selectedTime,
                            onDateSelected = viewModel::selectDate,
                            onTimeSelected = viewModel::selectTime,
                            onShowDatePicker = { viewModel.showDatePicker() },
                            onShowTimePicker = { viewModel.showTimePicker() }
                        )
                    }
                    
                    // Additional Notes
                    item {
                        NotesCard(
                            notes = uiState.notes,
                            onNotesChanged = viewModel::updateNotes
                        )
                    }
                    
                    // Action Buttons
                    item {
                        ActionButtonsCard(
                            canProceed = uiState.canProceed,
                            onConfirm = { viewModel.confirmMeetup(onMeetupConfirmed) },
                            onCancel = onBack
                        )
                    }
                }
            }
        }
    }
    
    // Map Picker Dialog
    if (uiState.showMapPicker) {
        MapLocationPickerScreen(
            onLocationSelected = { locationName, simpleLocation ->
                viewModel.selectLocation(locationName)
                viewModel.hideMapPicker()
            },
            onBack = { viewModel.hideMapPicker() }
        )
    }
    
    // Date Picker Dialog
    if (uiState.showDatePicker) {
        MaterialDatePickerDialog(
            onDismiss = { viewModel.hideDatePicker() },
            onDateSelected = { date -> viewModel.selectDate(date) }
        )
    }
    
    // Time Picker Dialog
    if (uiState.showTimePicker) {
        MaterialTimePickerDialog(
            onDismiss = { viewModel.hideTimePicker() },
            onTimeSelected = { time -> viewModel.selectTime(time) }
        )
    }
}

@Composable
private fun TradeSummaryCard(
    requestedItem: Item?,
    offeredItems: List<Item>,
    otherUser: User?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            GlobalAutoTranslatedTextBold(
                text = "Trade Summary"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Other User Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        text = otherUser?.name?.take(1)?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Trading with ${otherUser?.name ?: "Unknown User"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Trade Score: ${otherUser?.tradeScore ?: 0} | Level: ${otherUser?.level ?: 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Items being traded
            GlobalAutoTranslatedTextBold(
                text = "Items:"
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            requestedItem?.let { item ->
                GlobalAutoTranslatedText(
                    text = "• ${item.name} (you want)"
                )
            }
            
            offeredItems.forEach { item ->
                GlobalAutoTranslatedText(
                    text = "• ${item.name} (you're offering)"
                )
            }
        }
    }
}

@Composable
private fun MeetupTypeCard(
    selectedType: com.example.swoptrader.data.model.MeetupType,
    onTypeSelected: (com.example.swoptrader.data.model.MeetupType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            GlobalAutoTranslatedTextBold(
                text = "How would you like to meet?"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pickup Option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTypeSelected(com.example.swoptrader.data.model.MeetupType.PICKUP) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedType == com.example.swoptrader.data.model.MeetupType.PICKUP) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (selectedType == com.example.swoptrader.data.model.MeetupType.PICKUP) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        GlobalAutoTranslatedTextBold(
                            text = "Pickup"
                        )
                        GlobalAutoTranslatedText(
                            text = "Meet at a location"
                        )
                    }
                }
                
                // Delivery Option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTypeSelected(com.example.swoptrader.data.model.MeetupType.DELIVERY) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedType == com.example.swoptrader.data.model.MeetupType.DELIVERY) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (selectedType == com.example.swoptrader.data.model.MeetupType.DELIVERY) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        GlobalAutoTranslatedTextBold(
                            text = "Delivery"
                        )
                        GlobalAutoTranslatedText(
                            text = "Deliver to address"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationSelectionCard(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    meetupType: com.example.swoptrader.data.model.MeetupType,
    onOpenMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            GlobalAutoTranslatedTextBold(
                text = if (meetupType == com.example.swoptrader.data.model.MeetupType.PICKUP) "Meeting Location" else "Delivery Address"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location Display/Input
            if (selectedLocation.isEmpty()) {
                // Show map selection button when no location is selected
                Button(
                    onClick = onOpenMap,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GlobalAutoTranslatedText("Select Location on Map")
                }
            } else {
                // Show selected location with option to change
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedLocation,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap to change location",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onOpenMap) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Location"
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GlobalAutoTranslatedText(
                text = "Use the map to select a precise location for your meetup"
            )
        }
    }
}

@Composable
private fun DateTimeSelectionCard(
    selectedDate: String,
    selectedTime: String,
    onDateSelected: (String) -> Unit,
    onTimeSelected: (String) -> Unit,
    onShowDatePicker: () -> Unit,
    onShowTimePicker: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            GlobalAutoTranslatedTextBold(
                text = "When?"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Selection
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onShowDatePicker() },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedDate.isEmpty()) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (selectedDate.isEmpty()) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedDate.isEmpty()) "Select Date" else selectedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedDate.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                            color = if (selectedDate.isEmpty()) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Time Selection
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onShowTimePicker() },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedTime.isEmpty()) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (selectedTime.isEmpty()) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTime.isEmpty()) "Select Time" else selectedTime,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedTime.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                            color = if (selectedTime.isEmpty()) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesCard(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Additional Notes (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                label = { Text("Any special instructions?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                placeholder = { Text("e.g., I'll be wearing a red jacket, or call when you arrive") }
            )
        }
    }
}

@Composable
private fun ActionButtonsCard(
    canProceed: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cancel Button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                // Confirm Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    enabled = canProceed
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirm Meetup")
                }
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    isVisible: Boolean,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Date",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple date picker using buttons for common dates
                    val today = Calendar.getInstance()
                    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
                    val dayAfterTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }
                    val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
                    
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    
                    val dates = listOf(
                        "Today" to today,
                        "Tomorrow" to tomorrow,
                        "Day After Tomorrow" to dayAfterTomorrow,
                        "Next Week" to nextWeek
                    )
                    
                    dates.forEach { (label, calendar) ->
                        OutlinedButton(
                            onClick = {
                                onDateSelected(dateFormat.format(calendar.time))
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    isVisible: Boolean,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Time",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple time picker using buttons for common times
                    val times = listOf(
                        "09:00", "10:00", "11:00", "12:00",
                        "13:00", "14:00", "15:00", "16:00",
                        "17:00", "18:00", "19:00", "20:00"
                    )
                    
                    times.chunked(3).forEach { timeRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            timeRow.forEach { time ->
                                OutlinedButton(
                                    onClick = {
                                        onTimeSelected(time)
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(time)
                                }
                            }
                            // Fill remaining space if row has less than 3 items
                            repeat(3 - timeRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

enum class MeetupType {
    PICKUP, DELIVERY
}
