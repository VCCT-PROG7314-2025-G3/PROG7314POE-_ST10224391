package com.example.swoptrader.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.model.ChatMessage
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.ChatRepository
import com.example.swoptrader.service.InAppNotification
import com.example.swoptrader.service.InAppNotificationService
import com.example.swoptrader.service.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InAppNotificationViewModel @Inject constructor(
    private val inAppNotificationService: InAppNotificationService,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()
    
    init {
        startListeningForMessages()
    }
    
    private fun startListeningForMessages() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                // Listen for new messages in real-time
                listenForNewMessages(currentUser.id)
            }
        }
    }
    
    private suspend fun listenForNewMessages(userId: String) {
        // In a real implementation, this would use Firestore's real-time listeners
        viewModelScope.launch {
            // to detect new messages and show notifications
        }
    }
    
    fun showMessageNotification(message: ChatMessage, senderName: String) {
        val notification = InAppNotification(
            id = "message_${message.id}",
            title = "New Message from $senderName",
            message = message.content,
            type = NotificationType.MESSAGE,
            action = {
                // Navigate to chat
                // handled by the UI layer
            }
        )
        
        inAppNotificationService.addNotification(notification)
        _notifications.value = inAppNotificationService.notifications
    }
    
    fun showOfferNotification(offerTitle: String) {
        val notification = InAppNotification(
            id = "offer_${System.currentTimeMillis()}",
            title = "New Offer Received",
            message = offerTitle,
            type = NotificationType.OFFER,
            action = {
                // Navigate to offer details
            }
        )
        
        inAppNotificationService.addNotification(notification)
        _notifications.value = inAppNotificationService.notifications
    }
    
    fun dismissNotification(notificationId: String) {
        inAppNotificationService.removeNotification(notificationId)
        _notifications.value = inAppNotificationService.notifications
    }
    
    fun clearAllNotifications() {
        inAppNotificationService.clearAllNotifications()
        _notifications.value = emptyList()
    }
}




