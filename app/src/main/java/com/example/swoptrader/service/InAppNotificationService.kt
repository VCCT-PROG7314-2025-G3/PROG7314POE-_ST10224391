package com.example.swoptrader.service

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swoptrader.data.model.ChatMessage
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ChatRepository
import com.example.swoptrader.ui.screens.notifications.InAppNotificationViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) {
    
    private val _notifications = mutableStateOf<List<InAppNotification>>(emptyList())
    val notifications: List<InAppNotification> get() = _notifications.value
    
    fun addNotification(notification: InAppNotification) {
        _notifications.value = _notifications.value + notification
    }
    
    fun removeNotification(notificationId: String) {
        _notifications.value = _notifications.value.filter { it.id != notificationId }
    }
    
    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }
    
    suspend fun listenForNewMessages(): Flow<ChatMessage> = flow {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
        }
    }
}

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val action: (() -> Unit)? = null
)

enum class NotificationType {
    MESSAGE,
    OFFER,
    TRADE_UPDATE,
    SYSTEM
}

@Composable
fun InAppNotificationHost(
    snackbarHostState: SnackbarHostState,
    viewModel: InAppNotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    
    LaunchedEffect(notifications) {
        notifications.forEach { notification ->
            snackbarHostState.showSnackbar(
                message = notification.message,
                actionLabel = "View",
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
        }
    }
}



<<<<<<< HEAD
=======

>>>>>>> 3fb9eb7c543f3e71b1d46847563ffe84a3ac480d
