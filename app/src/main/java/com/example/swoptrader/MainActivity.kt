package com.example.swoptrader

/**
 * MainActivity - Android Application Entry Point
 * 
 * This class serves as the main entry point for the SwopTrader Android application,
 * implementing the Single Activity Architecture pattern recommended by Google for
 * modern Android development. It demonstrates proper integration of Jetpack Compose,
 * dependency injection, and security features.
 * 
 * Key Android Architecture Concepts:
 * - Single Activity Architecture (Google, 2021)
 * - Jetpack Compose UI Framework (Google, 2023)
 * - Dependency Injection with Hilt (Google, 2022)
 * - Biometric Authentication (Android Developers, 2023)
 * - Edge-to-Edge Display (Google, 2023)
 * - Navigation Component Integration (Android Developers, 2023)
 * 
 * Security Features:
 * - Biometric authentication integration
 * - Secure session management
 * - Edge-to-edge display with proper insets handling
 * 
 * References:
 * - Google. (2021). Single Activity Architecture. Android Developers Documentation.
 * - Google. (2023). Jetpack Compose. Android Developers Guide.
 * - Google. (2022). Dependency Injection with Hilt. Android Developers Guide.
 * - Android Developers. (2023). Biometric Authentication. Android Security Best Practices.
 * - Google. (2023). Edge-to-Edge Display. Android Developers Guide.
 * - Android Developers. (2023). Navigation Component. Jetpack Compose Guide.
 */

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.swoptrader.service.BiometricAuthService
import com.example.swoptrader.service.SessionManager
import com.example.swoptrader.service.NotificationService
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.example.swoptrader.ui.navigation.SwopTraderNavigation
import com.example.swoptrader.ui.screens.auth.BiometricAuthScreen
import com.example.swoptrader.ui.screens.auth.BiometricAuthResult
import com.example.swoptrader.ui.theme.SwopTraderTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle notification click intents
        handleNotificationIntent(intent)
        
        setContent {
            SwopTraderTheme {
                SwopTraderApp()
            }
        }
    }
    
    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        
        val navigateTo = intent.getStringExtra("navigate_to")
        val offerId = intent.getStringExtra("offer_id")
        val chatId = intent.getStringExtra("chat_id")
        
        if (navigateTo != null) {
            android.util.Log.d("MainActivity", "Notification intent received: navigate_to=$navigateTo, offerId=$offerId, chatId=$chatId")
            // Navigation will be handled by SwopTraderApp composable
            // Store the navigation target in a way that SwopTraderApp can access it
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationServiceEntryPoint {
    fun notificationService(): NotificationService
}

@Composable
fun SwopTraderApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Check if user is signed in and biometric is enabled
    val sessionManager = remember { SessionManager(context) }
    val biometricAuthService = remember { BiometricAuthService(context) }
    val notificationService = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationServiceEntryPoint::class.java
        ).notificationService()
    }
    
    val isSignedIn = remember { mutableStateOf(sessionManager.isLoggedIn()) }
    val isBiometricEnabled = remember { mutableStateOf(sessionManager.isBiometricEnabled()) }
    val showBiometricAuth = remember { mutableStateOf(false) }
    val biometricAuthCompleted = remember { mutableStateOf(false) }
    
    // Check if we should show biometric authentication
    LaunchedEffect(Unit) {
        val currentUser = sessionManager.getCurrentUser()
        val loggedIn = currentUser != null
        val biometricEnabled = sessionManager.isBiometricEnabled()
        
        isSignedIn.value = loggedIn
        isBiometricEnabled.value = biometricEnabled
        
        // Register FCM token if user is already logged in
        if (loggedIn) {
            scope.launch {
                notificationService.saveFCMTokenToUser()
            }
        }
        
        if (loggedIn && biometricEnabled && biometricAuthService.isBiometricAvailable()) {
            showBiometricAuth.value = true
        } else {
            biometricAuthCompleted.value = true
        }
    }
    
    if (showBiometricAuth.value && !biometricAuthCompleted.value) {
        BiometricAuthScreen(
            onAuthSuccess = {
                biometricAuthCompleted.value = true
                showBiometricAuth.value = false
            },
            onAuthFailed = {
                // If biometric auth fails, just show login screen without clearing session
                showBiometricAuth.value = false
                biometricAuthCompleted.value = true
            }
        )
    } else if (biometricAuthCompleted.value) {
        SwopTraderNavigation(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }
}