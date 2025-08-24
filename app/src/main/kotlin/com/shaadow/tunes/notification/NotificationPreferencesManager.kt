package com.shaadow.tunes.notification

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manager class for notification preferences and scheduling
 * Provides methods to check notification settings and schedule notifications
 */
class NotificationPreferencesService(private val context: Context) {
    
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val preferenceManager = NotificationPreferenceManager(context)
    private val scope = CoroutineScope(Dispatchers.Main)
    
    companion object {
        const val PREF_ENGAGEMENT_REMINDERS = "engagement_reminders_enabled"
        const val PREF_MARKETING_NOTIFICATIONS = "marketing_notifications_enabled"
    }
    
    /**
     * Check if a specific notification type is enabled
     */
    fun isNotificationTypeEnabled(notificationType: NotificationType): Boolean {
        return when (notificationType) {
            NotificationType.ENGAGEMENT -> prefs.getBoolean(PREF_ENGAGEMENT_REMINDERS, true)
            NotificationType.MUSIC_SUGGESTIONS -> prefs.getBoolean(NotificationPreferenceManager.PREF_MUSIC_SUGGESTIONS_ENABLED, true)
            NotificationType.MARKETING -> prefs.getBoolean(PREF_MARKETING_NOTIFICATIONS, true)
            NotificationType.TRENDING -> prefs.getBoolean(NotificationPreferenceManager.PREF_TRENDING_NOTIFICATIONS_ENABLED, true)
            NotificationType.PERSONALIZED_SUGGESTIONS -> prefs.getBoolean(NotificationPreferenceManager.PREF_PERSONALIZED_SUGGESTIONS_ENABLED, true)
            NotificationType.RATING_REQUESTS -> prefs.getBoolean(NotificationPreferenceManager.PREF_RATING_REQUESTS_ENABLED, true)
            NotificationType.GENERAL -> prefs.getBoolean(NotificationPreferenceManager.PREF_MASTER_ENABLED, true)
        }
    }
    
    /**
     * Enable or disable a specific notification type
     */
    fun setNotificationTypeEnabled(notificationType: NotificationType, enabled: Boolean) {
        val key = when (notificationType) {
            NotificationType.ENGAGEMENT -> PREF_ENGAGEMENT_REMINDERS
            NotificationType.MUSIC_SUGGESTIONS -> NotificationPreferenceManager.PREF_MUSIC_SUGGESTIONS_ENABLED
            NotificationType.MARKETING -> PREF_MARKETING_NOTIFICATIONS
            NotificationType.TRENDING -> NotificationPreferenceManager.PREF_TRENDING_NOTIFICATIONS_ENABLED
            NotificationType.PERSONALIZED_SUGGESTIONS -> NotificationPreferenceManager.PREF_PERSONALIZED_SUGGESTIONS_ENABLED
            NotificationType.RATING_REQUESTS -> NotificationPreferenceManager.PREF_RATING_REQUESTS_ENABLED
            NotificationType.GENERAL -> NotificationPreferenceManager.PREF_MASTER_ENABLED
        }
        
        prefs.edit().putBoolean(key, enabled).apply()
    }
    
    /**
     * Schedule engagement notifications
     */
    suspend fun scheduleEngagementNotifications() {
        if (!isNotificationTypeEnabled(NotificationType.ENGAGEMENT)) return
        
        // Implementation would schedule work manager tasks
        // For now, just log the scheduling
        android.util.Log.d("NotificationPreferences", "Scheduling engagement notifications")
    }
    
    /**
     * Schedule daily music suggestions
     */
    suspend fun scheduleDailySuggestions() {
        if (!isNotificationTypeEnabled(NotificationType.MUSIC_SUGGESTIONS)) return
        
        // Implementation would schedule work manager tasks
        // For now, just log the scheduling
        android.util.Log.d("NotificationPreferences", "Scheduling daily suggestions")
    }
    
    /**
     * Schedule marketing notifications
     */
    suspend fun scheduleMarketingNotification() {
        if (!isNotificationTypeEnabled(NotificationType.MARKETING)) return
        
        // Implementation would schedule work manager tasks
        // For now, just log the scheduling
        android.util.Log.d("NotificationPreferences", "Scheduling marketing notifications")
    }
    
    /**
     * Get current notification preferences
     */
    fun getPreferences(): NotificationPreferences {
        return preferenceManager.getPreferences()
    }
    
    /**
     * Save notification preferences
     */
    fun savePreferences(preferences: NotificationPreferences) {
        preferenceManager.savePreferences(preferences)
    }
    
    /**
     * Check if notifications should be sent based on current context
     */
    fun shouldSendNotification(notificationType: NotificationType): Boolean {
        return preferenceManager.shouldSendNotification(notificationType.name.lowercase())
    }
}