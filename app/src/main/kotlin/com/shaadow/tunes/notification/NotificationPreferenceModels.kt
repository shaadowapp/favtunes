package com.shaadow.tunes.notification

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Data class for comprehensive notification preferences
 * Provides granular control over notification types, timing, and behavior
 */
data class NotificationPreferences(
    val masterEnabled: Boolean = true,
    val musicSuggestions: NotificationTypePreference = NotificationTypePreference(),
    val engagementReminders: NotificationTypePreference = NotificationTypePreference(),
    val marketingNotifications: NotificationTypePreference = NotificationTypePreference(),
    val trendingNotifications: NotificationTypePreference = NotificationTypePreference(),
    val personalizedSuggestions: NotificationTypePreference = NotificationTypePreference(),
    val ratingRequests: NotificationTypePreference = NotificationTypePreference(),
    val quietHours: QuietHours = QuietHours(),
    val frequencyControl: FrequencyControl = FrequencyControl(),
    val contentPreferences: ContentPreferences = ContentPreferences()
)

/**
 * Preferences for individual notification types
 */
data class NotificationTypePreference(
    val enabled: Boolean = true,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ledEnabled: Boolean = true
)

/**
 * Quiet hours configuration
 */
data class QuietHours(
    val enabled: Boolean = false,
    val startHour: Int = 22, // 10 PM
    val startMinute: Int = 0,
    val endHour: Int = 8,    // 8 AM
    val endMinute: Int = 0,
    val weekendsOnly: Boolean = false
)

/**
 * Frequency control settings
 */
data class FrequencyControl(
    val maxNotificationsPerDay: Int = 5,
    val minIntervalBetweenNotifications: Long = 2 * 60 * 60 * 1000L, // 2 hours in ms
    val adaptiveFrequency: Boolean = true, // Learn from user behavior
    val respectUserActivity: Boolean = true // Don't send when user is active
)

/**
 * Content preferences for notification personalization
 */
data class ContentPreferences(
    val personalityTone: PersonalityTone = PersonalityTone.ENCOURAGING_WITTY,
    val includeEmojis: Boolean = true,
    val savageMode: Boolean = false, // Extra savage content
    val musicGenrePreferences: List<String> = emptyList(),
    val avoidRepetition: Boolean = true
)

/**
 * Notification priority levels
 */
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH
}

/**
 * Notification history entry
 */
data class NotificationHistoryEntry(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val wasClicked: Boolean = false,
    val wasDismissed: Boolean = false,
    val dismissedQuickly: Boolean = false // Dismissed within 5 seconds
)

/**
 * Manager class for notification preferences
 * Handles storage, retrieval, and validation of notification preferences
 */
class NotificationPreferenceManager(private val context: Context) {
    
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val notificationHistory = mutableListOf<NotificationHistoryEntry>()
    
    companion object {
        // Preference keys
        const val PREF_MASTER_ENABLED = "notifications_master_enabled"
        const val PREF_MUSIC_SUGGESTIONS_ENABLED = "music_suggestions_enabled"
        const val PREF_ENGAGEMENT_REMINDERS_ENABLED = "engagement_reminders_enabled"
        const val PREF_MARKETING_NOTIFICATIONS_ENABLED = "marketing_notifications_enabled"
        const val PREF_TRENDING_NOTIFICATIONS_ENABLED = "trending_notifications_enabled"
        const val PREF_PERSONALIZED_SUGGESTIONS_ENABLED = "personalized_suggestions_enabled"
        const val PREF_RATING_REQUESTS_ENABLED = "rating_requests_enabled"
        
        // Quiet hours
        const val PREF_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        const val PREF_QUIET_HOURS_START_HOUR = "quiet_hours_start_hour"
        const val PREF_QUIET_HOURS_START_MINUTE = "quiet_hours_start_minute"
        const val PREF_QUIET_HOURS_END_HOUR = "quiet_hours_end_hour"
        const val PREF_QUIET_HOURS_END_MINUTE = "quiet_hours_end_minute"
        const val PREF_QUIET_HOURS_WEEKENDS_ONLY = "quiet_hours_weekends_only"
        
        // Frequency control
        const val PREF_MAX_NOTIFICATIONS_PER_DAY = "max_notifications_per_day"
        const val PREF_MIN_INTERVAL_BETWEEN_NOTIFICATIONS = "min_interval_between_notifications"
        const val PREF_ADAPTIVE_FREQUENCY = "adaptive_frequency"
        const val PREF_RESPECT_USER_ACTIVITY = "respect_user_activity"
        
        // Content preferences
        const val PREF_PERSONALITY_TONE = "personality_tone"
        const val PREF_INCLUDE_EMOJIS = "include_emojis"
        const val PREF_SAVAGE_MODE = "savage_mode"
        const val PREF_AVOID_REPETITION = "avoid_repetition"
    }
    
    /**
     * Get current notification preferences
     */
    fun getPreferences(): NotificationPreferences {
        return NotificationPreferences(
            masterEnabled = prefs.getBoolean(PREF_MASTER_ENABLED, true),
            musicSuggestions = getNotificationTypePreference(PREF_MUSIC_SUGGESTIONS_ENABLED),
            engagementReminders = getNotificationTypePreference(PREF_ENGAGEMENT_REMINDERS_ENABLED),
            marketingNotifications = getNotificationTypePreference(PREF_MARKETING_NOTIFICATIONS_ENABLED),
            trendingNotifications = getNotificationTypePreference(PREF_TRENDING_NOTIFICATIONS_ENABLED),
            personalizedSuggestions = getNotificationTypePreference(PREF_PERSONALIZED_SUGGESTIONS_ENABLED),
            ratingRequests = getNotificationTypePreference(PREF_RATING_REQUESTS_ENABLED),
            quietHours = getQuietHours(),
            frequencyControl = getFrequencyControl(),
            contentPreferences = getContentPreferences()
        )
    }
    
    /**
     * Save notification preferences
     */
    fun savePreferences(preferences: NotificationPreferences) {
        prefs.edit().apply {
            putBoolean(PREF_MASTER_ENABLED, preferences.masterEnabled)
            putBoolean(PREF_MUSIC_SUGGESTIONS_ENABLED, preferences.musicSuggestions.enabled)
            putBoolean(PREF_ENGAGEMENT_REMINDERS_ENABLED, preferences.engagementReminders.enabled)
            putBoolean(PREF_MARKETING_NOTIFICATIONS_ENABLED, preferences.marketingNotifications.enabled)
            putBoolean(PREF_TRENDING_NOTIFICATIONS_ENABLED, preferences.trendingNotifications.enabled)
            putBoolean(PREF_PERSONALIZED_SUGGESTIONS_ENABLED, preferences.personalizedSuggestions.enabled)
            putBoolean(PREF_RATING_REQUESTS_ENABLED, preferences.ratingRequests.enabled)
            
            // Quiet hours
            putBoolean(PREF_QUIET_HOURS_ENABLED, preferences.quietHours.enabled)
            putInt(PREF_QUIET_HOURS_START_HOUR, preferences.quietHours.startHour)
            putInt(PREF_QUIET_HOURS_START_MINUTE, preferences.quietHours.startMinute)
            putInt(PREF_QUIET_HOURS_END_HOUR, preferences.quietHours.endHour)
            putInt(PREF_QUIET_HOURS_END_MINUTE, preferences.quietHours.endMinute)
            putBoolean(PREF_QUIET_HOURS_WEEKENDS_ONLY, preferences.quietHours.weekendsOnly)
            
            // Frequency control
            putInt(PREF_MAX_NOTIFICATIONS_PER_DAY, preferences.frequencyControl.maxNotificationsPerDay)
            putLong(PREF_MIN_INTERVAL_BETWEEN_NOTIFICATIONS, preferences.frequencyControl.minIntervalBetweenNotifications)
            putBoolean(PREF_ADAPTIVE_FREQUENCY, preferences.frequencyControl.adaptiveFrequency)
            putBoolean(PREF_RESPECT_USER_ACTIVITY, preferences.frequencyControl.respectUserActivity)
            
            // Content preferences
            putString(PREF_PERSONALITY_TONE, preferences.contentPreferences.personalityTone.name)
            putBoolean(PREF_INCLUDE_EMOJIS, preferences.contentPreferences.includeEmojis)
            putBoolean(PREF_SAVAGE_MODE, preferences.contentPreferences.savageMode)
            putBoolean(PREF_AVOID_REPETITION, preferences.contentPreferences.avoidRepetition)
            
            apply()
        }
    }
    
    /**
     * Check if notifications should be sent based on current preferences and context
     */
    fun shouldSendNotification(notificationType: String): Boolean {
        val preferences = getPreferences()
        
        if (!preferences.masterEnabled) return false
        
        // Check quiet hours
        if (preferences.quietHours.enabled && isInQuietHours(preferences.quietHours)) {
            return false
        }
        
        // Check frequency limits
        if (!respectsFrequencyLimits(preferences.frequencyControl)) {
            return false
        }
        
        // Check specific notification type
        return when (notificationType) {
            "music_suggestion" -> preferences.musicSuggestions.enabled
            "engagement" -> preferences.engagementReminders.enabled
            "marketing" -> preferences.marketingNotifications.enabled
            "trending" -> preferences.trendingNotifications.enabled
            "personalized_suggestion" -> preferences.personalizedSuggestions.enabled
            "rating" -> preferences.ratingRequests.enabled
            else -> true
        }
    }
    
    /**
     * Add notification to history
     */
    fun addToHistory(entry: NotificationHistoryEntry) {
        notificationHistory.add(0, entry) // Add to beginning
        
        // Keep only last 100 notifications
        if (notificationHistory.size > 100) {
            notificationHistory.removeAt(notificationHistory.size - 1)
        }
    }
    
    /**
     * Get notification history
     */
    fun getHistory(): List<NotificationHistoryEntry> {
        return notificationHistory.toList()
    }
    
    /**
     * Learn from user behavior and adjust preferences
     */
    fun learnFromUserBehavior() {
        val preferences = getPreferences()
        
        if (!preferences.frequencyControl.adaptiveFrequency) return
        
        // Analyze recent notification interactions
        val recentHistory = notificationHistory.take(20)
        val dismissedQuicklyRate = recentHistory.count { it.dismissedQuickly } / recentHistory.size.toFloat()
        
        // If user dismisses notifications quickly frequently, reduce frequency
        if (dismissedQuicklyRate > 0.7f) {
            val updatedFrequencyControl = preferences.frequencyControl.copy(
                maxNotificationsPerDay = maxOf(1, preferences.frequencyControl.maxNotificationsPerDay - 1),
                minIntervalBetweenNotifications = preferences.frequencyControl.minIntervalBetweenNotifications * 2
            )
            
            savePreferences(preferences.copy(frequencyControl = updatedFrequencyControl))
        }
    }
    
    private fun getNotificationTypePreference(key: String): NotificationTypePreference {
        return NotificationTypePreference(
            enabled = prefs.getBoolean(key, true)
        )
    }
    
    private fun getQuietHours(): QuietHours {
        return QuietHours(
            enabled = prefs.getBoolean(PREF_QUIET_HOURS_ENABLED, false),
            startHour = prefs.getInt(PREF_QUIET_HOURS_START_HOUR, 22),
            startMinute = prefs.getInt(PREF_QUIET_HOURS_START_MINUTE, 0),
            endHour = prefs.getInt(PREF_QUIET_HOURS_END_HOUR, 8),
            endMinute = prefs.getInt(PREF_QUIET_HOURS_END_MINUTE, 0),
            weekendsOnly = prefs.getBoolean(PREF_QUIET_HOURS_WEEKENDS_ONLY, false)
        )
    }
    
    private fun getFrequencyControl(): FrequencyControl {
        return FrequencyControl(
            maxNotificationsPerDay = prefs.getInt(PREF_MAX_NOTIFICATIONS_PER_DAY, 5),
            minIntervalBetweenNotifications = prefs.getLong(PREF_MIN_INTERVAL_BETWEEN_NOTIFICATIONS, 2 * 60 * 60 * 1000L),
            adaptiveFrequency = prefs.getBoolean(PREF_ADAPTIVE_FREQUENCY, true),
            respectUserActivity = prefs.getBoolean(PREF_RESPECT_USER_ACTIVITY, true)
        )
    }
    
    private fun getContentPreferences(): ContentPreferences {
        val toneString = prefs.getString(PREF_PERSONALITY_TONE, PersonalityTone.ENCOURAGING_WITTY.name)
        val tone = try {
            PersonalityTone.valueOf(toneString ?: PersonalityTone.ENCOURAGING_WITTY.name)
        } catch (e: IllegalArgumentException) {
            PersonalityTone.ENCOURAGING_WITTY
        }
        
        return ContentPreferences(
            personalityTone = tone,
            includeEmojis = prefs.getBoolean(PREF_INCLUDE_EMOJIS, true),
            savageMode = prefs.getBoolean(PREF_SAVAGE_MODE, false),
            avoidRepetition = prefs.getBoolean(PREF_AVOID_REPETITION, true)
        )
    }
    
    private fun isInQuietHours(quietHours: QuietHours): Boolean {
        val now = java.util.Calendar.getInstance()
        val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(java.util.Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMinute
        
        val startTimeMinutes = quietHours.startHour * 60 + quietHours.startMinute
        val endTimeMinutes = quietHours.endHour * 60 + quietHours.endMinute
        
        // Handle overnight quiet hours (e.g., 22:00 to 08:00)
        return if (startTimeMinutes > endTimeMinutes) {
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
        } else {
            currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes
        }
    }
    
    private fun respectsFrequencyLimits(frequencyControl: FrequencyControl): Boolean {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 24 * 60 * 60 * 1000L
        
        // Count notifications sent in the last 24 hours
        val notificationsToday = notificationHistory.count { it.timestamp > oneDayAgo }
        
        if (notificationsToday >= frequencyControl.maxNotificationsPerDay) {
            return false
        }
        
        // Check minimum interval
        val lastNotification = notificationHistory.firstOrNull()
        if (lastNotification != null) {
            val timeSinceLastNotification = now - lastNotification.timestamp
            if (timeSinceLastNotification < frequencyControl.minIntervalBetweenNotifications) {
                return false
            }
        }
        
        return true
    }
}