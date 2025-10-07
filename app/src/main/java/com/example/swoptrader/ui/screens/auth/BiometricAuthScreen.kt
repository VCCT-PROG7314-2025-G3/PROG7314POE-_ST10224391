package com.example.swoptrader.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swoptrader.service.BiometricAuthService
import com.example.swoptrader.ui.components.GlobalAutoTranslatedText
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextBold
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextTitle
import com.example.swoptrader.ui.theme.*

@Composable
fun BiometricAuthScreen(
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit,
    viewModel: BiometricAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val biometricAuthService = remember { BiometricAuthService(context) }
    
    LaunchedEffect(Unit) {
        viewModel.startBiometricAuth()
        
        // Trigger the actual biometric authentication prompt
        if (biometricAuthService.isBiometricAvailable()) {
            try {
                val activity = context as androidx.fragment.app.FragmentActivity
                val biometricPrompt = biometricAuthService.createBiometricPrompt(
                    activity = activity,
                    onSuccess = {
                        viewModel.onBiometricAuthSuccess()
                    },
                    onError = { errorMessage ->
                        viewModel.onBiometricAuthError(0, errorMessage)
                    },
                    onFailed = {
                        viewModel.onBiometricAuthFailed()
                    }
                )
                
                val promptInfo = biometricAuthService.getBiometricPromptInfo(
                    title = "Biometric Authentication",
                    subtitle = "Use your fingerprint or face recognition to continue"
                )
                
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                viewModel.onBiometricAuthError(0, "Failed to start biometric authentication: ${e.message}")
            }
        } else {
            viewModel.onBiometricAuthError(0, "Biometric authentication not available")
        }
    }
    
    // Handle authentication results
    LaunchedEffect(uiState.authResult) {
        when (uiState.authResult) {
            BiometricAuthResult.SUCCESS -> onAuthSuccess()
            BiometricAuthResult.FAILED -> onAuthFailed()
            BiometricAuthResult.ERROR -> onAuthFailed()
            else -> { /* Do nothing for PENDING */ }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Logo/Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric Authentication",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Title
            GlobalAutoTranslatedTextTitle(
                text = "Welcome Back!"
            )
            
            // Subtitle
            GlobalAutoTranslatedText(
                text = "Use your fingerprint or face recognition to continue"
            )
            
            // Status message
            if (uiState.statusMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (uiState.authResult) {
                            BiometricAuthResult.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                            BiometricAuthResult.FAILED, BiometricAuthResult.ERROR -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = uiState.statusMessage,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = when (uiState.authResult) {
                            BiometricAuthResult.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                            BiometricAuthResult.FAILED, BiometricAuthResult.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Retry button (if authentication failed)
            if (uiState.authResult == BiometricAuthResult.FAILED || uiState.authResult == BiometricAuthResult.ERROR) {
                Button(
                    onClick = { viewModel.startBiometricAuth() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again")
                }
            }
            
            // Fallback to login button
            OutlinedButton(
                onClick = onAuthFailed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Password Instead")
            }
        }
    }
}

enum class BiometricAuthResult {
    PENDING,
    SUCCESS,
    FAILED,
    ERROR
}

data class BiometricAuthUiState(
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val authResult: BiometricAuthResult = BiometricAuthResult.PENDING
)
