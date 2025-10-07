package com.example.swoptrader.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swoptrader.ui.components.BiometricPermissionDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.swoptrader.ui.components.AutoTranslatedText
import com.example.swoptrader.ui.components.AutoTranslatedTextBold
import com.example.swoptrader.ui.components.AutoTranslatedTextSmall
import com.example.swoptrader.ui.components.AutoTranslatedTextTitle
import com.example.swoptrader.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateToFirestoreTest: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
    
    LaunchedEffect(uiState.showLocationPermissionDialog) {
        if (uiState.showLocationPermissionDialog) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            AutoTranslatedTextTitle(
                text = "Settings"
            )
        }
        
        // Notifications Section
        item {
            SettingsSection(
                title = "Notifications",
                icon = Icons.Default.Notifications
            ) {
                NotificationSettings(
                    notificationsEnabled = uiState.notificationsEnabled,
                    onNotificationsToggle = viewModel::toggleNotifications,
                    pushNotifications = uiState.pushNotifications,
                    onPushNotificationsToggle = viewModel::togglePushNotifications,
                    emailNotifications = uiState.emailNotifications,
                    onEmailNotificationsToggle = viewModel::toggleEmailNotifications
                )
            }
        }
        
        // Location Section
        item {
            SettingsSection(
                title = "Location",
                icon = Icons.Default.LocationOn
            ) {
                LocationSettings(
                    locationSharing = uiState.locationSharing,
                    onLocationSharingToggle = viewModel::toggleLocationSharing,
                    tradeRadius = uiState.tradeRadius,
                    onTradeRadiusChange = viewModel::updateTradeRadius
                )
            }
        }
        
        // Language Section
        item {
            SettingsSection(
                title = "Language",
                icon = Icons.Default.Language
            ) {
                LanguageSettings(
                    selectedLanguage = uiState.selectedLanguage,
                    availableLanguages = uiState.availableLanguages,
                    translationEnabled = uiState.translationEnabled,
                    onLanguageSelected = viewModel::selectLanguage,
                    onTranslationToggle = viewModel::toggleTranslation
                )
            }
        }
        
        // Security Section
        item {
            SettingsSection(
                title = "Security",
                icon = Icons.Default.Security
            ) {
                SecuritySettings(
                    biometricEnabled = uiState.biometricEnabled,
                    onBiometricToggle = viewModel::toggleBiometric
                )
            }
        }
        
        
        // Privacy Section
        item {
            SettingsSection(
                title = "Privacy",
                icon = Icons.Default.PrivacyTip
            ) {
                PrivacySettings(
                    profileVisibility = uiState.profileVisibility,
                    onProfileVisibilityToggle = viewModel::toggleProfileVisibility,
                    dataSharing = uiState.dataSharing,
                    onDataSharingToggle = viewModel::toggleDataSharing
                )
            }
        }
        
        // About Section
        item {
            SettingsSection(
                title = "About",
                icon = Icons.Default.Info
            ) {
                AboutSection(
                    carbonSaved = uiState.carbonSaved,
                    totalTrades = uiState.totalTrades,
                    appVersion = uiState.appVersion
                )
            }
        }
        
        // Developer Section
        item {
            SettingsSection(
                title = "Developer",
                icon = Icons.Default.BugReport
            ) {
                DeveloperSettings(
                    onNavigateToFirestoreTest = onNavigateToFirestoreTest
                )
            }
        }
        
        // Logout Section
        item {
            LogoutSection(
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                AutoTranslatedTextBold(
                    text = title
                )
            }
            content()
        }
    }
}


@Composable
private fun LocationSettings(
    locationSharing: Boolean,
    onLocationSharingToggle: (Boolean) -> Unit,
    tradeRadius: Float,
    onTradeRadiusChange: (Float) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SwitchItem(
            title = "Location Sharing",
            subtitle = "Share your location to find nearby trades",
            checked = locationSharing,
            onCheckedChange = onLocationSharingToggle
        )
        
        if (locationSharing) {
            Column {
                AutoTranslatedTextBold(
                    text = "Trade Radius: ${tradeRadius.toInt()} km"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = tradeRadius,
                    onValueChange = onTradeRadiusChange,
                    valueRange = 1f..50f,
                    steps = 48,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun LanguageSettings(
    selectedLanguage: String,
    availableLanguages: List<com.example.swoptrader.service.Language>,
    translationEnabled: Boolean,
    onLanguageSelected: (String) -> Unit,
    onTranslationToggle: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Translation Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                AutoTranslatedTextBold(
                    text = "Auto Translation"
                )
                AutoTranslatedText(
                    text = "Automatically translate content to your selected language"
                )
            }
            Switch(
                checked = translationEnabled,
                onCheckedChange = onTranslationToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        
        // Language Selection
        AutoTranslatedTextBold(
            text = "Select Language"
        )
        
        // Show only first 10 languages to avoid overwhelming UI
        val displayLanguages = availableLanguages.take(10)
        displayLanguages.forEach { language ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedLanguage == language.name,
                    onClick = { onLanguageSelected(language.name) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = language.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        if (availableLanguages.size > 10) {
            Text(
                text = "And ${availableLanguages.size - 10} more languages available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }
}

@Composable
private fun PrivacySettings(
    profileVisibility: Boolean,
    onProfileVisibilityToggle: (Boolean) -> Unit,
    dataSharing: Boolean,
    onDataSharingToggle: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SwitchItem(
            title = "Public Profile",
            subtitle = "Make your profile visible to other users",
            checked = profileVisibility,
            onCheckedChange = onProfileVisibilityToggle
        )
        
        SwitchItem(
            title = "Data Sharing",
            subtitle = "Share anonymous data to improve the app",
            checked = dataSharing,
            onCheckedChange = onDataSharingToggle
        )
    }
}

@Composable
private fun AboutSection(
    carbonSaved: Double,
    totalTrades: Int,
    appVersion: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoTranslatedTextBold(
                text = "App Version"
            )
            Text(
                text = appVersion,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        
        Divider()
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoTranslatedTextBold(
                text = "Carbon Saved"
            )
            Text(
                text = "${carbonSaved.toInt()} kg CO₂",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoTranslatedTextBold(
                text = "Total Trades"
            )
            Text(
                text = totalTrades.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Divider()
        
        TextButton(
            onClick = { /* TODO: Open privacy policy */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Privacy Policy")
        }
        
        TextButton(
            onClick = { /* TODO: Open terms of service */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Terms of Service")
        }
    }
}

@Composable
private fun LogoutSection(
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AutoTranslatedText("Logout")
        }
    }
}

@Composable
private fun SwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            AutoTranslatedTextBold(
                text = title
            )
            AutoTranslatedText(
                text = subtitle
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}


@Composable
private fun SecuritySettings(
    biometricEnabled: Boolean,
    onBiometricToggle: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SwitchItem(
            title = "Biometric Authentication",
            subtitle = "Use fingerprint or face recognition for quick login",
            checked = biometricEnabled,
            onCheckedChange = onBiometricToggle
        )
    }
}

@Composable
private fun DeveloperSettings(
    onNavigateToFirestoreTest: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            onClick = onNavigateToFirestoreTest,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Firestore Test",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AutoTranslatedTextBold(
                        text = "Test Firestore Connection"
                    )
                    AutoTranslatedTextSmall(
                        text = "Test database connectivity and sync"
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NotificationSettings(
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    pushNotifications: Boolean,
    onPushNotificationsToggle: (Boolean) -> Unit,
    emailNotifications: Boolean,
    onEmailNotificationsToggle: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // General Notifications Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AutoTranslatedTextBold(
                    text = "Enable Notifications"
                )
                AutoTranslatedTextSmall(
                    text = "Receive notifications for messages, offers, and updates"
                )
            }
            
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsToggle
            )
        }
        
        // Push Notifications Toggle
        if (notificationsEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AutoTranslatedTextBold(
                        text = "Push Notifications"
                    )
                    AutoTranslatedTextSmall(
                        text = "Receive notifications when app is closed"
                    )
                }
                
                Switch(
                    checked = pushNotifications,
                    onCheckedChange = onPushNotificationsToggle,
                    enabled = notificationsEnabled
                )
            }
        }
        
        // Email Notifications Toggle
        if (notificationsEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AutoTranslatedTextBold(
                        text = "Email Notifications"
                    )
                    AutoTranslatedTextSmall(
                        text = "Receive email notifications for important updates"
                    )
                }
                
                Switch(
                    checked = emailNotifications,
                    onCheckedChange = onEmailNotificationsToggle,
                    enabled = notificationsEnabled
                )
            }
        }
    }
}
