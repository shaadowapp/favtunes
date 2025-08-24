package com.shaadow.tunes.notification

import android.content.Context
import android.content.SharedPreferences
import android.os.BatteryManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.abs

/**
 * Intelligent notification scheduling system that learns user behavior
 * and optimizes notification delivery timing for maximum engagement
 */
class IntelligentNotificationScheduler(
    private val context: Context,
    private val preferences: SharedPreferences = context.getSharedPreferences("notification_scheduler", Context.MODE_PRIVATE)
) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val behaviorAnalyzer = UserBehaviorAnalyzer(preferences)
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    companion object {
        private const val MIN_BATTERY_LEVEL = 20
        private const val OPTIMAL_BATTERY_LEVEL = 50
        private const val NOTIFICATION_COOLDOWN_MS = 3 * 60 * 60 * 1000L // 3 hours minimum gap (increased from 2)
        private const val PREFERRED_NOTIFICATION_GAP_MS = 4 * 60 * 60 * 1000L // 4 hours preferred gap (increased from 3)
        private const val MAX_DAILY_NOTIFICATIONS = 3 // Further reduced from 6 to prevent spam
        private const val PREF_LAST_LOCAL_NOTIFICATION = "last_local_notification_time"
        private const val PREF_NOTIFICATION_QUEUE = "notification_queue"
    }
    
    /**
     * Schedule a notification with intelligent timing
     */
    suspend fun scheduleNotification(
        content: NotificationContent,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        deliveryWindow: DeliveryWindow? = null
    ): ScheduleResult {
        return withContext(Dispatchers.Default) {
            val optimalTime = calculateOptimalDeliveryTime(content.contentType, priority, deliveryWindow)
            val canDeliver = canDeliverNotification(priority)
            
            if (canDeliver && optimalTime <= System.currentTimeMillis()) {
                // Deliver immediately
                deliverNotification(content)
                ScheduleResult.DeliveredImmediately
            } else if (canDeliver) {
                // Schedule for later
                scheduleDelayedNotification(content, optimalTime)
                ScheduleResult.ScheduledForLater(optimalTime)
            } else {
                // Cannot deliver due to constraints
                ScheduleResult.Blocked(getBlockingReason())
            }
        }
    }
    
    /**
     * Calculate optimal delivery time based on user behavior and system state
     * CRITICAL: Ensures minimum gap between local notifications
     */
    private suspend fun calculateOptimalDeliveryTime(
        contentType: String,
        priority: NotificationPriority,
        deliveryWindow: DeliveryWindow?
    ): Long {
        val userPatterns = behaviorAnalyzer.getUserEngagementPatterns()
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        // FIRST: Check minimum gap requirement for local notifications
        val nextAvailableTime = getNextAvailableLocalNotificationTime()
        
        // Get user's optimal engagement hours for this content type
        val optimalHours = userPatterns.getOptimalHoursForContent(contentType)
        
        // If we're in an optimal hour and conditions are good, but must respect gap
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        if (optimalHours.contains(currentHour) && isOptimalDeliveryCondition() && nextAvailableTime <= currentTime) {
            return currentTime
        }
        
        // Find next optimal delivery window that respects the minimum gap
        val nextOptimalTime = findNextOptimalWindow(optimalHours, deliveryWindow)
        val respectfulDeliveryTime = maxOf(nextOptimalTime, nextAvailableTime)
        
        // Apply priority adjustments while respecting minimum gap
        return when (priority) {
            NotificationPriority.HIGH -> {
                // High priority can reduce gap but still maintain minimum 30 minutes
                val minHighPriorityGap = 30 * 60 * 1000L // 30 minutes for high priority
                val lastNotification = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
                maxOf(respectfulDeliveryTime, lastNotification + minHighPriorityGap)
            }
            NotificationPriority.NORMAL -> respectfulDeliveryTime
            NotificationPriority.LOW -> maxOf(respectfulDeliveryTime, currentTime + 60 * 60 * 1000L) // Min 1 hour delay
        }
    }
    
    /**
     * Check if notification can be delivered based on current constraints
     */
    private fun canDeliverNotification(priority: NotificationPriority): Boolean {
        // CRITICAL: Check minimum gap between local notifications (except push notifications)
        if (!canDeliverLocalNotification() && priority != NotificationPriority.HIGH) {
            return false
        }
        
        // Check daily notification limit
        if (getTodayNotificationCount() >= MAX_DAILY_NOTIFICATIONS && priority != NotificationPriority.HIGH) {
            return false
        }
        
        // Check quiet hours
        if (isInQuietHours() && priority != NotificationPriority.HIGH) {
            return false
        }
        
        // Check battery level for low priority notifications
        if (priority == NotificationPriority.LOW && getBatteryLevel() < MIN_BATTERY_LEVEL) {
            return false
        }
        
        return true
    }
    
    /**
     * Check if we can deliver a local notification based on minimum gap requirement
     * This ensures at least 2-3 hours between local notifications
     */
    private fun canDeliverLocalNotification(): Boolean {
        val lastLocalNotificationTime = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastNotification = currentTime - lastLocalNotificationTime
        
        // If no previous notification, allow first one but add a small delay to prevent simultaneous delivery
        if (lastLocalNotificationTime == 0L) {
            // For first notification, check if we've delivered one in the last 5 minutes to prevent rapid-fire
            val recentDeliveryCheck = preferences.getLong("temp_delivery_lock", 0L)
            val timeSinceRecentDelivery = currentTime - recentDeliveryCheck
            return timeSinceRecentDelivery >= (5 * 60 * 1000L) // 5 minute minimum between any notifications
        }
        
        // Enforce minimum 2-hour gap between local notifications
        val canDeliver = timeSinceLastNotification >= NOTIFICATION_COOLDOWN_MS
        
        android.util.Log.d("NotificationScheduler", 
            "Throttle check: lastNotification=${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(lastLocalNotificationTime))}, " +
            "timeSince=${timeSinceLastNotification / (1000 * 60)}min, canDeliver=$canDeliver")
        
        return canDeliver
    }
    
    /**
     * Get the next available time slot for local notification delivery
     */
    private fun getNextAvailableLocalNotificationTime(): Long {
        val lastLocalNotificationTime = preferences.getLong(PREF_LAST_LOCAL_NOTIFICATION, 0L)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastNotification = currentTime - lastLocalNotificationTime
        
        return if (timeSinceLastNotification >= NOTIFICATION_COOLDOWN_MS) {
            currentTime // Can deliver now
        } else {
            // Must wait for the cooldown period to complete
            lastLocalNotificationTime + PREFERRED_NOTIFICATION_GAP_MS // Use 3-hour preferred gap
        }
    }
    
    /**
     * Check if current conditions are optimal for notification delivery
     */
    private fun isOptimalDeliveryCondition(): Boolean {
        val batteryLevel = getBatteryLevel()
        val hasGoodNetwork = hasGoodNetworkConnection()
        val isCharging = isDeviceCharging()
        
        return (batteryLevel > OPTIMAL_BATTERY_LEVEL || isCharging) && hasGoodNetwork
    }
    
    /**
     * Find the next optimal delivery window
     */
    private fun findNextOptimalWindow(optimalHours: List<Int>, deliveryWindow: DeliveryWindow?): Long {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Filter optimal hours by delivery window if specified
        val availableHours = if (deliveryWindow != null) {
            optimalHours.filter { hour ->
                hour >= deliveryWindow.startHour && hour <= deliveryWindow.endHour
            }
        } else {
            optimalHours
        }
        
        if (availableHours.isEmpty()) {
            // No optimal hours available, use default scheduling
            return System.currentTimeMillis() + 2 * 60 * 60 * 1000L // 2 hours from now
        }
        
        // Find next available optimal hour
        val nextHour = availableHours.find { it > currentHour } ?: availableHours.first()
        
        calendar.set(Calendar.HOUR_OF_DAY, nextHour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        // If the hour is earlier than current hour, it's for tomorrow
        if (nextHour <= currentHour) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * Record user interaction with notification for learning
     */
    fun recordNotificationInteraction(
        contentType: String,
        deliveryTime: Long,
        interactionType: NotificationInteraction
    ) {
        scope.launch {
            behaviorAnalyzer.recordInteraction(contentType, deliveryTime, interactionType)
        }
    }
    
    /**
     * Update user behavior patterns based on app usage
     */
    fun recordAppUsage(startTime: Long, endTime: Long, activityType: String) {
        scope.launch {
            behaviorAnalyzer.recordAppUsage(startTime, endTime, activityType)
        }
    }
    
    /**
     * Record push notification delivery (Firebase/OneSignal) - doesn't affect local notification timing
     */
    fun recordPushNotificationDelivery(notificationId: String, contentType: String) {
        val currentTime = System.currentTimeMillis()
        preferences.edit()
            .putLong("last_push_notification_time", currentTime)
            .apply()
        
        // Log for debugging
        android.util.Log.d("NotificationScheduler", 
            "Push notification delivered: $contentType at ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(currentTime))}")
    }
    
    /**
     * Get time until next local notification can be sent
     */
    fun getTimeUntilNextLocalNotification(): Long {
        val nextAvailableTime = getNextAvailableLocalNotificationTime()
        val currentTime = System.currentTimeMillis()
        return maxOf(0L, nextAvailableTime - currentTime)
    }
    
    /**
     * Check if there are any pending local notifications that should be delivered
     */
    fun checkPendingNotifications(): List<String> {
        val scheduledNotifications = preferences.getStringSet(PREF_NOTIFICATION_QUEUE, mutableSetOf()) ?: mutableSetOf()
        val currentTime = System.currentTimeMillis()
        val readyNotifications = mutableListOf<String>()
        
        scheduledNotifications.forEach { notification ->
            val parts = notification.split(":")
            if (parts.size >= 2) {
                val deliveryTime = parts[1].toLongOrNull()
                if (deliveryTime != null && deliveryTime <= currentTime && canDeliverLocalNotification()) {
                    readyNotifications.add(notification)
                }
            }
        }
        
        return readyNotifications
    }
    
    // Private helper methods
    
    private fun deliverNotification(content: NotificationContent) {
        // This would integrate with the actual notification delivery system
        // For now, we'll just record the delivery
        recordNotificationDelivery(content)
    }
    
    private fun scheduleDelayedNotification(content: NotificationContent, deliveryTime: Long) {
        // This would integrate with Android's AlarmManager or WorkManager
        // For now, we'll just record the scheduling
        recordScheduledNotification(content, deliveryTime)
    }
    
    private fun getTodayNotificationCount(): Int {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        return preferences.getInt("notifications_today_$today", 0)
    }
    
    private fun isInCooldownPeriod(): Boolean {
        // This method is now replaced by canDeliverLocalNotification() for better control
        return !canDeliverLocalNotification()
    }
    
    private fun isInQuietHours(): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val quietStartHour = preferences.getInt("quiet_hours_start", 22) // 10 PM default
        val quietEndHour = preferences.getInt("quiet_hours_end", 8) // 8 AM default
        
        return if (quietStartHour > quietEndHour) {
            // Quiet hours span midnight (e.g., 10 PM to 8 AM)
            currentHour >= quietStartHour || currentHour < quietEndHour
        } else {
            // Quiet hours within same day
            currentHour >= quietStartHour && currentHour < quietEndHour
        }
    }
    
    private fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    private fun isDeviceCharging(): Boolean {
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL
    }
    
    private fun hasGoodNetworkConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    private fun getBlockingReason(): String {
        return when {
            getTodayNotificationCount() >= MAX_DAILY_NOTIFICATIONS -> "Daily notification limit reached"
            isInCooldownPeriod() -> "In cooldown period"
            isInQuietHours() -> "In quiet hours"
            getBatteryLevel() < MIN_BATTERY_LEVEL -> "Low battery"
            else -> "Unknown constraint"
        }
    }
    
    private fun recordNotificationDelivery(content: NotificationContent) {
        val currentTime = System.currentTimeMillis()
        preferences.edit()
            .putLong("last_notification_time", currentTime)
            .putLong(PREF_LAST_LOCAL_NOTIFICATION, currentTime) // Track local notifications separately
            .putLong("temp_delivery_lock", currentTime) // Temporary lock to prevent rapid-fire
            .putInt("notifications_today_${getTodayKey()}", getTodayNotificationCount() + 1)
            .apply()
        
        // Log for debugging
        android.util.Log.d("NotificationScheduler", 
            "🚨 LOCAL NOTIFICATION DELIVERED: ${content.contentType} - '${content.title}' at ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(currentTime))}")
    }
    
    private fun recordScheduledNotification(content: NotificationContent, deliveryTime: Long) {
        // Record scheduled notification for tracking
        val scheduledNotifications = preferences.getStringSet("scheduled_notifications", mutableSetOf()) ?: mutableSetOf()
        scheduledNotifications.add("${content.contentType}:$deliveryTime")
        preferences.edit().putStringSet("scheduled_notifications", scheduledNotifications).apply()
    }
    
    private fun getTodayKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
    }
}

/**
 * Analyzes user behavior patterns to optimize notification timing
 */
class UserBehaviorAnalyzer(private val preferences: SharedPreferences) {
    
    fun getUserEngagementPatterns(): UserEngagementPatterns {
        val engagementData = preferences.getString("engagement_patterns", null)
        return if (engagementData != null) {
            // Parse stored engagement patterns
            parseEngagementPatterns(engagementData)
        } else {
            // Return default patterns
            UserEngagementPatterns.getDefault()
        }
    }
    
    fun recordInteraction(contentType: String, deliveryTime: Long, interactionType: NotificationInteraction) {
        val calendar = Calendar.getInstance().apply { timeInMillis = deliveryTime }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Update engagement patterns
        val key = "interaction_${contentType}_$hour"
        val currentCount = preferences.getInt(key, 0)
        preferences.edit().putInt(key, currentCount + 1).apply()
        
        // Record interaction type
        val interactionKey = "interaction_type_${contentType}_${interactionType.name}"
        val interactionCount = preferences.getInt(interactionKey, 0)
        preferences.edit().putInt(interactionKey, interactionCount + 1).apply()
    }
    
    fun recordAppUsage(startTime: Long, endTime: Long, activityType: String) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val startHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        calendar.timeInMillis = endTime
        val endHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Record usage patterns for each hour
        for (hour in startHour..endHour) {
            val key = "usage_hour_$hour"
            val currentUsage = preferences.getLong(key, 0L)
            val sessionDuration = if (hour == startHour && hour == endHour) {
                endTime - startTime
            } else if (hour == startHour) {
                (hour + 1) * 60 * 60 * 1000L - startTime
            } else if (hour == endHour) {
                endTime - hour * 60 * 60 * 1000L
            } else {
                60 * 60 * 1000L // Full hour
            }
            
            preferences.edit().putLong(key, currentUsage + sessionDuration).apply()
        }
    }
    
    private fun parseEngagementPatterns(data: String): UserEngagementPatterns {
        // Simple parsing implementation - in production this would be more robust
        return UserEngagementPatterns.getDefault()
    }
}

/**
 * Data classes for scheduling system
 */
data class UserEngagementPatterns(
    val optimalHoursByContent: Map<String, List<Int>>,
    val dismissalRateByHour: Map<Int, Double>,
    val engagementRateByContent: Map<String, Double>
) {
    fun getOptimalHoursForContent(contentType: String): List<Int> {
        return optimalHoursByContent[contentType] ?: listOf(9, 12, 18, 20) // Default optimal hours
    }
    
    companion object {
        fun getDefault(): UserEngagementPatterns {
            return UserEngagementPatterns(
                optimalHoursByContent = mapOf(
                    "engagement" to listOf(9, 12, 18, 20),
                    "music_suggestion" to listOf(8, 12, 17, 19),
                    "marketing" to listOf(10, 14, 19),
                    "trending" to listOf(12, 18, 20),
                    "personalized_suggestion" to listOf(8, 17, 19)
                ),
                dismissalRateByHour = (0..23).associateWith { 0.3 }, // 30% default dismissal rate
                engagementRateByContent = mapOf(
                    "engagement" to 0.4,
                    "music_suggestion" to 0.6,
                    "marketing" to 0.2,
                    "trending" to 0.5,
                    "personalized_suggestion" to 0.7
                )
            )
        }
    }
}

data class DeliveryWindow(
    val startHour: Int,
    val endHour: Int
)

// NotificationPriority enum is defined in NotificationPreferenceModels.kt

enum class NotificationInteraction {
    CLICKED,
    DISMISSED,
    IGNORED,
    ACTION_TAKEN
}

sealed class ScheduleResult {
    object DeliveredImmediately : ScheduleResult()
    data class ScheduledForLater(val deliveryTime: Long) : ScheduleResult()
    data class Blocked(val reason: String) : ScheduleResult()
}