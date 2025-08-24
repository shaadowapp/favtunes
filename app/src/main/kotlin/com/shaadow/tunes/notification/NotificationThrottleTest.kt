package com.shaadow.tunes.notification

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay

/**
 * Test utility to verify notification throttling is working correctly
 * This helps ensure we don't send multiple local notifications simultaneously
 */
class NotificationThrottleTest(private val context: Context) {
    
    private val notificationManager = FavTunesNotificationManager(context)
    private val throttleMonitor = NotificationThrottleMonitor(context)
    
    companion object {
        private const val TAG = "ThrottleTest"
    }
    
    /**
     * Test rapid notification attempts to verify throttling
     */
    suspend fun testRapidNotificationAttempts() {
        Log.d(TAG, "Starting rapid notification test...")
        
        // Clear history for clean test
        throttleMonitor.clearHistory()
        
        // Attempt to send multiple notifications rapidly
        val notifications = listOf(
            "engagement" to "Come back and discover new music! 🎵",
            "music_suggestion" to "Check out these trending songs! 🔥",
            "marketing" to "Special music features await! ✨"
        )
        
        notifications.forEachIndexed { index, (type, message) ->
            Log.d(TAG, "Attempting notification $index: $type")
            
            val throttleStatus = notificationManager.getNotificationThrottleStatus()
            Log.d(TAG, "Throttle status: canDeliver=${throttleStatus.canDeliverNow}, timeUntilNext=${throttleStatus.getFormattedTimeUntilNext()}")
            
            when (type) {
                "engagement" -> {
                    notificationManager.sendEngagementNotification(25) // 25 hours since last open
                }
                "music_suggestion" -> {
                    // Create dummy songs for testing
                    val dummySongs = emptyList<com.shaadow.tunes.models.Song>()
                    notificationManager.sendMusicSuggestion(dummySongs)
                }
                "marketing" -> {
                    notificationManager.scheduleMarketingNotification()
                }
            }
            
            // Record the attempt for monitoring
            throttleMonitor.recordNotificationDelivery("test_$type")
            
            // Small delay between attempts
            delay(1000)
        }
        
        // Log final status
        Log.d(TAG, "Test completed. Final status:")
        throttleMonitor.logThrottleStatus()
        Log.d(TAG, throttleMonitor.getThrottleSummary())
        
        // Check for violations
        val violations = throttleMonitor.checkForViolations()
        if (violations.isNotEmpty()) {
            Log.w(TAG, "⚠️ THROTTLING VIOLATIONS DETECTED:")
            violations.forEach { violation ->
                Log.w(TAG, "Gap of ${String.format("%.1f", violation.actualGapHours)}h between notifications (required: ${violation.requiredGapHours}h)")
            }
        } else {
            Log.i(TAG, "✅ No throttling violations - system working correctly!")
        }
    }
    
    /**
     * Simulate the scenario that caused the original issue
     */
    suspend fun simulateOriginalIssue() {
        Log.d(TAG, "Simulating original issue: 3 notifications at same time")
        
        throttleMonitor.clearHistory()
        
        // This should trigger the throttling system
        val startTime = System.currentTimeMillis()
        
        // Simulate what happened before - multiple notifications triggered simultaneously
        Log.d(TAG, "Triggering engagement notification...")
        notificationManager.sendEngagementNotification(30)
        
        Log.d(TAG, "Triggering music suggestion...")
        notificationManager.sendMusicSuggestion(emptyList())
        
        Log.d(TAG, "Triggering marketing notification...")
        notificationManager.scheduleMarketingNotification()
        
        val endTime = System.currentTimeMillis()
        Log.d(TAG, "All notifications triggered in ${endTime - startTime}ms")
        
        // Check what actually got delivered
        delay(2000) // Wait for processing
        
        Log.d(TAG, "Results after simulation:")
        throttleMonitor.logThrottleStatus()
        
        val violations = throttleMonitor.checkForViolations()
        if (violations.isEmpty()) {
            Log.i(TAG, "✅ SUCCESS: Throttling prevented simultaneous notifications!")
        } else {
            Log.e(TAG, "❌ FAILED: Multiple notifications still delivered simultaneously")
        }
    }
    
    /**
     * Test the preferred 3-hour gap scenario
     */
    suspend fun testPreferredGapScenario() {
        Log.d(TAG, "Testing preferred 3-hour gap scenario...")
        
        throttleMonitor.clearHistory()
        
        // Send first notification
        Log.d(TAG, "Sending first notification...")
        notificationManager.sendEngagementNotification(25)
        throttleMonitor.recordNotificationDelivery("local")
        
        // Wait 2.5 hours (less than preferred 3 hours)
        Log.d(TAG, "Simulating 2.5 hour wait...")
        val twoAndHalfHours = 2.5 * 60 * 60 * 1000L
        
        // Simulate time passage by manually updating the last notification time
        val preferences = context.getSharedPreferences("notification_scheduler", Context.MODE_PRIVATE)
        val simulatedLastTime = System.currentTimeMillis() - twoAndHalfHours.toLong()
        preferences.edit().putLong("last_local_notification_time", simulatedLastTime).apply()
        
        // Try to send second notification
        Log.d(TAG, "Attempting second notification after 2.5 hours...")
        val throttleStatus = notificationManager.getNotificationThrottleStatus()
        
        if (throttleStatus.canDeliverNow) {
            Log.i(TAG, "✅ Notification allowed after 2.5 hours (meets 2-hour minimum)")
        } else {
            Log.i(TAG, "⏳ Notification delayed - waiting for preferred 3-hour gap")
            Log.d(TAG, "Time until next slot: ${throttleStatus.getFormattedTimeUntilNext()}")
        }
    }
}