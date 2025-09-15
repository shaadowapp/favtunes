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
 * Simple and smart notification manager
 * Sends helpful notifications without being spammy
 */
class FavTunesNotificationManager(
    private val context: Context,
    private val notificationDelivery: NotificationDelivery = LocalNotificationHelper(context),
    private val contentGenerator: NotificationContentGenerator = NotificationContentGenerator()
) {
    
    companion object {
        const val PREF_LAST_APP_OPEN = "last_app_open"
        const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val PREF_MUSIC_SUGGESTIONS = "music_suggestions_enabled"
        const val PREF_ENGAGEMENT_REMINDERS = "engagement_reminders_enabled"
        const val PREF_LAST_NOTIFICATION = "last_notification_time"
        
        private const val ENGAGEMENT_WORK_TAG = "engagement_notifications"
        private const val DAILY_SUGGESTION_WORK_TAG = "daily_suggestions"
        private const val MIN_NOTIFICATION_GAP_HOURS = 6 // Minimum 6 hours between notifications
    }

    /**
     * Schedule smart engagement notifications
     */
    suspend fun scheduleEngagementNotifications() {
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, true)) return
        if (!getPreferences().getBoolean(PREF_ENGAGEMENT_REMINDERS, true)) return
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val engagementWork = PeriodicWorkRequestBuilder<EngagementNotificationWorker>(
            1, TimeUnit.DAYS // Check daily, but won't send if user was active recently
        )
            .setConstraints(constraints)
            .addTag(ENGAGEMENT_WORK_TAG)
            .setInitialDelay(8, TimeUnit.HOURS) // Start after 8 hours
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                ENGAGEMENT_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                engagementWork
            )
    }

    /**
     * Schedule reduced frequency engagement notifications (Play Store compliant)
     * Only sends notifications after 3+ days of inactivity, maximum once per week
     */
    suspend fun scheduleReducedEngagementNotifications() {
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, true)) return
        if (!getPreferences().getBoolean(PREF_ENGAGEMENT_REMINDERS, true)) return
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val engagementWork = PeriodicWorkRequestBuilder<ReducedEngagementNotificationWorker>(
            7, TimeUnit.DAYS // Check weekly instead of daily
        )
            .setConstraints(constraints)
            .addTag("reduced_$ENGAGEMENT_WORK_TAG")
            .setInitialDelay(3, TimeUnit.DAYS) // Start after 3 days
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "reduced_$ENGAGEMENT_WORK_TAG",
                ExistingPeriodicWorkPolicy.REPLACE,
                engagementWork
            )
    }

    /**
     * Schedule smart music suggestions
     */
    suspend fun scheduleDailySuggestions() {
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, true)) return
        if (!getPreferences().getBoolean(PREF_MUSIC_SUGGESTIONS, true)) return
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val suggestionWork = PeriodicWorkRequestBuilder<DailySuggestionWorker>(
            1, TimeUnit.DAYS // Check daily for new music suggestions
        )
            .setConstraints(constraints)
            .addTag(DAILY_SUGGESTION_WORK_TAG)
            .setInitialDelay(getRandomDelay(), TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_SUGGESTION_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                suggestionWork
            )
    }

    /**
     * Schedule weekly music suggestions (Play Store compliant)
     * Reduced frequency to avoid being considered spam
     */
    suspend fun scheduleWeeklySuggestions() {
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, true)) return
        if (!getPreferences().getBoolean(PREF_MUSIC_SUGGESTIONS, true)) return
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val suggestionWork = PeriodicWorkRequestBuilder<WeeklySuggestionWorker>(
            7, TimeUnit.DAYS // Weekly instead of daily
        )
            .setConstraints(constraints)
            .addTag("weekly_$DAILY_SUGGESTION_WORK_TAG")
            .setInitialDelay(2, TimeUnit.DAYS) // Start after 2 days
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "weekly_$DAILY_SUGGESTION_WORK_TAG",
                ExistingPeriodicWorkPolicy.REPLACE,
                suggestionWork
            )
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

    private fun canSendNotification(): Boolean {
        val lastNotification = getPreferences().getLong(PREF_LAST_NOTIFICATION, 0)
        val hoursSinceLastNotification = (System.currentTimeMillis() - lastNotification) / (1000 * 60 * 60)
        return hoursSinceLastNotification >= MIN_NOTIFICATION_GAP_HOURS
    }

    private fun recordNotificationSent() {
        getPreferences().edit()
            .putLong(PREF_LAST_NOTIFICATION, System.currentTimeMillis())
            .apply()
    }

    /**
     * Send engagement notification if appropriate
     */
    suspend fun sendEngagementNotification(hoursSinceLastOpen: Long) {
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, true)) return
        if (!getPreferences().getBoolean(PREF_ENGAGEMENT_REMINDERS, true)) return
        if (!canSendNotification()) return
        
        val content = contentGenerator.generateEngagementContent(hoursSinceLastOpen)
        notificationDelivery.showEngagementNotification(content)
        recordNotificationSent()
    }
    
    /**
     * Send music suggestion notification
     */
    suspend fun sendMusicSuggestion(songs: List<Song>) {
        if (!getPreferences().getBoolean(PREF_NOTIFICATIONS_ENABLED, true)) return
        if (!getPreferences().getBoolean(PREF_MUSIC_SUGGESTIONS, true)) return
        if (!canSendNotification()) return
        if (songs.isEmpty()) return
        
        val content = contentGenerator.generateMusicSuggestionContent(songs)
        notificationDelivery.showMusicSuggestion(content)
        recordNotificationSent()
    }

    fun cancelAllNotifications() {
        WorkManager.getInstance(context).cancelAllWorkByTag(ENGAGEMENT_WORK_TAG)
        WorkManager.getInstance(context).cancelAllWorkByTag(DAILY_SUGGESTION_WORK_TAG)
    }
    
    private fun getRandomDelay(): Long = Random.nextLong(4, 12) // 4-12 hours
    
    private fun getPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}

/**
 * Simple engagement notification worker
 */
class EngagementNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationManager = FavTunesNotificationManager(applicationContext)
        val hoursSinceLastOpen = notificationManager.getHoursSinceLastOpen()
        
        // Only send if user has been away for at least 2 days
        if (hoursSinceLastOpen >= 48) {
            notificationManager.sendEngagementNotification(hoursSinceLastOpen)
        }
        
        return Result.success()
    }
}

/**
 * Simple music suggestion worker
 */
class DailySuggestionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val notificationManager = FavTunesNotificationManager(applicationContext)
            
            // Get some recent songs for suggestions
            val recentSongs = Database.songs(
                sortBy = com.shaadow.tunes.enums.SongSortBy.DateAdded, 
                sortOrder = com.shaadow.tunes.enums.SortOrder.Descending
            ).first().take(5)
            
            if (recentSongs.isNotEmpty()) {
                notificationManager.sendMusicSuggestion(recentSongs)
            }
            
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}

/**
 * Reduced frequency engagement notification worker (Play Store compliant)
 * Only sends notifications after 3+ days of inactivity, maximum once per week
 */
class ReducedEngagementNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationManager = FavTunesNotificationManager(applicationContext)
        val hoursSinceLastOpen = notificationManager.getHoursSinceLastOpen()
        
        // Only send if user has been away for at least 3 days (72 hours)
        // and respects the minimum gap between notifications
        if (hoursSinceLastOpen >= 72) {
            notificationManager.sendEngagementNotification(hoursSinceLastOpen)
        }
        
        return Result.success()
    }
}

/**
 * Weekly music suggestion worker (Play Store compliant)
 * Reduced frequency to avoid being considered spam
 */
class WeeklySuggestionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val notificationManager = FavTunesNotificationManager(applicationContext)
            
            // Only send suggestions if user has been somewhat active
            val hoursSinceLastOpen = notificationManager.getHoursSinceLastOpen()
            if (hoursSinceLastOpen > 168) { // More than a week - user might have abandoned app
                return Result.success() // Don't send to inactive users
            }
            
            // Get some recent songs for suggestions
            val recentSongs = Database.songs(
                sortBy = com.shaadow.tunes.enums.SongSortBy.DateAdded, 
                sortOrder = com.shaadow.tunes.enums.SortOrder.Descending
            ).first().take(3) // Reduced from 5 to 3
            
            if (recentSongs.isNotEmpty()) {
                notificationManager.sendMusicSuggestion(recentSongs)
            }
            
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}