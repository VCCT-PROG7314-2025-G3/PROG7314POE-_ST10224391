package com.example.swoptrader.service

import android.content.Context
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.data.repository.FirestoreRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) {
    
    suspend fun getFCMToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun saveFCMTokenToUser() {
        val currentUser = authRepository.getCurrentUser()
        val token = getFCMToken()
        
        if (currentUser != null && token != null) {
            // Save token to Firestore user document
            firestoreRepository.updateUserFCMToken(currentUser.id, token)
        }
    }
    
    suspend fun sendMessageNotification(
        recipientUserId: String,
        senderName: String,
        messageContent: String,
        chatId: String
    ) {

        val notificationData = mapOf(
            "type" to "message",
            "chatId" to chatId,
            "senderName" to senderName,
            "message" to messageContent,
            "recipientUserId" to recipientUserId
        )
        
        
        // For now, this is a placeholder for the notification sending logic
        sendNotificationToUser(recipientUserId, notificationData)
    }
    
    suspend fun sendOfferNotification(
        recipientUserId: String,
        offerTitle: String,
        offerId: String
    ) {
        val notificationData = mapOf(
            "type" to "offer",
            "offerId" to offerId,
            "title" to "New Offer Received",
            "message" to offerTitle,
            "recipientUserId" to recipientUserId
        )
        
        sendNotificationToUser(recipientUserId, notificationData)
    }
    
    suspend fun sendTradeUpdateNotification(
        recipientUserId: String,
        tradeUpdate: String,
        tradeId: String
    ) {
        val notificationData = mapOf(
            "type" to "trade_update",
            "tradeId" to tradeId,
            "title" to "Trade Update",
            "message" to tradeUpdate,
            "recipientUserId" to recipientUserId
        )
        
        sendNotificationToUser(recipientUserId, notificationData)
    }
    
    private suspend fun sendNotificationToUser(
        userId: String,
        notificationData: Map<String, String>
    ) {
        // This is a placeholder for the actual notification sending logic
        
        // For now, we'll just log the notification data
        println("Would send notification to user $userId: $notificationData")
    }
    
    fun requestNotificationPermission() {
        // Request notification permissions
    }
    
    fun areNotificationsEnabled(): Boolean {
        // Check if notifications are enabled in system settings
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        return notificationManager.areNotificationsEnabled()
    }
}




