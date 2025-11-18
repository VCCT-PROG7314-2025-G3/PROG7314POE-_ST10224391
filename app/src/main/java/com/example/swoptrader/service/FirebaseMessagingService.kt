package com.example.swoptrader.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.swoptrader.MainActivity
import com.example.swoptrader.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SwopTraderFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FirebaseMsgService"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "onMessageReceived called - app is in foreground")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Data: ${remoteMessage.data}")
        Log.d(TAG, "Notification: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")
        
        // Handle data payload
        val data = remoteMessage.data
        val notificationType = data["type"] ?: "message"
        val chatId = data["chatId"]
        val senderName = data["senderName"] ?: "Someone"
        val messageContent = data["message"] ?: "You have a new message"
        
        when (notificationType) {
            "message" -> {
                sendMessageNotification(
                    title = "New Message from $senderName",
                    body = messageContent,
                    chatId = chatId
                )
            }
            "offer" -> {
                val offerTitle = data["senderName"]?.let { sender ->
                    val item = data["itemName"]?.takeIf { it.isNotBlank() }
                    if (item != null) {
                        "$sender sent you an offer for $item"
                    } else {
                        "$sender sent you an offer"
                    }
                } ?: "New Offer Received"
                val offerBody = messageContent.takeIf { it.isNotBlank() } 
                    ?: data["itemName"]?.let { "New pitch on $it" }
                    ?: "You have a new trade offer"
                
                sendOfferNotification(
                    title = offerTitle,
                    body = offerBody,
                    offerId = data["offerId"]
                )
            }
            "trade_update" -> {
                sendTradeUpdateNotification(
                    title = "Trade Update",
                    body = messageContent
                )
            }
            else -> {
                remoteMessage.notification?.let { notification ->
                    sendNotification(
                        title = notification.title ?: "SwopTrader",
                        body = notification.body ?: "You have a new notification"
                    )
                }
            }
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: ${token.take(20)}...")
        // Send token to server
        sendTokenToServer(token)
    }
    
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(TAG, "onDeletedMessages called - messages were deleted on the server")
    }
    
    private fun sendNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = "swoptrader_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SwopTrader Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for trades, offers, and messages"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(0, notificationBuilder.build())
    }
    
    private fun sendMessageNotification(title: String, body: String, chatId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "chat")
            putExtra("chat_id", chatId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = "swoptrader_messages"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SwopTrader Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(1, notificationBuilder.build())
    }
    
    private fun sendOfferNotification(title: String, body: String, offerId: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "offers")
            offerId?.let { putExtra("offer_id", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = "swoptrader_offers"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SwopTrader Offers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New offer notifications"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(2, notificationBuilder.build())
    }
    
    private fun sendTradeUpdateNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "trades")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = "swoptrader_trades"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SwopTrader Trades",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Trade update notifications"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(3, notificationBuilder.build())
    }
    
    private fun sendTokenToServer(token: String) {
        // This will be handled by NotificationService.saveFCMTokenToUser()
        // which is called when the app starts or when user enables notifications
        // We don't need to do anything here as the token refresh is handled
        // by the NotificationService lifecycle
    }
}
