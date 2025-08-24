package com.shaadow.tunes.notification

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class to manage and monitor notification throttling
 * Ensures minimum 2-3 hour gaps between local notifications
 */
class NotificationThrottleManager(private val context: Context) {
    
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        private const val PREF_LAST_LOCAL_NOTIFICATION = "last_local_notification_time"
        private const val PREF_NOTIFICATION_HISTORY = "notification_history"
        private const val MIN_GAP_MINUTES = 120 // Minimum 2 hours between notifications (increased from 15 minutes)
        private const val PREFERRED_GAP_MINUTES = 180 // Preferred 3 hours between notifications (increased from 30 minutes)
        private const val MAX_DAILY_NOTIFICATIONS = 4 // Maximum 4 notifications per day (reduced from 20)
        private const val MAX_HISTORY_ENTRIES = 50
        
        private val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
    }
    
    /**
     * Check if a local notification can be sent now
     */
    fun canSendLocalNotification(): Boolean {
        val lastNotificationTime = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastNotification = currentTime - lastNotificationTime
        val minGapMs = MIN_GAP_MINUTES * 60 * 1000L // Convert minutes to milliseconds
        
        return timeSinceLastNotification >= minGapMs
    }
    
    /**
     * Get time remaining until next notification can be sent (in milliseconds)
     */
    fun getTimeUntilNextNotification(): Long {
        if (canSendLocalNotification()) return 0L
        
        val lastNotificationTime = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
        val currentTime = System.currentTimeMillis()
        val minGapMs = PREFERRED_GAP_MINUTES * 60 * 1000L // Use preferred gap in minutes
        val nextAllowedTime = lastNotificationTime + minGapMs
        
        return maxOf(0L, nextAllowedTime - currentTime)
    }
    
    /**
     * Record that a local notification was sent
     */
    fun recordLocalNotificationSent(contentType: String, title: String) {
        val currentTime = System.currentTimeMillis()
        
        // Update last notification time
        preferences.edit()
            .putLong(PREF_LAST_LOCAL_NOTIFICATION, currentTime)
            .apply()
        
        // Add to history
        addToNotificationHistory(contentType, title, currentTime)
        
        // Log for debugging
        android.util.Log.i("NotificationThrottle", 
            "Local notification sent: $contentType - '$title' at ${dateFormat.format(Date(currentTime))}")
    }
    
    /**
     * Record that a push notification was received (doesn't affect throttling)
     */
    fun recordPushNotificationReceived(contentType: String, title: String) {
        val currentTime = System.currentTimeMillis()
        
        // Add to history but don't update throttling
        addToNotificationHistory("PUSH:$contentType", title, currentTime)
        
        // Log for debugging
        android.util.Log.i("NotificationThrottle", 
            "Push notification received: $contentType - '$title' at ${dateFormat.format(Date(currentTime))}")
    }
    
    /**
     * Get formatted time until next notification
     */
    fun getFormattedTimeUntilNext(): String {
        val timeMs = getTimeUntilNextNotification()
        if (timeMs == 0L) return "Now"
        
        val hours = timeMs / (1000 * 60 * 60)
        val minutes = (timeMs % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
    
    /**
     * Get recent notification history for debugging
     */
    fun getNotificationHistory(): List<NotificationHistoryEntry> {
        val historyJson = preferences.getString(PREF_NOTIFICATION_HISTORY, "[]") ?: "[]"
        return try {
            parseNotificationHistory(historyJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get throttling status summary
     */
    fun getThrottleStatus(): ThrottleStatus {
        val canSend = canSendLocalNotification()
        val timeUntilNext = getTimeUntilNextNotification()
        val lastNotificationTime = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
        val history = getNotificationHistory()
        
        return ThrottleStatus(
            canSendNow = canSend,
            timeUntilNextMs = timeUntilNext,
            timeUntilNextFormatted = getFormattedTimeUntilNext(),
            lastNotificationTime = lastNotificationTime,
            lastNotificationFormatted = if (lastNotificationTime > 0) dateFormat.format(Date(lastNotificationTime)) else "Never",
            recentNotifications = history.take(5)
        )
    }
    
    /**
     * Clear notification history (for testing/debugging)
     */
    fun clearHistory() {
        preferences.edit()
            .remove(PREF_NOTIFICATION_HISTORY)
            .remove(PREF_LAST_LOCAL_NOTIFICATION)
            .apply()
        
        android.util.Log.i("NotificationThrottle", "Notification history cleared")
    }
    
    private fun addToNotificationHistory(contentType: String, title: String, timestamp: Long) {
        val history = getNotificationHistory().toMutableList()
        
        // Add new entry
        history.add(0, NotificationHistoryEntry(
            id = "throttle_${System.currentTimeMillis()}",
            type = contentType,
            title = title,
            body = "",
            timestamp = timestamp
        ))
        
        // Keep only recent entries
        val trimmedHistory = history.take(MAX_HISTORY_ENTRIES)
        
        // Save back to preferences
        val historyJson = serializeNotificationHistory(trimmedHistory)
        preferences.edit()
            .putString(PREF_NOTIFICATION_HISTORY, historyJson)
            .apply()
    }
    
    private fun parseNotificationHistory(json: String): List<NotificationHistoryEntry> {
        // Simple JSON parsing - in production you might want to use a proper JSON library
        val entries = mutableListOf<NotificationHistoryEntry>()
        
        if (json.startsWith("[") && json.endsWith("]")) {
            val content = json.substring(1, json.length - 1)
            if (content.isNotBlank()) {
                val items = content.split("},{")
                for (item in items) {
                    val cleanItem = item.replace("{", "").replace("}", "")
                    val parts = cleanItem.split(",")
                    if (parts.size >= 3) {
                        try {
                            val contentType = parts[0].split(":")[1].trim('"')
                            val title = parts[1].split(":")[1].trim('"')
                            val timestamp = parts[2].split(":")[1].toLong()
                            entries.add(NotificationHistoryEntry(
                                id = "parsed_${timestamp}",
                                type = contentType,
                                title = title,
                                body = "",
                                timestamp = timestamp
                            ))
                        } catch (e: Exception) {
                            // Skip malformed entries
                        }
                    }
                }
            }
        }
        
        return entries
    }
    
    private fun serializeNotificationHistory(history: List<NotificationHistoryEntry>): String {
        val items = history.map { entry ->
            """{"type":"${entry.type}","title":"${entry.title}","time":${entry.timestamp}}"""
        }
        return "[${items.joinToString(",")}]"
    }
}

/**
 * Data classes for notification throttling
 */

data class ThrottleStatus(
    val canSendNow: Boolean,
    val timeUntilNextMs: Long,
    val timeUntilNextFormatted: String,
    val lastNotificationTime: Long,
    val lastNotificationFormatted: String,
    val recentNotifications: List<NotificationHistoryEntry>
)