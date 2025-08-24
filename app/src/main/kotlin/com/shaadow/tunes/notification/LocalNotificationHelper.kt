package com.shaadow.tunes.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.shaadow.tunes.MainActivity
import com.shaadow.tunes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.SupervisorJob
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL

/**
 * Refactored LocalNotificationHelper implementing NotificationDelivery interface
 * Focuses solely on notification delivery, with content generation separated
 * Now includes intelligent scheduling for optimal user engagement
 */
class LocalNotificationHelper(
    private val context: Context,
    private val notificationBuilder: NotificationBuilder = AndroidNotificationBuilder(context),
    private val errorHandler: NotificationErrorHandler = DefaultNotificationErrorHandler(),
    private val scheduler: IntelligentNotificationScheduler = IntelligentNotificationScheduler(context),
    private val throttleManager: NotificationThrottleManager = NotificationThrottleManager(context)
) : NotificationDelivery {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val prefs = context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE)
    private val RATING_MIN_INTERVAL = 7 * 24 * 60 * 60 * 1000L // 7 days in ms
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Implementation of NotificationDelivery interface
    
    override fun showEngagementNotification(content: NotificationContent) {
        scope.launch {
            try {
                val scheduleResult = scheduler.scheduleNotification(
                    content = content,
                    priority = NotificationPriority.NORMAL
                )
                
                when (scheduleResult) {
                    is ScheduleResult.DeliveredImmediately -> {
                        deliverEngagementNotificationNow(content)
                    }
                    is ScheduleResult.ScheduledForLater -> {
                        // Notification scheduled for optimal time
                        errorHandler.logSchedulingInfo("Engagement notification scheduled for ${scheduleResult.deliveryTime}")
                    }
                    is ScheduleResult.Blocked -> {
                        errorHandler.logSchedulingInfo("Engagement notification blocked: ${scheduleResult.reason}")
                    }
                }
            } catch (e: Exception) {
                errorHandler.handleDeliveryError(e, content)
            }
        }
    }
    
    private fun deliverEngagementNotificationNow(content: NotificationContent) {
        try {
            val pendingIntent = PendingIntent.getActivity(
                context, 0, createMainActivityIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = notificationBuilder.buildActionNotification(
                content = content,
                channelId = NotificationService.CHANNEL_ID_ENGAGEMENT,
                primaryAction = pendingIntent,
                secondaryAction = NotificationAction(
                    icon = R.drawable.play,
                    title = content.actionText,
                    pendingIntent = pendingIntent
                )
            )
            
            notificationManager.notify(generateNotificationId(), notification)
            errorHandler.logNotificationMetrics(content, true)
            
            // Record delivery for behavior learning
            scheduler.recordNotificationInteraction(
                content.contentType,
                System.currentTimeMillis(),
                NotificationInteraction.CLICKED // Will be updated based on actual user interaction
            )
        } catch (e: Exception) {
            errorHandler.handleDeliveryError(e, content)
        }
    }
    
    override fun showMusicSuggestion(content: NotificationContent) {
        scope.launch {
            try {
                val scheduleResult = scheduler.scheduleNotification(
                    content = content,
                    priority = NotificationPriority.NORMAL,
                    deliveryWindow = DeliveryWindow(8, 22) // Music suggestions during active hours
                )
                
                when (scheduleResult) {
                    is ScheduleResult.DeliveredImmediately -> {
                        deliverMusicSuggestionNow(content)
                    }
                    is ScheduleResult.ScheduledForLater -> {
                        errorHandler.logSchedulingInfo("Music suggestion scheduled for ${scheduleResult.deliveryTime}")
                    }
                    is ScheduleResult.Blocked -> {
                        errorHandler.logSchedulingInfo("Music suggestion blocked: ${scheduleResult.reason}")
                    }
                }
            } catch (e: Exception) {
                errorHandler.handleDeliveryError(e, content)
            }
        }
    }
    
    private fun deliverMusicSuggestionNow(content: NotificationContent) {
        try {
            val intent = createMainActivityIntent().apply {
                content.songId?.let { putExtra("songId", it) }
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = notificationBuilder.buildActionNotification(
                content = content,
                channelId = NotificationService.CHANNEL_ID_MUSIC,
                primaryAction = pendingIntent,
                secondaryAction = NotificationAction(
                    icon = R.drawable.play,
                    title = content.actionText,
                    pendingIntent = pendingIntent
                )
            )
            
            notificationManager.notify(generateNotificationId(), notification)
            errorHandler.logNotificationMetrics(content, true)
            
            // Record delivery for behavior learning
            scheduler.recordNotificationInteraction(
                content.contentType,
                System.currentTimeMillis(),
                NotificationInteraction.CLICKED
            )
        } catch (e: Exception) {
            errorHandler.handleDeliveryError(e, content)
        }
    }
    
    override fun showMarketingNotification(content: NotificationContent) {
        try {
            val pendingIntent = PendingIntent.getActivity(
                context, 0, createMainActivityIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = notificationBuilder.buildBigTextNotification(
                content = content,
                channelId = NotificationService.CHANNEL_ID_MARKETING,
                pendingIntent = pendingIntent
            )
            
            notificationManager.notify(generateNotificationId(), notification)
            errorHandler.logNotificationMetrics(content, true)
        } catch (e: Exception) {
            errorHandler.handleDeliveryError(e, content)
        }
    }
    
    override fun showTrendingNotification(content: NotificationContent) {
        try {
            val intent = createMainActivityIntent().apply {
                content.songId?.let { 
                    putExtra("songId", it)
                    putExtra("autoPlay", true)
                }
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = notificationBuilder.buildActionNotification(
                content = content,
                channelId = NotificationService.CHANNEL_ID_MUSIC,
                primaryAction = pendingIntent,
                secondaryAction = NotificationAction(
                    icon = R.drawable.play,
                    title = "Play Trending",
                    pendingIntent = pendingIntent
                )
            )
            
            notificationManager.notify(generateNotificationId(), notification)
            errorHandler.logNotificationMetrics(content, true)
        } catch (e: Exception) {
            errorHandler.handleDeliveryError(e, content)
        }
    }
    
    override fun showPersonalizedSuggestion(content: NotificationContent) {
        try {
            val intent = createMainActivityIntent().apply {
                content.songId?.let { putExtra("songId", it) }
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = notificationBuilder.buildBigPictureNotification(
                content = content,
                channelId = NotificationService.CHANNEL_ID_MUSIC,
                pendingIntent = pendingIntent
            )
            
            notificationManager.notify(generateNotificationId(), notification)
            errorHandler.logNotificationMetrics(content, true)
        } catch (e: Exception) {
            errorHandler.handleDeliveryError(e, content)
        }
    }
    
    override fun showRatingNotification(content: NotificationContent) {
        val lastShown = prefs.getLong("last_rating_shown", 0L)
        val now = System.currentTimeMillis()
        
        if (now - lastShown > RATING_MIN_INTERVAL && !isUserBusy()) {
            try {
                val intent = createMainActivityIntent().apply {
                    putExtra("showRatingDialog", true)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val notification = notificationBuilder.buildActionNotification(
                    content = content,
                    channelId = NotificationService.CHANNEL_ID_GENERAL,
                    primaryAction = pendingIntent,
                    secondaryAction = NotificationAction(
                        icon = R.drawable.ic_stat_name,
                        title = "Rate Now",
                        pendingIntent = pendingIntent
                    )
                )
                
                notificationManager.notify(generateNotificationId(), notification)
                prefs.edit().putLong("last_rating_shown", now).apply()
                errorHandler.logNotificationMetrics(content, true)
            } catch (e: Exception) {
                errorHandler.handleDeliveryError(e, content)
            }
        }
    }
    
    override fun showCustomNotification(content: NotificationContent, pendingIntent: PendingIntent) {
        try {
            val notification = notificationBuilder.buildBasicNotification(
                content = content,
                channelId = NotificationService.CHANNEL_ID_GENERAL,
                pendingIntent = pendingIntent
            )
            
            notificationManager.notify(generateNotificationId(), notification)
            errorHandler.logNotificationMetrics(content, true)
        } catch (e: Exception) {
            errorHandler.handleDeliveryError(e, content)
        }
    }
    
    override fun cancelNotification(notificationId: Int) {
        try {
            notificationManager.cancel(notificationId)
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    override fun cancelAllNotifications() {
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    override fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    override fun generateNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }
    
    /**
     * Get throttle status for debugging
     */
    fun getThrottleStatus(): String {
        val status = throttleManager.getThrottleStatus()
        return buildString {
            appendLine("=== NOTIFICATION THROTTLE STATUS ===")
            appendLine("Can send now: ${status.canSendNow}")
            appendLine("Time until next: ${status.timeUntilNextFormatted}")
            appendLine("Last notification: ${status.lastNotificationFormatted}")
            appendLine("Recent notifications: ${status.recentNotifications.size}")
            appendLine("====================================")
        }
    }
    
    /**
     * Reset throttling for testing
     */
    fun resetThrottling() {
        throttleManager.clearHistory()
    }
    
    // Legacy methods for backward compatibility (deprecated)
    
    @Deprecated("Use showRatingNotification with NotificationContent instead")
    fun showRatingNotificationIfAllowed() {
        val content = NotificationContent(
            title = "Enjoying FavTunes?",
            body = "Rate us! Your feedback helps us improve.",
            emoji = "⭐",
            actionText = "Rate Now",
            contentType = "rating"
        )
        showRatingNotification(content)
    }

    @Deprecated("Use showEngagementNotification with NotificationContent instead")
    fun showEngagementNotification(title: String, body: String) {
        val content = NotificationContent(
            title = title,
            body = body,
            emoji = "🎵",
            actionText = "Fix This",
            contentType = "engagement"
        )
        showEngagementNotification(content)
    }
    @Deprecated("Use showEngagementNotification instead")
    fun showEngagementNotificationEnhanced(content: NotificationContent) {
        showEngagementNotification(content)
    }

    @Deprecated("Use showMusicSuggestion with NotificationContent instead")
    fun showMusicSuggestion(title: String, body: String, songId: String? = null) {
        val content = NotificationContent(
            title = title,
            body = body,
            emoji = "🎵",
            actionText = "Play Now",
            songId = songId,
            contentType = "music_suggestion"
        )
        showMusicSuggestion(content)
    }
    @Deprecated("Use showMusicSuggestion instead")
    fun showMusicSuggestionEnhanced(content: NotificationContent) {
        showMusicSuggestion(content)
    }

    @Deprecated("Use showCustomNotification with NotificationContent instead")
    fun showMusicNotification(title: String, body: String, pendingIntent: PendingIntent) {
        val content = NotificationContent(
            title = title,
            body = body,
            emoji = "🎵",
            actionText = "Play Now",
            contentType = "music"
        )
        showCustomNotification(content, pendingIntent)
    }

    @Deprecated("Use showMarketingNotification with NotificationContent instead")
    fun showMarketingNotification(title: String, body: String) {
        val content = NotificationContent(
            title = title,
            body = body,
            emoji = "📢",
            actionText = "Check It Out",
            contentType = "marketing"
        )
        showMarketingNotification(content)
    }

    @Deprecated("Use showTrendingNotification with NotificationContent instead")
    fun showTrendingNotification(title: String, body: String, songId: String) {
        val content = NotificationContent(
            title = title,
            body = body,
            emoji = "🔥",
            actionText = "Play Trending",
            songId = songId,
            contentType = "trending"
        )
        showTrendingNotification(content)
    }

    @Deprecated("Use showPersonalizedSuggestion with NotificationContent instead")
    fun showMusicSuggestionPersonalized() {
        val suggestionSystem = com.shaadow.tunes.suggestion.SimpleSuggestionIntegration.getInstance(context).getSuggestionSystem()
        CoroutineScope(Dispatchers.Main).launch {
            val recommendations = withContext(Dispatchers.IO) { suggestionSystem.getRecommendations(1) }
            if (recommendations.isNotEmpty()) {
                val mediaItem = recommendations.first()
                val content = NotificationContent(
                    title = mediaItem.mediaMetadata?.title?.toString() ?: "Recommended Song",
                    body = "From ${mediaItem.mediaMetadata?.artist?.toString() ?: "Unknown Artist"}",
                    emoji = "🎵",
                    actionText = "Play Now",
                    songId = mediaItem.mediaId,
                    contentType = "personalized_suggestion"
                )
                showPersonalizedSuggestion(content)
            }
        }
    }

    // Private helper methods
    
    private fun createMainActivityIntent(): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
    
    private fun isUserBusy(): Boolean {
        // TODO: Integrate with playback or activity state
        return false
    }
}