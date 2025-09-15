package com.shaadow.tunes.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

/**
 * Advanced Remote Config for complete PlayerService control
 * Allows remote control of all error handling and playback behavior
 */
object AdvancedRemoteConfig {
    
    private const val DEFAULT_REMOTE_CONFIG_URL = "https://raw.githubusercontent.com/shaadowapp/shaadowapp/main/playback-fix.json"
    private const val PREFS_NAME = "advanced_remote_config"
    private const val LAST_FETCH_KEY = "last_fetch_time"
    private const val CONFIG_DATA_KEY = "config_data"
    private const val CACHE_DURATION = 15 * 60 * 1000L // 15 minutes for faster updates
    
    @Serializable
    data class ErrorHandlingConfig(
        val videoIdMismatchEnabled: Boolean = true,
        val videoIdMismatchMaxRetries: Int = 3, // Normal: Allow retries
        val videoIdMismatchRetryDelayMs: Long = 2000L, // Normal: Reasonable delay
        val videoIdMismatchSkipOnFailure: Boolean = false, // Normal: Don't skip immediately
        val videoIdMismatchUseAlternativeApi: Boolean = false,
        
        val playableFormatNotFoundEnabled: Boolean = true,
        val playableFormatMaxRetries: Int = 2, // Normal: Allow retries
        val playableFormatRetryDelayMs: Long = 1500L, // Normal: Reasonable delay
        val playableFormatSkipOnFailure: Boolean = true, // Normal: Skip unplayable formats
        
        val unplayableExceptionEnabled: Boolean = true,
        val unplayableMaxRetries: Int = 1, // Normal: Allow one retry
        val unplayableRetryDelayMs: Long = 1000L, // Normal: Reasonable delay
        val unplayableSkipOnFailure: Boolean = true, // Normal: Skip unplayable songs
        
        val loginRequiredEnabled: Boolean = true,
        val loginRequiredMaxRetries: Int = 2, // Normal: Allow retries
        val loginRequiredRetryDelayMs: Long = 3000L, // Normal: Longer delay for login
        val loginRequiredSkipOnFailure: Boolean = false, // Normal: Don't skip login required
        
        val genericErrorEnabled: Boolean = true,
        val genericErrorMaxRetries: Int = 2, // Normal: Allow retries
        val genericErrorRetryDelayMs: Long = 2000L, // Normal: Reasonable delay
        val genericErrorSkipOnFailure: Boolean = false // Normal: Don't skip on generic errors
    )
    
    @Serializable
    data class PlayerServiceConfig(
        val cacheEnabled: Boolean = true, // Normal: Enable cache
        val cacheMaxSizeMB: Long = 512L, // Normal: Reasonable cache size
        val connectTimeoutMs: Int = 16000, // Normal: Good timeout
        val readTimeoutMs: Int = 8000, // Normal: Good timeout
        val chunkSizeKB: Long = 512L, // Normal: Good chunk size
        val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0", // Normal: Standard user agent
        val maxConcurrentRequests: Int = 3, // Normal: Allow concurrent requests
        val enablePreloading: Boolean = true, // Normal: Enable preloading
        val preloadNextSongs: Int = 2 // Normal: Preload next songs
    )
    
    @Serializable
    data class ApiEndpointsConfig(
        val primaryYouTubeApi: String = "https://www.youtube.com",
        val alternativeEndpoints: List<String> = emptyList(),
        val fallbackTimeout: Long = 5000L,
        val useAlternativeFirst: Boolean = false,
        val rotateEndpoints: Boolean = false
    )
    
    @Serializable
    data class DebuggingConfig(
        val enableDetailedLogging: Boolean = false,
        val logNetworkRequests: Boolean = false,
        val logErrorDetails: Boolean = false,
        val logPerformanceMetrics: Boolean = false,
        val enableCrashReporting: Boolean = true,
        val logLevel: String = "ERROR" // ERROR, WARN, INFO, DEBUG, VERBOSE
    )
    
    @Serializable
    data class EmergencyConfig(
        val disablePlayback: Boolean = false, // Normal: Allow playback by default
        val disableSpecificFeatures: List<String> = listOf("preloading", "caching", "concurrent_requests"), // TEST: Disable features
        val forceSkipProblematicSongs: Boolean = true, // TEST: Force skip songs
        val emergencyMessage: String = "",
        val redirectToAlternativePlayer: Boolean = false
    )
    
    @Serializable
    data class PerformanceConfig(
        val enableAudioOffload: Boolean = true,
        val bufferSizeMs: Int = 50000,
        val minBufferMs: Int = 2500,
        val maxBufferMs: Int = 50000,
        val enableSilenceSkipping: Boolean = false,
        val silenceThresholdDb: Float = -50f,
        val enableVolumeNormalization: Boolean = false
    )
    
    @Serializable
    data class AdvancedRemoteConfigData(
        val version: String = "1.0",
        val lastUpdated: String = "",
        val errorHandling: ErrorHandlingConfig = ErrorHandlingConfig(),
        val playerService: PlayerServiceConfig = PlayerServiceConfig(),
        val apiEndpoints: ApiEndpointsConfig = ApiEndpointsConfig(),
        val debugging: DebuggingConfig = DebuggingConfig(),
        val emergency: EmergencyConfig = EmergencyConfig(),
        val performance: PerformanceConfig = PerformanceConfig(),
        val customErrorMessages: Map<String, String> = emptyMap(),
        val featureFlags: Map<String, Boolean> = emptyMap()
    )
    
    private var cachedConfig: AdvancedRemoteConfigData? = null
    private lateinit var prefs: SharedPreferences
    private const val TAG = "AdvancedRemoteConfig"
    
    private lateinit var context: Context
    
    fun initialize(context: Context) {
        this.context = context.applicationContext
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadCachedConfig()
    }
    
    private fun getRemoteConfigUrl(): String {
        val credentialsManager = SecureCredentialsManager.getInstance(context)
        val owner = credentialsManager.getRemoteConfigOwner() ?: "shaadowapp"
        val repo = credentialsManager.getRemoteConfigRepo() ?: "shaadowapp"
        return "https://raw.githubusercontent.com/$owner/$repo/main/playback-fix.json"
    }
    
    private fun loadCachedConfig() {
        val configJson = prefs.getString(CONFIG_DATA_KEY, null)
        if (configJson != null) {
            try {
                cachedConfig = Json.decodeFromString<AdvancedRemoteConfigData>(configJson)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse cached config", e)
                cachedConfig = AdvancedRemoteConfigData()
            }
        } else {
            cachedConfig = AdvancedRemoteConfigData()
        }
    }
    
    suspend fun fetchConfig(): AdvancedRemoteConfigData = withContext(Dispatchers.IO) {
        val lastFetch = prefs.getLong(LAST_FETCH_KEY, 0)
        val now = System.currentTimeMillis()
        
        // Return cached config if still fresh
        if (now - lastFetch < CACHE_DURATION && cachedConfig != null) {
            return@withContext cachedConfig!!
        }
        
        try {
            val configUrl = getRemoteConfigUrl()
            val response = URL(configUrl).readText()
            val newConfig = Json.decodeFromString<AdvancedRemoteConfigData>(response)
            
            // Cache the new config
            prefs.edit()
                .putString(CONFIG_DATA_KEY, Json.encodeToString(AdvancedRemoteConfigData.serializer(), newConfig))
                .putLong(LAST_FETCH_KEY, now)
                .apply()
            
            cachedConfig = newConfig
            
            if (newConfig.debugging.enableDetailedLogging) {
                Log.i(TAG, "Remote config updated: version ${newConfig.version}")
            }
            
            newConfig
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch remote config", e)
            // Return cached config or default if fetch fails
            cachedConfig ?: AdvancedRemoteConfigData()
        }
    }
    
    fun getConfig(): AdvancedRemoteConfigData {
        return cachedConfig ?: AdvancedRemoteConfigData()
    }
    
    // Quick access methods for error handling
    fun shouldRetryError(errorType: String): Boolean {
        val config = getConfig().errorHandling
        return when (errorType) {
            "VideoIdMismatch" -> config.videoIdMismatchEnabled
            "PlayableFormatNotFound" -> config.playableFormatNotFoundEnabled
            "Unplayable" -> config.unplayableExceptionEnabled
            "LoginRequired" -> config.loginRequiredEnabled
            else -> config.genericErrorEnabled
        }
    }
    
    fun getMaxRetries(errorType: String): Int {
        val config = getConfig().errorHandling
        return when (errorType) {
            "VideoIdMismatch" -> config.videoIdMismatchMaxRetries
            "PlayableFormatNotFound" -> config.playableFormatMaxRetries
            "Unplayable" -> config.unplayableMaxRetries
            "LoginRequired" -> config.loginRequiredMaxRetries
            else -> config.genericErrorMaxRetries
        }
    }
    
    fun getRetryDelay(errorType: String): Long {
        val config = getConfig().errorHandling
        return when (errorType) {
            "VideoIdMismatch" -> config.videoIdMismatchRetryDelayMs
            "PlayableFormatNotFound" -> config.playableFormatRetryDelayMs
            "Unplayable" -> config.unplayableRetryDelayMs
            "LoginRequired" -> config.loginRequiredRetryDelayMs
            else -> config.genericErrorRetryDelayMs
        }
    }
    
    fun shouldSkipOnFailure(errorType: String): Boolean {
        val config = getConfig().errorHandling
        return when (errorType) {
            "VideoIdMismatch" -> config.videoIdMismatchSkipOnFailure
            "PlayableFormatNotFound" -> config.playableFormatSkipOnFailure
            "Unplayable" -> config.unplayableSkipOnFailure
            "LoginRequired" -> config.loginRequiredSkipOnFailure
            else -> config.genericErrorSkipOnFailure
        }
    }
    
    // Emergency controls
    fun isPlaybackDisabled(): Boolean = getConfig().emergency.disablePlayback
    fun getEmergencyMessage(): String = getConfig().emergency.emergencyMessage
    fun shouldUseAlternativePlayer(): Boolean = getConfig().emergency.redirectToAlternativePlayer
    
    // Debugging controls
    fun isLoggingEnabled(): Boolean = getConfig().debugging.enableDetailedLogging
    fun shouldLogNetworkRequests(): Boolean = getConfig().debugging.logNetworkRequests
    fun shouldLogErrors(): Boolean = getConfig().debugging.logErrorDetails
    fun getLogLevel(): String = getConfig().debugging.logLevel
    
    // Performance controls
    fun getConnectTimeout(): Int = getConfig().playerService.connectTimeoutMs
    fun getReadTimeout(): Int = getConfig().playerService.readTimeoutMs
    fun getChunkSize(): Long = getConfig().playerService.chunkSizeKB * 1024L
    fun getUserAgent(): String = getConfig().playerService.userAgent
    
    // API endpoints
    fun getAlternativeEndpoints(): List<String> = getConfig().apiEndpoints.alternativeEndpoints
    fun shouldUseAlternativeFirst(): Boolean = getConfig().apiEndpoints.useAlternativeFirst
    fun getFallbackTimeout(): Long = getConfig().apiEndpoints.fallbackTimeout
    
    // Feature flags
    fun isFeatureEnabled(featureName: String): Boolean {
        return getConfig().featureFlags[featureName] ?: false
    }
    
    // Custom error messages
    fun getCustomErrorMessage(errorType: String): String? {
        return getConfig().customErrorMessages[errorType]
    }
}