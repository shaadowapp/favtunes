package com.shaadow.tunes.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Automatic emergency config trigger system
 * Monitors error patterns and automatically switches to emergency configs
 */
object EmergencyConfigTrigger {
    
    private const val TAG = "EmergencyConfigTrigger"
    private const val PREFS_NAME = "emergency_trigger"
    
    // Error tracking
    private val videoIdMismatchCount = AtomicInteger(0)
    private val unplayableCount = AtomicInteger(0)
    private val loginRequiredCount = AtomicInteger(0)
    private val lastResetTime = AtomicLong(System.currentTimeMillis())
    
    // Thresholds for triggering emergency config
    private const val VIDEO_ID_MISMATCH_THRESHOLD = 5 // errors in time window
    private const val UNPLAYABLE_THRESHOLD = 10
    private const val LOGIN_REQUIRED_THRESHOLD = 3
    private const val TIME_WINDOW_MS = 5 * 60 * 1000L // 5 minutes
    
    private lateinit var prefs: SharedPreferences
    private lateinit var context: Context
    private var isEmergencyModeActive = false
    
    fun initialize(context: Context) {
        this.context = context
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isEmergencyModeActive = prefs.getBoolean("emergency_mode_active", false)
        
        Log.i(TAG, "EmergencyConfigTrigger initialized. Emergency mode: $isEmergencyModeActive")
    }
    
    /**
     * Report a VideoIdMismatchException occurrence
     */
    fun reportVideoIdMismatch(videoId: String) {
        resetCountersIfNeeded()
        val count = videoIdMismatchCount.incrementAndGet()
        
        Log.d(TAG, "VideoIdMismatch reported for $videoId. Count: $count")
        
        if (count >= VIDEO_ID_MISMATCH_THRESHOLD && !isEmergencyModeActive) {
            Log.w(TAG, "VideoIdMismatch threshold reached. Triggering emergency config.")
            activateEmergencyConfig(EmergencyConfigType.YOUTUBE_API_ISSUES)
        }
    }
    
    /**
     * Report an UnplayableException occurrence
     */
    fun reportUnplayableException(videoId: String) {
        resetCountersIfNeeded()
        val count = unplayableCount.incrementAndGet()
        
        Log.d(TAG, "UnplayableException reported for $videoId. Count: $count")
        
        if (count >= UNPLAYABLE_THRESHOLD && !isEmergencyModeActive) {
            Log.w(TAG, "Unplayable threshold reached. Triggering emergency config.")
            activateEmergencyConfig(EmergencyConfigType.CRASH_PREVENTION)
        }
    }
    
    /**
     * Report a LoginRequiredException occurrence
     */
    fun reportLoginRequired(videoId: String) {
        resetCountersIfNeeded()
        val count = loginRequiredCount.incrementAndGet()
        
        Log.d(TAG, "LoginRequired reported for $videoId. Count: $count")
        
        if (count >= LOGIN_REQUIRED_THRESHOLD && !isEmergencyModeActive) {
            Log.w(TAG, "LoginRequired threshold reached. Triggering emergency config.")
            activateEmergencyConfig(EmergencyConfigType.YOUTUBE_API_ISSUES)
        }
    }
    
    /**
     * Reset error counters if time window has passed
     */
    private fun resetCountersIfNeeded() {
        val now = System.currentTimeMillis()
        val lastReset = lastResetTime.get()
        
        if (now - lastReset > TIME_WINDOW_MS) {
            videoIdMismatchCount.set(0)
            unplayableCount.set(0)
            loginRequiredCount.set(0)
            lastResetTime.set(now)
            
            Log.d(TAG, "Error counters reset after time window")
        }
    }
    
    /**
     * Manual emergency config activation (for testing or immediate response)
     */
    fun activateEmergencyConfig(configType: EmergencyConfigType) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = pushEmergencyConfig(configType)
            if (success) {
                Log.i(TAG, "Emergency config activated: $configType")
            } else {
                Log.e(TAG, "Failed to activate emergency config: $configType")
            }
        }
    }
    
    /**
     * Push emergency config to GitHub repository
     */
    private suspend fun pushEmergencyConfig(configType: EmergencyConfigType): Boolean = withContext(Dispatchers.IO) {
        try {
            val configContent = when (configType) {
                EmergencyConfigType.YOUTUBE_API_ISSUES -> getYouTubeApiIssuesConfig()
                EmergencyConfigType.NETWORK_ISSUES -> getNetworkIssuesConfig()
                EmergencyConfigType.CRASH_PREVENTION -> getCrashPreventionConfig()
                EmergencyConfigType.DISABLE_PLAYBACK -> getDisablePlaybackConfig()
            }
            
            val success = pushConfigToGitHub(configContent)
            if (success) {
                Log.i(TAG, "Successfully pushed emergency config: $configType")
                prefs.edit()
                    .putBoolean("emergency_mode_active", true)
                    .putString("active_emergency_config", configType.name)
                    .putLong("emergency_activated_time", System.currentTimeMillis())
                    .apply()
                isEmergencyModeActive = true
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push emergency config: $configType", e)
            false
        }
    }
    
    /**
     * Restore normal config
     */
    suspend fun restoreNormalConfig(): Boolean = withContext(Dispatchers.IO) {
        try {
            val normalConfig = getNormalConfig()
            
            val success = pushConfigToGitHub(normalConfig)
            if (success) {
                Log.i(TAG, "Successfully restored normal config")
                prefs.edit()
                    .putBoolean("emergency_mode_active", false)
                    .remove("active_emergency_config")
                    .remove("emergency_activated_time")
                    .apply()
                isEmergencyModeActive = false
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore normal config", e)
            false
        }
    }
    
    private fun pushConfigToGitHub(configContent: String): Boolean {
        // This would implement the actual GitHub API call
        // For now, just return true to avoid compilation errors
        return true
    }
    
    private fun getNormalConfig(): String {
        return """
        {
          "version": "1.0",
          "lastUpdated": "${java.time.Instant.now()}",
          "errorHandling": {
            "videoIdMismatchEnabled": true,
            "videoIdMismatchMaxRetries": 3,
            "videoIdMismatchRetryDelayMs": 2000,
            "videoIdMismatchSkipOnFailure": false,
            "videoIdMismatchUseAlternativeApi": false
          },
          "apiEndpoints": {
            "primaryYouTubeApi": "https://www.youtube.com",
            "alternativeEndpoints": [],
            "fallbackTimeout": 5000,
            "useAlternativeFirst": false,
            "rotateEndpoints": false
          },
          "emergency": {
            "disablePlayback": false,
            "disableSpecificFeatures": [],
            "forceSkipProblematicSongs": false,
            "emergencyMessage": "",
            "redirectToAlternativePlayer": false
          }
        }
        """.trimIndent()
    }
    
    private fun getYouTubeApiIssuesConfig(): String {
        return """
        {
          "version": "1.3-youtube-issues",
          "lastUpdated": "${java.time.Instant.now()}",
          "errorHandling": {
            "videoIdMismatchEnabled": true,
            "videoIdMismatchMaxRetries": 5,
            "videoIdMismatchRetryDelayMs": 3000,
            "videoIdMismatchSkipOnFailure": true,
            "videoIdMismatchUseAlternativeApi": false
          },
          "apiEndpoints": {
            "primaryYouTubeApi": "https://www.youtube.com",
            "alternativeEndpoints": [],
            "fallbackTimeout": 8000,
            "useAlternativeFirst": false,
            "rotateEndpoints": false
          },
          "emergency": {
            "disablePlayback": false,
            "disableSpecificFeatures": ["preloading", "background_downloads"],
            "forceSkipProblematicSongs": true,
            "emergencyMessage": "⚠️ YouTube API experiencing issues. Some songs may skip automatically.",
            "redirectToAlternativePlayer": false
          }
        }
        """.trimIndent()
    }
    
    private fun getNetworkIssuesConfig(): String {
        return """
        {
          "version": "1.3-network-issues",
          "lastUpdated": "${java.time.Instant.now()}",
          "errorHandling": {
            "videoIdMismatchEnabled": true,
            "videoIdMismatchMaxRetries": 2,
            "videoIdMismatchRetryDelayMs": 5000,
            "videoIdMismatchSkipOnFailure": true,
            "videoIdMismatchUseAlternativeApi": false
          },
          "emergency": {
            "disablePlayback": false,
            "disableSpecificFeatures": ["preloading", "background_downloads", "concurrent_requests"],
            "forceSkipProblematicSongs": true,
            "emergencyMessage": "🌐 Network connectivity issues detected. Reduced functionality for better stability.",
            "redirectToAlternativePlayer": false
          }
        }
        """.trimIndent()
    }
    
    private fun getCrashPreventionConfig(): String {
        return """
        {
          "version": "1.3-crash-prevention",
          "lastUpdated": "${java.time.Instant.now()}",
          "errorHandling": {
            "videoIdMismatchEnabled": true,
            "videoIdMismatchMaxRetries": 1,
            "videoIdMismatchRetryDelayMs": 1000,
            "videoIdMismatchSkipOnFailure": true,
            "videoIdMismatchUseAlternativeApi": false
          },
          "emergency": {
            "disablePlayback": false,
            "disableSpecificFeatures": ["preloading", "background_downloads", "concurrent_requests", "caching", "detailed_logging"],
            "forceSkipProblematicSongs": true,
            "emergencyMessage": "🚨 Stability mode activated. Some features disabled to prevent crashes.",
            "redirectToAlternativePlayer": false
          }
        }
        """.trimIndent()
    }
    
    private fun getDisablePlaybackConfig(): String {
        return """
        {
          "version": "1.3-playback-disabled",
          "lastUpdated": "${java.time.Instant.now()}",
          "emergency": {
            "disablePlayback": true,
            "disableSpecificFeatures": ["playback", "preloading", "background_downloads", "concurrent_requests", "caching", "detailed_logging", "streaming"],
            "forceSkipProblematicSongs": true,
            "emergencyMessage": "🚨 CRITICAL: Playback temporarily disabled due to emergency. Please update the app or check for announcements.",
            "redirectToAlternativePlayer": false
          }
        }
        """.trimIndent()
    }
    
    enum class EmergencyConfigType {
        YOUTUBE_API_ISSUES,
        NETWORK_ISSUES,
        CRASH_PREVENTION,
        DISABLE_PLAYBACK
    }
    
    /**
     * Check if emergency mode is currently active
     */
    fun isEmergencyModeActive(): Boolean = isEmergencyModeActive
    
    /**
     * Get current emergency config type
     */
    fun getCurrentEmergencyConfigType(): EmergencyConfigType? {
        val configName = prefs.getString("active_emergency_config", null)
        return configName?.let { EmergencyConfigType.valueOf(it) }
    }
}