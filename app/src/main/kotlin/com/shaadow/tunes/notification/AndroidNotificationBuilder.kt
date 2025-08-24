package com.shaadow.tunes.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.shaadow.tunes.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Android-specific implementation of NotificationBuilder
 * Handles the technical aspects of creating Android notifications
 */
class AndroidNotificationBuilder(
    private val context: Context
) : NotificationBuilder {
    
    override fun buildBasicNotification(
        content: NotificationContent,
        channelId: String,
        pendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("${content.emoji} ${content.title}")
            .setContentText(content.body)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
    
    override fun buildBigTextNotification(
        content: NotificationContent,
        channelId: String,
        pendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("${content.emoji} ${content.title}")
            .setContentText(content.body)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content.body)
                    .setBigContentTitle("${content.emoji} ${content.title}")
            )
            .build()
    }
    
    override fun buildBigPictureNotification(
        content: NotificationContent,
        channelId: String,
        pendingIntent: PendingIntent,
        imageUrl: String?
    ): Notification {
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("${content.emoji} ${content.title}")
            .setContentText(content.body)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        // Use provided imageUrl or content.imageUrl
        val finalImageUrl = imageUrl ?: (content as? NotificationContent)?.let { 
            // Try to get imageUrl if it exists in the content object
            try { 
                content::class.java.getDeclaredField("imageUrl").get(content) as? String 
            } catch (e: Exception) { 
                null 
            }
        }
        
        if (finalImageUrl != null) {
            // In a real implementation, you'd load the image asynchronously
            // For now, we'll use a placeholder or default image
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(getDefaultMusicBitmap())
                    .setBigContentTitle("${content.emoji} ${content.title}")
                    .setSummaryText(content.body)
            )
        } else {
            // Fallback to big text style if no image
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content.body)
                    .setBigContentTitle("${content.emoji} ${content.title}")
            )
        }
        
        return builder.build()
    }
    
    override fun buildActionNotification(
        content: NotificationContent,
        channelId: String,
        primaryAction: PendingIntent,
        secondaryAction: NotificationAction?
    ): Notification {
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("${content.emoji} ${content.title}")
            .setContentText(content.body)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(primaryAction)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        // Add secondary action if provided
        secondaryAction?.let { action ->
            builder.addAction(
                action.icon,
                action.title,
                action.pendingIntent
            )
        }
        
        return builder.build()
    }
    
    /**
     * Get default music-related bitmap for notifications
     * In a real implementation, this would load album art or app icon
     */
    private fun getDefaultMusicBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_stat_name)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Load bitmap from URL asynchronously
     * This is a simplified implementation - in production you'd use proper image loading
     */
    private suspend fun loadBitmapFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection()
                connection.doInput = true
                connection.connect()
                val inputStream = connection.getInputStream()
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                null
            }
        }
    }
}