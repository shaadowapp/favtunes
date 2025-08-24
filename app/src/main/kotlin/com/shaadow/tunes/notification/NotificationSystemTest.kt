package com.shaadow.tunes.notification

import android.content.Context
import android.util.Log

/**
 * Simple test utility for the notification system
 * Provides methods to test and verify notification functionality
 */
class NotificationSystemTest(private val context: Context) {
    
    private val notificationHelper = LocalNotificationHelper(context)
    private val throttleManager = NotificationThrottleManager(context)
    private val contentGenerator = NotificationContentGenerator()
    
    companion object {
        private const val TAG = "NotificationSystemTest"
    }
    
    /**
     * Test basic notification delivery
     */
    fun testBasicNotification(): TestResult {
        return try {
            val content = NotificationContent(
                title = "Test Notification",
                body = "This is a test notification to verify the system works",
                emoji = "🧪",
                actionText = "Test Action",
                contentType = "test"
            )
            
            notificationHelper.showEngagementNotification(content)
            
            Log.d(TAG, "✅ Basic notification test passed")
            TestResult.Success("Basic notification delivered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Basic notification test failed", e)
            TestResult.Failure("Basic notification failed: ${e.message}")
        }
    }
    
    /**
     * Test notification throttling
     */
    fun testNotificationThrottling(): TestResult {
        return try {
            val canSend = throttleManager.canSendLocalNotification()
            val timeUntilNext = throttleManager.getFormattedTimeUntilNext()
            val status = throttleManager.getThrottleStatus()
            
            Log.d(TAG, "Throttling Status:")
            Log.d(TAG, "  Can send now: $canSend")
            Log.d(TAG, "  Time until next: $timeUntilNext")
            Log.d(TAG, "  Status: ${status.canSendNow}")
            
            TestResult.Success("Throttling system working. Can send: $canSend, Next: $timeUntilNext")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Throttling test failed", e)
            TestResult.Failure("Throttling test failed: ${e.message}")
        }
    }
    
    /**
     * Test notification permissions
     */
    fun testNotificationPermissions(): TestResult {
        return try {
            val enabled = notificationHelper.areNotificationsEnabled()
            
            Log.d(TAG, "Notification permissions enabled: $enabled")
            
            if (enabled) {
                TestResult.Success("Notification permissions are enabled")
            } else {
                TestResult.Warning("Notification permissions are disabled by user")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Permission test failed", e)
            TestResult.Failure("Permission test failed: ${e.message}")
        }
    }
    
    /**
     * Test content generation
     */
    fun testContentGeneration(): TestResult {
        return try {
            val engagementContent = contentGenerator.generateEngagementContent(48)
            val musicContent = contentGenerator.generateMusicSuggestionContent(emptyList())
            val marketingContent = contentGenerator.generateMarketingContent()
            
            Log.d(TAG, "Generated content:")
            Log.d(TAG, "  Engagement: ${engagementContent.title}")
            Log.d(TAG, "  Music: ${musicContent.title}")
            Log.d(TAG, "  Marketing: ${marketingContent.title}")
            
            TestResult.Success("Content generation working properly")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Content generation test failed", e)
            TestResult.Failure("Content generation failed: ${e.message}")
        }
    }
    
    /**
     * Run all tests and return summary
     */
    fun runAllTests(): TestSummary {
        Log.d(TAG, "🧪 Starting notification system tests...")
        
        val results = mutableListOf<TestResult>()
        
        results.add(testNotificationPermissions())
        results.add(testNotificationThrottling())
        results.add(testContentGeneration())
        results.add(testBasicNotification())
        
        val summary = TestSummary(
            totalTests = results.size,
            passed = results.count { it is TestResult.Success },
            failed = results.count { it is TestResult.Failure },
            warnings = results.count { it is TestResult.Warning },
            results = results
        )
        
        Log.d(TAG, "🧪 Test Summary:")
        Log.d(TAG, "  Total: ${summary.totalTests}")
        Log.d(TAG, "  Passed: ${summary.passed}")
        Log.d(TAG, "  Failed: ${summary.failed}")
        Log.d(TAG, "  Warnings: ${summary.warnings}")
        
        return summary
    }
    
    /**
     * Reset throttling for testing
     */
    fun resetThrottling() {
        try {
            notificationHelper.resetThrottling()
            Log.d(TAG, "✅ Throttling reset for testing")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to reset throttling", e)
        }
    }
    
    /**
     * Get detailed system status
     */
    fun getSystemStatus(): String {
        return try {
            val throttleStatus = notificationHelper.getThrottleStatus()
            val permissionsEnabled = notificationHelper.areNotificationsEnabled()
            
            buildString {
                appendLine("=== NOTIFICATION SYSTEM STATUS ===")
                appendLine("Permissions enabled: $permissionsEnabled")
                appendLine("Throttle status:")
                appendLine(throttleStatus)
                appendLine("===================================")
            }
        } catch (e: Exception) {
            "Error getting system status: ${e.message}"
        }
    }
}

/**
 * Test result classes
 */
sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Failure(val message: String) : TestResult()
    data class Warning(val message: String) : TestResult()
}

data class TestSummary(
    val totalTests: Int,
    val passed: Int,
    val failed: Int,
    val warnings: Int,
    val results: List<TestResult>
) {
    val allPassed: Boolean get() = failed == 0
    val hasWarnings: Boolean get() = warnings > 0
}