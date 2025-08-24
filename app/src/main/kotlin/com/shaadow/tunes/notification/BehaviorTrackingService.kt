package com.shaadow.tunes.notification

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import java.util.*

/**
 * Service for tracking user behavior patterns to optimize notification scheduling
 * Integrates with IntelligentNotificationScheduler for behavior learning
 */
class BehaviorTrackingService(
    private val context: Context,
    private val scheduler: IntelligentNotificationScheduler,
    private val preferences: SharedPreferences = context.getSharedPreferences("behavior_tracking", Context.MODE_PRIVATE)
) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var sessionStartTime: Long = 0L
    private var currentActivity: String = ""
    
    companion object {
        private const val SESSION_TIMEOUT_MS = 30 * 60 * 1000L // 30 minutes
        private const val MIN_SESSION_DURATION_MS = 10 * 1000L // 10 seconds
    }
    
    /**
     * Start tracking a user session
     */
    fun startSession(activityType: String = "general") {
        sessionStartTime = System.currentTimeMillis()
        currentActivity = activityType
        
        // Record session start
        recordSessionEvent("session_start", activityType)
    }
    
    /**
     * End tracking current user session
     */
    fun endSession() {
        if (sessionStartTime > 0L) {
            val endTime = System.currentTimeMillis()
            val sessionDuration = endTime - sessionStartTime
            
            // Only record meaningful sessions
            if (sessionDuration >= MIN_SESSION_DURATION_MS) {
                scheduler.recordAppUsage(sessionStartTime, endTime, currentActivity)
                recordSessionEvent("session_end", currentActivity, sessionDuration)
            }
            
            sessionStartTime = 0L
            currentActivity = ""
        }
    }
    
    /**
     * Record user interaction with a notification
     */
    fun recordNotificationClick(contentType: String, deliveryTime: Long) {
        scheduler.recordNotificationInteraction(
            contentType,
            deliveryTime,
            NotificationInteraction.CLICKED
        )
        
        // Track click patterns for learning
        recordNotificationEvent("click", contentType, deliveryTime)
    }
    
    /**
     * Record user dismissing a notification
     */
    fun recordNotificationDismissal(contentType: String, deliveryTime: Long) {
        scheduler.recordNotificationInteraction(
            contentType,
            deliveryTime,
            NotificationInteraction.DISMISSED
        )
        
        recordNotificationEvent("dismiss", contentType, deliveryTime)
    }
    
    /**
     * Record user ignoring a notification (no interaction)
     */
    fun recordNotificationIgnored(contentType: String, deliveryTime: Long) {
        scheduler.recordNotificationInteraction(
            contentType,
            deliveryTime,
            NotificationInteraction.IGNORED
        )
        
        recordNotificationEvent("ignore", contentType, deliveryTime)
    }
    
    /**
     * Record user taking action from notification (e.g., playing suggested song)
     */
    fun recordNotificationAction(contentType: String, deliveryTime: Long, actionType: String) {
        scheduler.recordNotificationInteraction(
            contentType,
            deliveryTime,
            NotificationInteraction.ACTION_TAKEN
        )
        
        recordNotificationEvent("action_$actionType", contentType, deliveryTime)
    }
    
    /**
     * Track music listening behavior
     */
    fun recordMusicActivity(
        songId: String,
        genre: String,
        listenDuration: Long,
        wasSkipped: Boolean,
        skipTime: Long? = null
    ) {
        scope.launch {
            val currentTime = System.currentTimeMillis()
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            
            // Update genre preferences
            updateGenrePreference(genre, listenDuration, wasSkipped)
            
            // Update listening patterns by hour
            updateHourlyListeningPattern(hour, listenDuration)
            
            // Track skip behavior
            if (wasSkipped && skipTime != null) {
                recordSkipBehavior(genre, skipTime, listenDuration)
            }
            
            // Update overall music profile
            updateMusicProfile(songId, genre, listenDuration, wasSkipped)
        }
    }
    
    /**
     * Get current user engagement score (0.0 to 1.0)
     */
    fun getUserEngagementScore(): Double {
        val recentSessions = getRecentSessionCount(7) // Last 7 days
        val recentNotificationClicks = getRecentNotificationClicks(7)
        val totalRecentNotifications = getRecentNotificationCount(7)
        
        val sessionScore = minOf(recentSessions / 14.0, 1.0) // Normalize to max 2 sessions per day
        val clickRate = if (totalRecentNotifications > 0) {
            recentNotificationClicks.toDouble() / totalRecentNotifications
        } else {
            0.5 // Default neutral score
        }
        
        return (sessionScore * 0.6 + clickRate * 0.4).coerceIn(0.0, 1.0)
    }
    
    /**
     * Get optimal notification timing based on user patterns
     */
    fun getOptimalNotificationHours(): List<Int> {
        val hourlyActivity = mutableMapOf<Int, Long>()
        
        // Analyze historical session data
        for (hour in 0..23) {
            val activityKey = "hourly_activity_$hour"
            hourlyActivity[hour] = preferences.getLong(activityKey, 0L)
        }
        
        // Return top 4 most active hours
        return hourlyActivity.entries
            .sortedByDescending { it.value }
            .take(4)
            .map { it.key }
            .sorted()
    }
    
    // Private helper methods
    
    private fun recordSessionEvent(eventType: String, activityType: String, duration: Long? = null) {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            val key = "session_${eventType}_$timestamp"
            
            preferences.edit()
                .putString(key, "$activityType:${duration ?: 0}")
                .putLong("last_session_time", timestamp)
                .apply()
            
            // Update daily session count
            val today = getTodayKey()
            val dailySessionsKey = "daily_sessions_$today"
            val currentCount = preferences.getInt(dailySessionsKey, 0)
            preferences.edit().putInt(dailySessionsKey, currentCount + 1).apply()
        }
    }
    
    private fun recordNotificationEvent(eventType: String, contentType: String, deliveryTime: Long) {
        scope.launch {
            val calendar = Calendar.getInstance().apply { timeInMillis = deliveryTime }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            
            // Update hourly interaction patterns
            val hourlyKey = "notification_${eventType}_hour_$hour"
            val hourlyCount = preferences.getInt(hourlyKey, 0)
            preferences.edit().putInt(hourlyKey, hourlyCount + 1).apply()
            
            // Update content type interaction patterns
            val contentKey = "notification_${eventType}_${contentType}"
            val contentCount = preferences.getInt(contentKey, 0)
            preferences.edit().putInt(contentKey, contentCount + 1).apply()
            
            // Update daily notification interaction count
            val today = getTodayKey()
            val dailyKey = "daily_notifications_${eventType}_$today"
            val dailyCount = preferences.getInt(dailyKey, 0)
            preferences.edit().putInt(dailyKey, dailyCount + 1).apply()
        }
    }
    
    private fun updateGenrePreference(genre: String, listenDuration: Long, wasSkipped: Boolean) {
        val genreKey = "genre_preference_$genre"
        val currentScore = preferences.getFloat(genreKey, 0.5f)
        
        // Adjust score based on listening behavior
        val adjustment = if (wasSkipped) {
            -0.1f * (listenDuration / 30000f) // Penalty for skipping, less if listened longer
        } else {
            0.1f * (listenDuration / 180000f) // Reward for full listen, more for longer songs
        }
        
        val newScore = (currentScore + adjustment).coerceIn(0.0f, 1.0f)
        preferences.edit().putFloat(genreKey, newScore).apply()
    }
    
    private fun updateHourlyListeningPattern(hour: Int, duration: Long) {
        val hourlyKey = "listening_hour_$hour"
        val currentDuration = preferences.getLong(hourlyKey, 0L)
        preferences.edit().putLong(hourlyKey, currentDuration + duration).apply()
    }
    
    private fun recordSkipBehavior(genre: String, skipTime: Long, totalDuration: Long) {
        val skipKey = "skip_behavior_$genre"
        val skipData = preferences.getString(skipKey, "0:0:0") // count:totalSkipTime:totalDuration
        val parts = skipData!!.split(":")
        
        val count = parts[0].toInt() + 1
        val totalSkipTime = parts[1].toLong() + skipTime
        val totalSongDuration = parts[2].toLong() + totalDuration
        
        preferences.edit().putString(skipKey, "$count:$totalSkipTime:$totalSongDuration").apply()
    }
    
    private fun updateMusicProfile(songId: String, genre: String, duration: Long, wasSkipped: Boolean) {
        // Update total listening time
        val totalListeningKey = "total_listening_time"
        val currentTotal = preferences.getLong(totalListeningKey, 0L)
        preferences.edit().putLong(totalListeningKey, currentTotal + duration).apply()
        
        // Update recent songs list (keep last 20)
        val recentSongsKey = "recent_songs"
        val recentSongs = preferences.getStringSet(recentSongsKey, mutableSetOf()) ?: mutableSetOf()
        recentSongs.add("$songId:${System.currentTimeMillis()}")
        
        // Keep only last 20 songs
        val sortedSongs = recentSongs.sortedByDescending { 
            it.split(":")[1].toLong() 
        }.take(20).toMutableSet()
        
        preferences.edit().putStringSet(recentSongsKey, sortedSongs).apply()
    }
    
    private fun getRecentSessionCount(days: Int): Int {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        var count = 0
        
        for (i in 0 until days) {
            val date = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L)
            }
            val dayKey = "${date.get(Calendar.YEAR)}-${date.get(Calendar.DAY_OF_YEAR)}"
            count += preferences.getInt("daily_sessions_$dayKey", 0)
        }
        
        return count
    }
    
    private fun getRecentNotificationClicks(days: Int): Int {
        var count = 0
        
        for (i in 0 until days) {
            val date = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L)
            }
            val dayKey = "${date.get(Calendar.YEAR)}-${date.get(Calendar.DAY_OF_YEAR)}"
            count += preferences.getInt("daily_notifications_click_$dayKey", 0)
        }
        
        return count
    }
    
    private fun getRecentNotificationCount(days: Int): Int {
        var count = 0
        
        for (i in 0 until days) {
            val date = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L)
            }
            val dayKey = "${date.get(Calendar.YEAR)}-${date.get(Calendar.DAY_OF_YEAR)}"
            count += preferences.getInt("daily_notifications_click_$dayKey", 0)
            count += preferences.getInt("daily_notifications_dismiss_$dayKey", 0)
            count += preferences.getInt("daily_notifications_ignore_$dayKey", 0)
        }
        
        return count
    }
    
    private fun getTodayKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
    }
    
    /**
     * Clean up old tracking data to prevent storage bloat
     */
    fun cleanupOldData() {
        scope.launch {
            val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
            val editor = preferences.edit()
            
            // Remove old session data
            preferences.all.keys.filter { key ->
                key.startsWith("session_") && key.contains("_") && 
                key.substringAfterLast("_").toLongOrNull()?.let { it < cutoffTime } == true
            }.forEach { key ->
                editor.remove(key)
            }
            
            editor.apply()
        }
    }
}