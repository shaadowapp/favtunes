package com.shaadow.tunes.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shaadow.tunes.MainActivity
import com.shaadow.tunes.R
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService : FirebaseMessagingService() {
    
    companion object {
        const val CHANNEL_ID_GENERAL = "general_notifications"
        const val CHANNEL_ID_MUSIC = "music_suggestions"
        const val CHANNEL_ID_ENGAGEMENT = "engagement_reminders"
        const val CHANNEL_ID_MARKETING = "marketing_fun"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM_RECEIVED", "Message received from: ${remoteMessage.from}")
        Log.d("FCM_RECEIVED", "Message data: ${remoteMessage.data}")
        Log.d("FCM_RECEIVED", "Message notification: ${remoteMessage.notification}")
        
        // Always show notification (for both foreground and background)
        val notificationType = remoteMessage.data["type"] ?: "general"
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "FavTunes"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val songId = remoteMessage.data["songId"]
        val playlistId = remoteMessage.data["playlistId"]
        
        Log.d("FCM_RECEIVED", "Showing notification: $title - $body")
        
        // Show notification regardless of app state
        if (title.isNotEmpty()) {
            showNotification(notificationType, title, body, songId, playlistId)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New token received: $token")
        
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit().putString("fcm_token", token).apply()
        
        CoroutineScope(Dispatchers.IO).launch {
            // Send token to backend for targeted notifications
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General app notifications"
                },
                NotificationChannel(
                    CHANNEL_ID_MUSIC,
                    "Music Suggestions",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Personalized music recommendations"
                },
                NotificationChannel(
                    CHANNEL_ID_ENGAGEMENT,
                    "Engagement Reminders",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Gentle reminders to discover new music"
                },
                NotificationChannel(
                    CHANNEL_ID_MARKETING,
                    "Fun Updates",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Witty updates and music humor"
                }
            )
            
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    private fun showNotification(
        type: String,
        title: String,
        body: String,
        songId: String? = null,
        playlistId: String? = null
    ) {
        val channelId = when (type) {
            "music_suggestion" -> CHANNEL_ID_MUSIC
            "engagement" -> CHANNEL_ID_ENGAGEMENT
            "marketing" -> CHANNEL_ID_MARKETING
            else -> CHANNEL_ID_GENERAL
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            songId?.let { putExtra("songId", it) }
            playlistId?.let { putExtra("playlistId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationIcon = when (type) {
            "music_suggestion" -> R.drawable.ic_stat_name
            "engagement" -> R.drawable.heart
            "marketing" -> R.drawable.disc
            else -> R.drawable.ic_stat_name
        }
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(notificationIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}