package com.shaadow.tunes.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.shaadow.tunes.Database
import com.shaadow.tunes.models.Song
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Refactored NotificationManager using new architecture with proper separation of concerns
 * Delegates notification delivery to NotificationDelivery interface implementations
 */
class FavTunesNotificationManager(
    private val context: Context,
    private val notificationDelivery: NotificationDelivery = LocalNotificationHelper(context),
    private val contentGenerator: NotificationContentGenerator = NotificationContentGenerator(),
    private val scheduler: IntelligentNotificationScheduler = IntelligentNotificationScheduler(context),
    private val preferences: NotificationPreferencesService = NotificationPreferencesService(context)
) {
    
    companion object {
        const val PREF_LAST_APP_OPEN = "last_app_open"
        const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val PREF_MUSIC_SUGGESTIONS = "music_suggestions_enabled"
        const val PREF_ENGAGEMENT_REMINDERS = "engagement_reminders_enabled"
        const val PREF_MARKETING_NOTIFICATIONS = "marketing_notifications_enabled"
        
        private const val ENGAGEMENT_WORK_TAG = "engagement_notifications"
        private const val DAILY_SUGGESTION_WORK_TAG = "daily_suggestions"
    }

    /**
     * Schedule engagement notifications using intelligent scheduling with proper throttling
     */
    suspend fun scheduleEngagementNotifications() {
        // Check master notification switch first
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, false)) return
        if (!preferences.isNotificationTypeEnabled(NotificationType.ENGAGEMENT)) return
        
        // Check if we can schedule based on throttling rules
        val timeUntilNext = scheduler.getTimeUntilNextLocalNotification()
        val initialDelay = maxOf(timeUntilNext / (1000 * 60 * 60), 4L) // At least 4 hours to prevent spam
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val engagementWork = PeriodicWorkRequestBuilder<EngagementNotificationWorker>(
            24, TimeUnit.HOURS // Check every 24 hours instead of 12 to reduce frequency
        )
            .setConstraints(constraints)
            .addTag(ENGAGEMENT_WORK_TAG)
            .setInitialDelay(initialDelay, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                ENGAGEMENT_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing work to prevent duplicate scheduling
                engagementWork
            )
    }

    /**
     * Schedule daily music suggestions with intelligent timing and proper throttling
     */
    suspend fun scheduleDailySuggestions() {
        // Check master notification switch first
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, false)) return
        if (!preferences.isNotificationTypeEnabled(NotificationType.MUSIC_SUGGESTIONS)) return
        
        // Ensure proper spacing between notifications
        val timeUntilNext = scheduler.getTimeUntilNextLocalNotification()
        val baseDelay = getRandomDelay() // 8-20 hours
        val adjustedDelay = maxOf(baseDelay, timeUntilNext / (1000 * 60 * 60) + 3L) // At least 3 hours after next available slot
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val suggestionWork = PeriodicWorkRequestBuilder<DailySuggestionWorker>(
            2, TimeUnit.DAYS // Reduce frequency to every 2 days instead of daily
        )
            .setConstraints(constraints)
            .addTag(DAILY_SUGGESTION_WORK_TAG)
            .setInitialDelay(adjustedDelay, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_SUGGESTION_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing work to prevent duplicate scheduling
                suggestionWork
            )
    }

    /**
     * Schedule marketing notifications with user preference respect
     * This method only schedules workers, it doesn't deliver notifications immediately
     */
    suspend fun scheduleMarketingNotification() {
        // Check master notification switch first
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, false)) return
        if (!preferences.isNotificationTypeEnabled(NotificationType.MARKETING)) return
        
        // Schedule a marketing worker for later, don't deliver immediately
        val delay = getRandomMarketingDelay() // 1-7 days
        val marketingWork = PeriodicWorkRequestBuilder<MarketingNotificationWorker>(
            14, TimeUnit.DAYS // Check bi-weekly instead of weekly to reduce frequency
        )
            .setInitialDelay(delay, TimeUnit.HOURS)
            .addTag("marketing_notifications")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "marketing_notifications",
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing work to prevent duplicate scheduling
                marketingWork
            )
        
        android.util.Log.d("FavTunesNotificationManager", 
            "Marketing notifications scheduled with ${delay}h initial delay")
    }
    
    /**
     * Send marketing notification (called by worker, not on app startup)
     */
    suspend fun sendMarketingNotification() {
        // Check master notification switch first
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, false)) return
        if (!preferences.isNotificationTypeEnabled(NotificationType.MARKETING)) return
        
        val content = contentGenerator.generateMarketingContent()
        val result = scheduler.scheduleNotification(
            content = content,
            priority = NotificationPriority.LOW,
            deliveryWindow = DeliveryWindow(startHour = 10, endHour = 20)
        )
        
        when (result) {
            is ScheduleResult.DeliveredImmediately -> {
                notificationDelivery.showMarketingNotification(content)
            }
            is ScheduleResult.ScheduledForLater -> {
                scheduleDelayedMarketingNotification(content, result.deliveryTime)
            }
            is ScheduleResult.Blocked -> {
                android.util.Log.d("FavTunesNotificationManager", 
                    "Marketing notification blocked: ${result.reason}")
            }
        }
    }

    fun updateLastAppOpen() {
        getPreferences().edit()
            .putLong(PREF_LAST_APP_OPEN, System.currentTimeMillis())
            .apply()
    }

    fun getHoursSinceLastOpen(): Long {
        val lastOpen = getPreferences().getLong(PREF_LAST_APP_OPEN, System.currentTimeMillis())
        return (System.currentTimeMillis() - lastOpen) / (1000 * 60 * 60)
    }

    /**
     * Send immediate engagement notification with intelligent content
     */
    suspend fun sendEngagementNotification(hoursSinceLastOpen: Long) {
        // Check master notification switch first
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, false)) return
        if (!preferences.isNotificationTypeEnabled(NotificationType.ENGAGEMENT)) return
        
        val content = contentGenerator.generateEngagementContent(hoursSinceLastOpen)
        val result = scheduler.scheduleNotification(
            content = content,
            priority = NotificationPriority.NORMAL
        )
        
        when (result) {
            is ScheduleResult.DeliveredImmediately -> {
                notificationDelivery.showEngagementNotification(content)
            }
            is ScheduleResult.ScheduledForLater -> {
                // Will be delivered later by scheduler
            }
            is ScheduleResult.Blocked -> {
                // Respect user preferences and constraints
            }
        }
    }
    
    /**
     * Send music suggestion with personalization
     */
    suspend fun sendMusicSuggestion(songs: List<Song>) {
        // Check master notification switch first
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, false)) return
        if (!preferences.isNotificationTypeEnabled(NotificationType.MUSIC_SUGGESTIONS)) return
        
        val content = contentGenerator.generateMusicSuggestionContent(songs)
        val result = scheduler.scheduleNotification(
            content = content,
            priority = NotificationPriority.NORMAL,
            deliveryWindow = DeliveryWindow(startHour = 8, endHour = 22)
        )
        
        when (result) {
            is ScheduleResult.DeliveredImmediately -> {
                notificationDelivery.showMusicSuggestion(content)
            }
            is ScheduleResult.ScheduledForLater -> {
                // Will be delivered at optimal time
            }
            is ScheduleResult.Blocked -> {
                // Respect constraints
            }
        }
    }

    fun cancelAllNotifications() {
        WorkManager.getInstance(context).cancelAllWorkByTag(ENGAGEMENT_WORK_TAG)
        WorkManager.getInstance(context).cancelAllWorkByTag(DAILY_SUGGESTION_WORK_TAG)
    }

    /**
     * Record user interaction for learning
     */
    fun recordNotificationInteraction(
        contentType: String,
        deliveryTime: Long,
        interactionType: NotificationInteraction
    ) {
        scheduler.recordNotificationInteraction(contentType, deliveryTime, interactionType)
    }
    
    /**
     * Record app usage for behavior learning
     */
    fun recordAppUsage(startTime: Long, endTime: Long, activityType: String) {
        scheduler.recordAppUsage(startTime, endTime, activityType)
    }
    
    /**
     * Handle push notification delivery (Firebase/OneSignal) - bypasses local notification throttling
     */
    fun handlePushNotification(notificationId: String, contentType: String, title: String, body: String) {
        // Record push notification (doesn't affect local notification timing)
        scheduler.recordPushNotificationDelivery(notificationId, contentType)
        
        // Push notifications can be delivered immediately as they don't count toward local notification limits
        android.util.Log.d("FavTunesNotificationManager", 
            "Push notification received: $contentType - $title")
    }
    
    /**
     * Get status of notification throttling
     */
    fun getNotificationThrottleStatus(): NotificationThrottleStatus {
        val timeUntilNext = scheduler.getTimeUntilNextLocalNotification()
        val pendingCount = scheduler.checkPendingNotifications().size
        
        return NotificationThrottleStatus(
            canDeliverNow = timeUntilNext == 0L,
            timeUntilNextSlot = timeUntilNext,
            pendingNotifications = pendingCount,
            nextAvailableTime = System.currentTimeMillis() + timeUntilNext
        )
    }
    
    private fun getRandomDelay(): Long = Random.nextLong(8, 20) // 8-20 hours

    private fun getRandomMarketingDelay(): Long = Random.nextLong(24, 168) // 1-7 days
    
    private fun scheduleDelayedMarketingNotification(content: NotificationContent, deliveryTime: Long) {
        val delay = deliveryTime - System.currentTimeMillis()
        val marketingWork = OneTimeWorkRequestBuilder<DelayedMarketingWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("content_title" to content.title, "content_body" to content.body))
            .build()
        
        WorkManager.getInstance(context).enqueue(marketingWork)
    }
    
    private fun getPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}

/**
 * Refactored worker using new architecture with throttling respect
 */
class EngagementNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationManager = FavTunesNotificationManager(applicationContext)
        val hoursSinceLastOpen = notificationManager.getHoursSinceLastOpen()
        
        // Only send engagement notifications if user has been away for at least 48 hours (increased from 24)
        if (hoursSinceLastOpen < 48) {
            android.util.Log.d("EngagementWorker", "User was active recently ($hoursSinceLastOpen hours ago). Skipping engagement notification.")
            return Result.success()
        }
        
        // Check throttling status before attempting to send
        val throttleStatus = notificationManager.getNotificationThrottleStatus()
        
        if (!throttleStatus.canDeliverNow) {
            // If we can't deliver now, skip this attempt and let the next periodic run handle it
            android.util.Log.d("EngagementWorker", 
                "Notification throttled. Next slot in: ${throttleStatus.getFormattedTimeUntilNext()}. Skipping this attempt.")
            return Result.success() // Don't retry immediately, wait for next periodic run
        }
        
        // Additional check: don't send if we've already sent too many notifications today
        if (throttleStatus.pendingNotifications > 2) {
            android.util.Log.d("EngagementWorker", "Too many pending notifications (${throttleStatus.pendingNotifications}). Skipping.")
            return Result.success()
        }
        
        // Use new architecture with intelligent scheduling
        notificationManager.sendEngagementNotification(hoursSinceLastOpen)
        
        return Result.success()
    }
}

/**
 * Refactored worker using new architecture with throttling respect
 */
class DailySuggestionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val notificationManager = FavTunesNotificationManager(applicationContext)
            
            // Check throttling status before attempting to send
            val throttleStatus = notificationManager.getNotificationThrottleStatus()
            
            if (!throttleStatus.canDeliverNow) {
                // If we can't deliver now, skip this attempt and let the next periodic run handle it
                android.util.Log.d("DailySuggestionWorker", 
                    "Notification throttled. Next slot in: ${throttleStatus.getFormattedTimeUntilNext()}. Skipping this attempt.")
                return Result.success() // Don't retry immediately, wait for next periodic run
            }
            
            // Additional check: don't send if we've already sent too many notifications today
            if (throttleStatus.pendingNotifications > 1) {
                android.util.Log.d("DailySuggestionWorker", "Too many pending notifications (${throttleStatus.pendingNotifications}). Skipping.")
                return Result.success()
            }
            
            val recentSongs = Database.songs(
                sortBy = com.shaadow.tunes.enums.SongSortBy.DateAdded, 
                sortOrder = com.shaadow.tunes.enums.SortOrder.Descending
            ).first().take(10)
            
            // Only send if we have songs to suggest
            if (recentSongs.isEmpty()) {
                android.util.Log.d("DailySuggestionWorker", "No songs available for suggestion. Skipping.")
                return Result.success()
            }
            
            // Use new architecture with intelligent scheduling
            notificationManager.sendMusicSuggestion(recentSongs)
            
            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DailySuggestionWorker", "Error in daily suggestion worker", e)
            return Result.failure() // Changed from retry to failure to prevent endless retries
        }
    }
}

/**
 * Refactored marketing worker using new architecture with throttling respect
 */
class MarketingNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationManager = FavTunesNotificationManager(applicationContext)
        
        // Check throttling status before attempting to send
        val throttleStatus = notificationManager.getNotificationThrottleStatus()
        
        if (!throttleStatus.canDeliverNow) {
            // If we can't deliver now, reschedule for the next available slot
            android.util.Log.d("MarketingWorker", 
                "Notification throttled. Next slot in: ${throttleStatus.getFormattedTimeUntilNext()}")
            return Result.retry()
        }
        
        // Use new architecture with intelligent scheduling
        notificationManager.sendMarketingNotification()
        
        return Result.success()
    }
}

/**
 * Worker for delayed marketing notifications scheduled by intelligent scheduler
 */
class DelayedMarketingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("content_title") ?: return Result.failure()
        val body = inputData.getString("content_body") ?: return Result.failure()
        
        val content = NotificationContent(
            title = title,
            body = body,
            emoji = "📢",
            actionText = "Check It Out",
            contentType = "marketing"
        )
        
        // Check throttling before delivering
        val scheduler = IntelligentNotificationScheduler(applicationContext)
        val timeUntilNext = scheduler.getTimeUntilNextLocalNotification()
        
        if (timeUntilNext > 0) {
            // Reschedule for later if we're in cooldown
            return Result.retry()
        }
        
        val notificationDelivery = LocalNotificationHelper(applicationContext)
        notificationDelivery.showMarketingNotification(content)
        
        return Result.success()
    }
}

/**
 * Data class for notification throttle status
 */
data class NotificationThrottleStatus(
    val canDeliverNow: Boolean,
    val timeUntilNextSlot: Long,
    val pendingNotifications: Int,
    val nextAvailableTime: Long
) {
    fun getFormattedTimeUntilNext(): String {
        val hours = timeUntilNextSlot / (1000 * 60 * 60)
        val minutes = (timeUntilNextSlot % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Now"
        }
    }
}