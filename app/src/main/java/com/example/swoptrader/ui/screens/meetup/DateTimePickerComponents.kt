package com.example.swoptrader.ui.screens.meetup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(true) }
    
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Date") },
            text = {
                DatePicker(state = datePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            onDateSelected(formatter.format(date))
                        }
                        showDatePicker = false
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDatePicker = false
                    onDismiss() 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )
    var showTimePicker by remember { mutableStateOf(true) }
    
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val amPm = if (hour < 12) "AM" else "PM"
                        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        val timeString = String.format("%d:%02d %s", displayHour, minute, amPm)
                        onTimeSelected(timeString)
                        showTimePicker = false
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showTimePicker = false
                    onDismiss() 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CustomDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column {
                Text("Choose a date for the meetup:")
                Spacer(modifier = Modifier.height(16.dp))
                
                // Get next 7 days
                val calendar = Calendar.getInstance()
                val dateOptions = mutableListOf<String>()
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                
                repeat(7) { i ->
                    calendar.add(Calendar.DAY_OF_MONTH, if (i == 0) 0 else 1)
                    val dateString = formatter.format(calendar.time)
                    val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> "Monday"
                        Calendar.TUESDAY -> "Tuesday"
                        Calendar.WEDNESDAY -> "Wednesday"
                        Calendar.THURSDAY -> "Thursday"
                        Calendar.FRIDAY -> "Friday"
                        Calendar.SATURDAY -> "Saturday"
                        Calendar.SUNDAY -> "Sunday"
                        else -> ""
                    }
                    dateOptions.add("$dayName, $dateString")
                }
                
                dateOptions.forEach { dateOption ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        TextButton(
                            onClick = {
                                onDateSelected(dateOption)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = dateOption,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CustomTimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column {
                Text("Choose a time for the meetup:")
                Spacer(modifier = Modifier.height(16.dp))
                
                // Generate time slots
                val timeSlots = listOf(
                    "9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
                    "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM",
                    "3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM",
                    "6:00 PM", "6:30 PM", "7:00 PM", "7:30 PM", "8:00 PM"
                )
                
                // Group times by period
                val morningTimes = timeSlots.filter { it.contains("AM") }
                val afternoonTimes = timeSlots.filter { it.contains("PM") && it.startsWith("12") }
                val eveningTimes = timeSlots.filter { it.contains("PM") && !it.startsWith("12") }
                
                // Morning
                Text(
                    text = "Morning",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                morningTimes.chunked(3).forEach { timeRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        timeRow.forEach { time ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                TextButton(
                                    onClick = {
                                        onTimeSelected(time)
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        // Fill remaining space if needed
                        repeat(3 - timeRow.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Afternoon
                Text(
                    text = "Afternoon",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                afternoonTimes.chunked(3).forEach { timeRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        timeRow.forEach { time ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                TextButton(
                                    onClick = {
                                        onTimeSelected(time)
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        // Fill remaining space if needed
                        repeat(3 - timeRow.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Evening
                Text(
                    text = "Evening",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                eveningTimes.chunked(3).forEach { timeRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        timeRow.forEach { time ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                TextButton(
                                    onClick = {
                                        onTimeSelected(time)
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        // Fill remaining space if needed
                        repeat(3 - timeRow.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
