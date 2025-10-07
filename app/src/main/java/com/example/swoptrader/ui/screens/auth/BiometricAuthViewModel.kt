package com.example.swoptrader.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.service.BiometricAuthService
import com.example.swoptrader.service.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BiometricAuthViewModel @Inject constructor(
    private val biometricAuthService: BiometricAuthService,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BiometricAuthUiState())
    val uiState: StateFlow<BiometricAuthUiState> = _uiState.asStateFlow()
    
    fun startBiometricAuth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                statusMessage = "Please authenticate with your biometric",
                authResult = BiometricAuthResult.PENDING
            )
            
            // Check if biometric authentication is available and enabled
            if (!biometricAuthService.isBiometricAvailable()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Biometric authentication is not available on this device",
                    authResult = BiometricAuthResult.ERROR
                )
                return@launch
            }
            
            if (!sessionManager.isBiometricEnabled()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Biometric authentication is not enabled",
                    authResult = BiometricAuthResult.ERROR
                )
                return@launch
            }
            
            // Note: The actual biometric prompt will be triggered by the Activity
            // This ViewModel just manages the state
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = "Ready for biometric authentication"
            )
        }
    }
    
    fun onBiometricAuthSuccess() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            statusMessage = "Authentication successful!",
            authResult = BiometricAuthResult.SUCCESS
        )
    }
    
    fun onBiometricAuthFailed() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            statusMessage = "Authentication failed. Please try again.",
            authResult = BiometricAuthResult.FAILED
        )
    }
    
    fun onBiometricAuthError(errorCode: Int, errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            statusMessage = "Authentication error: $errorMessage",
            authResult = BiometricAuthResult.ERROR
        )
    }
}

