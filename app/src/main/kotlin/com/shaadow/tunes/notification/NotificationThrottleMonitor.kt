package com.shaadow.tunes.notification

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Monitor and debug notification throttling to ensure proper spacing between local notifications
 */
class NotificationThrottleMonitor(private val context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences("notification_scheduler", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
    
    companion object {
        private const val TAG = "NotificationThrottle"
        private const val PREF_LAST_LOCAL_NOTIFICATION = "last_local_notification_time"
        private const val MIN_GAP_HOURS = 2
        private const val PREFERRED_GAP_HOURS = 3
    }
    
    /**
     * Log current throttling status for debugging
     */
    fun logThrottleStatus() {
        val lastNotificationTime = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastMs = currentTime - lastNotificationTime
        val timeSinceLastHours = timeSinceLastMs / (1000 * 60 * 60.0)
        
        val canDeliverNow = timeSinceLastMs >= (MIN_GAP_HOURS * 60 * 60 * 1000L)
        val timeUntilNext = if (canDeliverNow) 0L else 
            (lastNotificationTime + PREFERRED_GAP_HOURS * 60 * 60 * 1000L) - currentTime
        
        Log.d(TAG, "=== NOTIFICATION THROTTLE STATUS ===")
        Log.d(TAG, "Last local notification: ${if (lastNotificationTime > 0) dateFormat.format(Date(lastNotificationTime)) else "Never"}")
        Log.d(TAG, "Time since last: ${String.format("%.1f", timeSinceLastHours)} hours")
        Log.d(TAG, "Can deliver now: $canDeliverNow")
        Log.d(TAG, "Time until next slot: ${formatDuration(timeUntilNext)}")
        Log.d(TAG, "=====================================")
    }
    
    /**
     * Check if multiple notifications were sent too close together
     */
    fun checkForViolations(): List<ThrottleViolation> {
        val violations = mutableListOf<ThrottleViolation>()
        
        // Check recent notification history from preferences
        val notificationHistory = getRecentNotificationHistory()
        
        for (i in 1 until notificationHistory.size) {
            val current = notificationHistory[i]
            val previous = notificationHistory[i - 1]
            val gap = current.timestamp - previous.timestamp
            val gapHours = gap / (1000 * 60 * 60.0)
            
            if (gapHours < MIN_GAP_HOURS) {
                violations.add(
                    ThrottleViolation(
                        previousNotification = previous,
                        currentNotification = current,
                        actualGapHours = gapHours,
                        requiredGapHours = MIN_GAP_HOURS.toDouble()
                    )
                )
            }
        }
        
        return violations
    }
    
    /**
     * Get recent notification history for analysis
     */
    private fun getRecentNotificationHistory(): List<NotificationRecord> {
        val history = mutableListOf<NotificationRecord>()
        
        // Get last few notification timestamps from preferences
        val lastNotification = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
        if (lastNotification > 0) {
            history.add(NotificationRecord("local", lastNotification))
        }
        
        // Add any other stored notification records
        val storedHistory = preferences.getString("notification_history", "") ?: ""
        if (storedHistory.isNotEmpty()) {
            storedHistory.split(";").forEach { record ->
                val parts = record.split(":")
                if (parts.size >= 2) {
                    val type = parts[0]
                    val timestamp = parts[1].toLongOrNull()
                    if (timestamp != null) {
                        history.add(NotificationRecord(type, timestamp))
                    }
                }
            }
        }
        
        return history.sortedBy { it.timestamp }
    }
    
    /**
     * Record a notification delivery for monitoring
     */
    fun recordNotificationDelivery(type: String, timestamp: Long = System.currentTimeMillis()) {
        // Update the main tracking
        if (type == "local") {
            preferences.edit()
                .putLong(PREF_LAST_LOCAL_NOTIFICATION, timestamp)
                .apply()
        }
        
        // Add to history for analysis
        val currentHistory = preferences.getString("notification_history", "") ?: ""
        val newRecord = "$type:$timestamp"
        val updatedHistory = if (currentHistory.isEmpty()) {
            newRecord
        } else {
            "$currentHistory;$newRecord"
        }
        
        // Keep only last 10 records to prevent storage bloat
        val records = updatedHistory.split(";")
        val trimmedHistory = records.takeLast(10).joinToString(";")
        
        preferences.edit()
            .putString("notification_history", trimmedHistory)
            .apply()
        
        Log.d(TAG, "Recorded $type notification at ${dateFormat.format(Date(timestamp))}")
    }
    
    /**
     * Clear notification history (for testing/debugging)
     */
    fun clearHistory() {
        preferences.edit()
            .remove(PREF_LAST_LOCAL_NOTIFICATION)
            .remove("notification_history")
            .apply()
        Log.d(TAG, "Notification history cleared")
    }
    
    /**
     * Get a summary report of notification throttling
     */
    fun getThrottleSummary(): String {
        val violations = checkForViolations()
        val status = if (violations.isEmpty()) "✅ No throttling violations" else "⚠️ ${violations.size} violations found"
        
        val lastNotificationTime = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
        val timeSinceLastHours = if (lastNotificationTime > 0) {
            (System.currentTimeMillis() - lastNotificationTime) / (1000 * 60 * 60.0)
        } else {
            Double.MAX_VALUE
        }
        
        return buildString {
            appendLine("Notification Throttle Summary")
            appendLine("Status: $status")
            appendLine("Last notification: ${String.format("%.1f", timeSinceLastHours)} hours ago")
            appendLine("Min gap required: $MIN_GAP_HOURS hours")
            appendLine("Preferred gap: $PREFERRED_GAP_HOURS hours")
            
            if (violations.isNotEmpty()) {
                appendLine("\nViolations:")
                violations.forEach { violation ->
                    appendLine("- Gap of ${String.format("%.1f", violation.actualGapHours)}h (required: ${violation.requiredGapHours}h)")
                }
            }
        }
    }
    
    private fun formatDuration(milliseconds: Long): String {
        if (milliseconds <= 0) return "Now"
        
        val hours = milliseconds / (1000 * 60 * 60)
        val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}

/**
 * Data classes for monitoring
 */
data class NotificationRecord(
    val type: String,
    val timestamp: Long
)

data class ThrottleViolation(
    val previousNotification: NotificationRecord,
    val currentNotification: NotificationRecord,
    val actualGapHours: Double,
    val requiredGapHours: Double
)