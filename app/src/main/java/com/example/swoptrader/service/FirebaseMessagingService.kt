package com.example.swoptrader.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.swoptrader.MainActivity
import com.example.swoptrader.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SwopTraderFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
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
                sendOfferNotification(
                    title = "New Offer Received",
                    body = messageContent
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
        // Send token to server
        sendTokenToServer(token)
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
    
    private fun sendOfferNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "offers")
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
        // Send token to server through API service
    }
}
