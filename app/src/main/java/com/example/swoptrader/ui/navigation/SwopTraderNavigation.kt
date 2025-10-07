package com.example.swoptrader.ui.navigation

/**
 * SwopTrader Navigation System
 * 
 * This file implements the navigation architecture for the SwopTrader Android application
 * using Jetpack Compose Navigation Component. The navigation system follows the Single
 * Activity Architecture pattern recommended by Google for modern Android development.
 * 
 * Key Design Patterns Implemented:
 * - Single Activity Architecture (Google, 2021)
 * - Navigation Component with Type-Safe Arguments (Android Developers, 2023)
 * - Dependency Injection with Hilt (Google, 2022)
 * - State Management with StateFlow (Kotlin, 2023)
 * 
 * References:
 * - Google. (2021). Single Activity Architecture. Android Developers Documentation.
 * - Android Developers. (2023). Navigation Component. Jetpack Compose Guide.
 * - Google. (2022). Dependency Injection with Hilt. Android Developers Guide.
 * - Kotlin. (2023). StateFlow and SharedFlow. Kotlin Coroutines Documentation.
 */

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swoptrader.service.SessionManager
import com.example.swoptrader.ui.screens.auth.LoginScreen
import com.example.swoptrader.ui.screens.home.HomeScreen
import com.example.swoptrader.ui.screens.profile.ProfileScreen
import com.example.swoptrader.ui.screens.settings.SettingsScreen
import com.example.swoptrader.ui.screens.item.ListItemScreen
import com.example.swoptrader.ui.screens.item.ViewItemScreen
import com.example.swoptrader.ui.screens.chat.ChatScreen
import com.example.swoptrader.ui.screens.chat.ChatListScreen
import com.example.swoptrader.ui.screens.test.FirestoreTestScreen
import com.example.swoptrader.ui.screens.offer.OfferDetailsScreen
import com.example.swoptrader.ui.components.NotificationBannerList
import com.example.swoptrader.ui.screens.notifications.InAppNotificationViewModel

/**
 * Main Navigation Composable Function
 * 
 * This function implements the core navigation logic for the SwopTrader application.
 * It follows the MVVM (Model-View-ViewModel) architecture pattern and implements
 * proper state management using Jetpack Compose's state handling mechanisms.
 * 
 * Architecture Patterns Used:
 * - MVVM Pattern (Microsoft, 2020)
 * - Dependency Injection with Hilt (Google, 2022)
 * - State Management with StateFlow (Kotlin, 2023)
 * - Session Management (Android Developers, 2023)
 * 
 * References:
 * - Microsoft. (2020). MVVM Pattern. Microsoft Documentation.
 * - Google. (2022). Dependency Injection with Hilt. Android Developers Guide.
 * - Kotlin. (2023). StateFlow and SharedFlow. Kotlin Coroutines Documentation.
 * - Android Developers. (2023). Session Management. Android Security Best Practices.
 */
@Composable
fun SwopTraderNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Context and dependency injection setup following Hilt best practices (Google, 2022)
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    // ViewModel injection using Hilt for proper lifecycle management (Google, 2022)
    val loginViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.example.swoptrader.ui.screens.auth.LoginViewModel>()
    val loginUiState = loginViewModel.uiState.collectAsState()
    val isLoggedIn = remember { mutableStateOf(sessionManager.isLoggedIn()) }
    val homeViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.example.swoptrader.ui.screens.home.HomeViewModel>()
    val profileViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.example.swoptrader.ui.screens.profile.ProfileViewModel>()
    val notificationViewModel = androidx.hilt.navigation.compose.hiltViewModel<InAppNotificationViewModel>()
    
    // Session state management using LaunchedEffect for side effects (Android Developers, 2023)
    LaunchedEffect(Unit) {
        val currentUser = sessionManager.getCurrentUser()
        isLoggedIn.value = currentUser != null
    }
    
    // Update login state when it changes
    LaunchedEffect(loginUiState.value.isLoggedIn) {
        isLoggedIn.value = loginUiState.value.isLoggedIn
    }
    
    if (isLoggedIn.value) {
        // Main app with bottom navigation
        androidx.compose.material3.Scaffold(
            bottomBar = {
                com.example.swoptrader.ui.components.SwopTraderBottomNavigation(navController)
            },
            modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Main content
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                composable(Screen.Home.route) {
                    com.example.swoptrader.ui.screens.home.HomeScreen(
                        onItemClick = { itemId ->
                            navController.navigate(Screen.ViewItem.createRoute(itemId))
                        },
                        onListItemClick = {
                            navController.navigate(Screen.ListItem.route)
                        },
                        onChatClick = {
                            navController.navigate(Screen.ChatList.route)
                        }
                    )
                }
                
                composable(Screen.ChatList.route) {
                    com.example.swoptrader.ui.screens.chat.ChatListScreen(
                        onChatClick = { chatId ->
                            navController.navigate(Screen.Chat.createRoute(chatId))
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(Screen.Profile.route) {
                    com.example.swoptrader.ui.screens.profile.ProfileScreen(
                        onEditProfile = {
                            // Navigate to edit profile screen
                        },
                        onItemClick = { itemId ->
                            navController.navigate(Screen.ViewItem.createRoute(itemId))
                        },
                        onOfferClick = { offerId ->
                            navController.navigate(Screen.OfferDetails.createRoute(offerId))
                        },
                        onChatClick = {
                            navController.navigate(Screen.ChatList.route)
                        }
                    )
                }
                
                composable(Screen.Settings.route) {
                    com.example.swoptrader.ui.screens.settings.SettingsScreen(
                        onLogout = {
                            // Clear session and set logged out state
                            loginViewModel.logout()
                            isLoggedIn.value = false
                            // No need to navigate - the UI will automatically switch to login screen
                        },
                        onNavigateToFirestoreTest = {
                            navController.navigate(Screen.FirestoreTest.route)
                        }
                    )
                }
                
                composable(Screen.ListItem.route) {
                    com.example.swoptrader.ui.screens.item.ListItemScreen(
                        onItemCreated = {
                            homeViewModel.refreshItems()
                            profileViewModel.refreshProfile()
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(
                    route = Screen.ViewItem.route,
                    arguments = Screen.ViewItem.arguments
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                    com.example.swoptrader.ui.screens.item.ViewItemScreen(
                        itemId = itemId,
                        onPitchOffer = { itemId ->
                            navController.navigate(Screen.PitchOffer.createRoute(itemId))
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(
                    route = Screen.PitchOffer.route,
                    arguments = Screen.PitchOffer.arguments
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                    com.example.swoptrader.ui.screens.offer.PitchOfferScreen(
                        targetItemId = itemId,
                        onOfferSent = { offerId ->
                            navController.navigate(Screen.OfferDetails.createRoute(offerId))
                        },
                        onBack = {
                            navController.popBackStack()
                        },
                        onCreateItem = {
                            navController.navigate(Screen.ListItem.route)
                        }
                    )
                }
                
                composable(
                    route = Screen.OfferDetails.route,
                    arguments = Screen.OfferDetails.arguments
                ) { backStackEntry ->
                    val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
                    com.example.swoptrader.ui.screens.offer.OfferDetailsScreen(
                        offerId = offerId,
                        onBack = {
                            navController.popBackStack()
                        },
                        onChat = { chatId ->
                            navController.navigate(Screen.Chat.createRoute(chatId))
                        },
                        onUserProfile = { userId ->
                            // Navigate to user profile if needed
                        },
                        onMeetup = { offerId ->
                            navController.navigate("meetup_specifications/$offerId")
                        }
                    )
                }
                
                composable(
                    route = Screen.Chat.route,
                    arguments = Screen.Chat.arguments
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                    com.example.swoptrader.ui.screens.chat.ChatScreen(
                        chatId = chatId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToMeetup = { offerId ->
                            navController.navigate("meetup_specifications/$offerId")
                        }
                    )
                }
                
                
                
                composable("meetup_specifications/{offerId}") { backStackEntry ->
                    val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
                    com.example.swoptrader.ui.screens.meetup.MeetupSpecificationsScreen(
                        offerId = offerId,
                        onBack = {
                            navController.popBackStack()
                        },
                        onMeetupConfirmed = {
                            // Navigate back to profile or show success
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(Screen.FirestoreTest.route) {
                    FirestoreTestScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                }
                
                // Notification banners overlay
                val notifications = notificationViewModel.notifications.collectAsState().value
                NotificationBannerList(
                    notifications = notifications,
                    onDismiss = { notificationId ->
                        notificationViewModel.dismissNotification(notificationId)
                    },
                    onAction = { notificationId, action ->
                        action()
                        notificationViewModel.dismissNotification(notificationId)
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    } else {
        // Login screen
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = modifier
        ) {
            composable(Screen.Login.route) {
                com.example.swoptrader.ui.screens.auth.LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn.value = true
                    }
                )
            }
        }
    }
}
