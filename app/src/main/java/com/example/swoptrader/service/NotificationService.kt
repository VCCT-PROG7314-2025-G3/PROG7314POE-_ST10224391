package com.example.swoptrader.service

import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.swoptrader.data.model.Offer
import com.example.swoptrader.data.model.OfferNotificationRequest
import com.example.swoptrader.data.model.RegisterDeviceTokenRequest
import com.example.swoptrader.data.remote.api.SwopTraderApi
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
    private val firestoreRepository: FirestoreRepository,
    private val api: SwopTraderApi
) {

    companion object {
        private const val TAG = "NotificationService"
    }

    suspend fun getFCMToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch FCM token", e)
            null
        }
    }
    
    suspend fun saveFCMTokenToUser() {
        Log.d(TAG, "saveFCMTokenToUser() called")
        val currentUser = authRepository.getCurrentUser()
        val token = getFCMToken()
        
        Log.d(TAG, "Current user: ${currentUser?.id}, Token: ${token?.take(20)}...")
        
        if (currentUser != null && token != null) {
            Log.d(TAG, "Updating FCM token in Firestore for user: ${currentUser.id}")
            firestoreRepository.updateUserFCMToken(currentUser.id, token)
            Log.d(TAG, "Registering token with API for user: ${currentUser.id}")
            registerTokenWithApi(currentUser.id, token)
        } else {
            Log.w(TAG, "Cannot register token: user=${currentUser != null}, token=${token != null}")
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
        
        logNotificationPlaceholder(recipientUserId, notificationData)
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
        
        logNotificationPlaceholder(recipientUserId, notificationData)
    }

    suspend fun sendOfferNotification(
        offer: Offer,
        senderName: String,
        itemName: String?
    ) {
        try {
            val response = api.sendOfferNotification(
                OfferNotificationRequest(
                    offerId = offer.id,
                    recipientUserId = offer.toUserId,
                    senderUserId = offer.fromUserId,
                    senderName = senderName,
                    itemName = itemName,
                    message = offer.message
                )
            )

            if (!response.isSuccessful || response.body()?.success != true) {
                val errorMessage = response.body()?.error?.message ?: response.message()
                Log.w(TAG, "Offer notification API call failed: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dispatch offer notification via API", e)
        }
    }
    
    private suspend fun registerTokenWithApi(userId: String, token: String) {
        try {
            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            Log.d(TAG, "Calling API to register token for userId=$userId, deviceId=$deviceId")
            val response = api.registerDeviceToken(
                RegisterDeviceTokenRequest(
                    userId = userId,
                    token = token,
                    deviceId = deviceId
                )
            )
            
            Log.d(TAG, "Token registration response: code=${response.code()}, success=${response.isSuccessful}")
            
            if (!response.isSuccessful || response.body()?.success != true) {
                val errorMessage = response.body()?.error?.message ?: response.message()
                val errorBody = response.errorBody()?.string()
                Log.w(TAG, "Failed to register device token with API: $errorMessage, body=$errorBody")
            } else {
                Log.d(TAG, "Successfully registered device token with API")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while registering device token with API", e)
        }
    }
    
    private fun logNotificationPlaceholder(
        userId: String,
        notificationData: Map<String, String>
    ) {
        Log.d(TAG, "Notification placeholder for user $userId: $notificationData")
    }
    
    fun requestNotificationPermission() {
        // Request notification permissions
    }
    
    fun areNotificationsEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }
}