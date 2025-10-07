package com.example.swoptrader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.swoptrader.service.InAppNotification
import com.example.swoptrader.service.NotificationType
import com.example.swoptrader.ui.components.GlobalAutoTranslatedText
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextBold
import com.example.swoptrader.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationBanner(
    notification: InAppNotification,
    onDismiss: () -> Unit,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onAction?.invoke() },
            colors = CardDefaults.cardColors(
                containerColor = getNotificationColor(notification.type)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification icon
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = "Notification",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Notification content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    GlobalAutoTranslatedTextBold(
                        text = notification.title
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    GlobalAutoTranslatedText(
                        text = notification.message
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    GlobalAutoTranslatedText(
                        text = formatNotificationTime(notification.timestamp)
                    )
                }
                
                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationBannerList(
    notifications: List<InAppNotification>,
    onDismiss: (String) -> Unit,
    onAction: (String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        notifications.forEach { notification ->
            NotificationBanner(
                notification = notification,
                onDismiss = { onDismiss(notification.id) },
                onAction = { onAction(notification.id, notification.action ?: {}) }
            )
        }
    }
}

private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.MESSAGE -> Color(0xFF2196F3) // Blue
        NotificationType.OFFER -> Color(0xFF4CAF50) // Green
        NotificationType.TRADE_UPDATE -> Color(0xFFFF9800) // Orange
        NotificationType.SYSTEM -> Color(0xFF9C27B0) // Purple
    }
}

private fun getNotificationIcon(type: NotificationType) = when (type) {
    NotificationType.MESSAGE -> Icons.Default.Chat
    NotificationType.OFFER -> Icons.Default.SwapHoriz
    NotificationType.TRADE_UPDATE -> Icons.Default.Update
    NotificationType.SYSTEM -> Icons.Default.Info
}

private fun formatNotificationTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            formatter.format(date)
        }
    }
}
