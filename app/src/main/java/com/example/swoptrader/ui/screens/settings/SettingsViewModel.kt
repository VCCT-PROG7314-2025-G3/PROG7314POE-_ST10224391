package com.example.swoptrader.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.service.TranslationService
import com.example.swoptrader.service.TranslationStateManager
import com.example.swoptrader.service.TranslationManager
import com.example.swoptrader.service.LocationService
import com.example.swoptrader.service.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val translationService: TranslationService,
    private val translationStateManager: TranslationStateManager,
    private val translationManager: TranslationManager,
    private val locationService: LocationService,
    private val biometricAuthService: com.example.swoptrader.service.BiometricAuthService,
    private val sessionManager: com.example.swoptrader.service.SessionManager,
    private val notificationService: NotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            // Load available languages from TranslationService
            val availableLanguages = translationService.getSupportedLanguages()
            
            // Load settings from SharedPreferences or other storage
            _uiState.value = _uiState.value.copy(
                notificationsEnabled = true,
                pushNotifications = true,
                emailNotifications = false,
                locationSharing = true,
                tradeRadius = 10f,
                selectedLanguage = "English",
                selectedLanguageCode = "en",
                availableLanguages = availableLanguages,
                translationEnabled = true,
                profileVisibility = true,
                dataSharing = true,
                biometricEnabled = sessionManager.isBiometricEnabled(),
                carbonSaved = 45.7,
                totalTrades = 12,
                appVersion = "1.0.0"
            )
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        if (!enabled) {
            _uiState.value = _uiState.value.copy(
                pushNotifications = false,
                emailNotifications = false
            )
        } else {
            // Request notification permission when enabling notifications
            viewModelScope.launch {
                notificationService.requestNotificationPermission()
                notificationService.saveFCMTokenToUser()
            }
        }
    }
    
    fun togglePushNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotifications = enabled)
    }
    
    fun toggleEmailNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(emailNotifications = enabled)
    }
    
    fun toggleLocationSharing(enabled: Boolean) {
        if (enabled && !locationService.hasLocationPermission()) {
            // Request permission when enabling location sharing
            _uiState.value = _uiState.value.copy(
                locationSharing = enabled,
                showLocationPermissionDialog = true
            )
        } else {
            _uiState.value = _uiState.value.copy(locationSharing = enabled)
        }
    }
    
    fun onLocationPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            showLocationPermissionDialog = false
        )
    }
    
    fun onLocationPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            locationSharing = false,
            showLocationPermissionDialog = false
        )
    }
    
    fun dismissLocationPermissionDialog() {
        _uiState.value = _uiState.value.copy(showLocationPermissionDialog = false)
    }
    
    fun updateTradeRadius(radius: Float) {
        _uiState.value = _uiState.value.copy(tradeRadius = radius)
    }
    
    fun selectLanguage(language: String) {
        val languageCode = _uiState.value.availableLanguages.find { it.name == language }?.code ?: "en"
        println("SettingsViewModel: Language selected: $language (code: $languageCode)")
        _uiState.value = _uiState.value.copy(
            selectedLanguage = language,
            selectedLanguageCode = languageCode
        )
        // Update shared state manager
        translationStateManager.setSelectedLanguage(language, languageCode)
        translationManager.setSelectedLanguage(language, languageCode)
    }
    
    fun toggleTranslation(enabled: Boolean) {
        println("SettingsViewModel: Translation toggled: $enabled")
        _uiState.value = _uiState.value.copy(translationEnabled = enabled)
        // Update shared state manager
        translationStateManager.setTranslationEnabled(enabled)
        translationManager.setTranslationEnabled(enabled)
    }
    
    suspend fun translateText(text: String): Result<String> {
        return translationService.translateText(
            text = text,
            targetLanguage = _uiState.value.selectedLanguageCode,
            sourceLanguage = "auto"
        )
    }
    
    fun toggleProfileVisibility(visible: Boolean) {
        _uiState.value = _uiState.value.copy(profileVisibility = visible)
    }
    
    fun toggleDataSharing(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(dataSharing = enabled)
    }
    
    fun toggleBiometric(enabled: Boolean) {
        if (enabled) {
            // Check if biometric authentication is available
            if (biometricAuthService.isBiometricAvailable()) {
                // Save the setting and show biometric permission dialog
                sessionManager.setBiometricEnabled(true)
                _uiState.value = _uiState.value.copy(
                    biometricEnabled = true,
                    showBiometricPermissionDialog = true
                )
            } else {
                // Biometric not available, show error
                _uiState.value = _uiState.value.copy(
                    biometricEnabled = false,
                    errorMessage = "Biometric authentication is not available on this device"
                )
            }
        } else {
            // Disable biometric authentication
            sessionManager.setBiometricEnabled(false)
            _uiState.value = _uiState.value.copy(biometricEnabled = false)
        }
    }
    
    fun onBiometricPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            showBiometricPermissionDialog = false,
            errorMessage = ""
        )
    }
    
    fun onBiometricPermissionDenied() {
        sessionManager.setBiometricEnabled(false)
        _uiState.value = _uiState.value.copy(
            biometricEnabled = false,
            showBiometricPermissionDialog = false,
            errorMessage = "Biometric authentication permission denied"
        )
    }
    
    fun dismissBiometricPermissionDialog() {
        _uiState.value = _uiState.value.copy(showBiometricPermissionDialog = false)
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val pushNotifications: Boolean = true,
    val emailNotifications: Boolean = false,
    val locationSharing: Boolean = true,
    val showLocationPermissionDialog: Boolean = false,
    val tradeRadius: Float = 10f,
    val selectedLanguage: String = "English",
    val selectedLanguageCode: String = "en",
    val availableLanguages: List<com.example.swoptrader.service.Language> = emptyList(),
    val translationEnabled: Boolean = true,
    val profileVisibility: Boolean = true,
    val dataSharing: Boolean = true,
    val biometricEnabled: Boolean = false,
    val showBiometricPermissionDialog: Boolean = false,
    val errorMessage: String = "",
    val carbonSaved: Double = 0.0,
    val totalTrades: Int = 0,
    val appVersion: String = "1.0.0"
)
