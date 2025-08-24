package com.shaadow.tunes.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shaadow.tunes.MainActivity
import com.shaadow.tunes.R

/**
 * Simple local notification helper without complex dependencies
 */
class LocalNotificationHelper(private val context: Context) : NotificationDelivery {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val ENGAGEMENT_CHANNEL_ID = "engagement_notifications"
        private const val MUSIC_CHANNEL_ID = "music_suggestions"
        private const val MARKETING_CHANNEL_ID = "marketing_fun"
        
        private const val ENGAGEMENT_NOTIFICATION_ID = 1001
        private const val MUSIC_NOTIFICATION_ID = 1002
        private const val MARKETING_NOTIFICATION_ID = 1003
    }
    
    init {
        createNotificationChannels()
    }
    
    override fun showEngagementNotification(content: NotificationContent) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, ENGAGEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(ENGAGEMENT_NOTIFICATION_ID, notification)
    }
    
    override fun showMusicSuggestion(content: NotificationContent) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, MUSIC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(MUSIC_NOTIFICATION_ID, notification)
    }
    
    override fun showMarketingNotification(content: NotificationContent) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, MARKETING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(MARKETING_NOTIFICATION_ID, notification)
    }
    
    override fun showTrendingNotification(content: NotificationContent) {
        showMusicSuggestion(content) // Reuse music suggestion logic
    }
    
    override fun showPersonalizedSuggestion(content: NotificationContent) {
        showMusicSuggestion(content) // Reuse music suggestion logic
    }
    
    override fun showRatingNotification(content: NotificationContent) {
        showEngagementNotification(content) // Reuse engagement logic
    }
    
    override fun showCustomNotification(content: NotificationContent, pendingIntent: PendingIntent) {
        val notification = NotificationCompat.Builder(context, ENGAGEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(generateNotificationId(), notification)
    }
    
    override fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    override fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    override fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    override fun generateNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    ENGAGEMENT_CHANNEL_ID,
                    "Engagement Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Gentle reminders to discover new music"
                },
                NotificationChannel(
                    MUSIC_CHANNEL_ID,
                    "Music Suggestions",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Personalized music recommendations"
                },
                NotificationChannel(
                    MARKETING_CHANNEL_ID,
                    "Fun Updates",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Witty updates and music humor"
                }
            )
            
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }
}