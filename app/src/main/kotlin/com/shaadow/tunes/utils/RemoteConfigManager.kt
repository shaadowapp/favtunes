package com.shaadow.tunes.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import kotlin.text.Charsets
import com.shaadow.tunes.BuildConfig

/**
 * Remote Config Manager for quick bug fixes without Play Store updates
 * Fetches configuration from a remote JSON endpoint
 */
object RemoteConfigManager {
    
    private val REMOTE_CONFIG_URL = "https://raw.githubusercontent.com/${BuildConfig.REMOTE_CONFIG_OWNER}/${BuildConfig.REMOTE_CONFIG_REPO}/main/advanced-tunes-config.json"
    private val GITHUB_TOKEN = BuildConfig.GITHUB_TOKEN
    private const val PREFS_NAME = "remote_config"
    private const val LAST_FETCH_KEY = "last_fetch_time"
    private const val CONFIG_DATA_KEY = "config_data"
    private const val CACHE_DURATION = 30 * 60 * 1000L // 30 minutes
    
    @Serializable
    data class RemoteConfig(
        val videoIdMismatchRetryEnabled: Boolean = true,
        val videoIdMismatchMaxRetries: Int = 3,
        val videoIdMismatchRetryDelayMs: Long = 1000L,
        val fallbackToAlternativePlayer: Boolean = true,
        val skipFailedVideos: Boolean = false,
        val enableVideoIdValidation: Boolean = true,
        val alternativePlayerEndpoints: List<String> = emptyList(),
        val emergencyDisablePlayback: Boolean = false,
        val debugLoggingEnabled: Boolean = false
    )
    
    private var cachedConfig: RemoteConfig? = null
    private lateinit var prefs: SharedPreferences
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadCachedConfig()
    }
    
    private fun loadCachedConfig() {
        val configJson = prefs.getString(CONFIG_DATA_KEY, null)
        if (configJson != null) {
            try {
                cachedConfig = Json.decodeFromString<RemoteConfig>(configJson)
            } catch (e: Exception) {
                // Use default config if parsing fails
                cachedConfig = RemoteConfig()
            }
        } else {
            cachedConfig = RemoteConfig()
        }
    }
    
    suspend fun fetchConfig(): RemoteConfig = withContext(Dispatchers.IO) {
        val lastFetch = prefs.getLong(LAST_FETCH_KEY, 0)
        val now = System.currentTimeMillis()
        
        // Return cached config if still fresh
        if (now - lastFetch < CACHE_DURATION && cachedConfig != null) {
            return@withContext cachedConfig!!
        }
        
        try {
            // Use GitHub API for better security and error handling
            val apiUrl = "https://api.github.com/repos/${BuildConfig.REMOTE_CONFIG_OWNER}/${BuildConfig.REMOTE_CONFIG_REPO}/contents/advanced-tunes-config.json"
            val connection = URL(apiUrl).openConnection()
            connection.setRequestProperty("Authorization", "token $GITHUB_TOKEN")
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            
            val apiResponse = connection.getInputStream().bufferedReader().use { it.readText() }
            val contentJson = Json.parseToJsonElement(apiResponse).jsonObject
            val content = contentJson["content"]?.jsonPrimitive?.content ?: throw Exception("No content found")
            
            // Decode base64 content
            val decodedContent = java.util.Base64.getDecoder().decode(content.replace("\n", "")).toString(Charsets.UTF_8)
            val newConfig = Json.decodeFromString<RemoteConfig>(decodedContent)
            
            // Cache the new config
            prefs.edit()
                .putString(CONFIG_DATA_KEY, Json.encodeToString(RemoteConfig.serializer(), newConfig))
                .putLong(LAST_FETCH_KEY, now)
                .apply()
            
            cachedConfig = newConfig
            newConfig
        } catch (e: Exception) {
            // Return cached config or default if fetch fails
            cachedConfig ?: RemoteConfig()
        }
    }
    
    fun getConfig(): RemoteConfig {
        return cachedConfig ?: RemoteConfig()
    }
    
    // Quick access methods for common config values
    fun shouldRetryVideoIdMismatch(): Boolean = getConfig().videoIdMismatchRetryEnabled
    fun getMaxRetries(): Int = getConfig().videoIdMismatchMaxRetries
    fun getRetryDelay(): Long = getConfig().videoIdMismatchRetryDelayMs
    fun shouldFallbackToAlternative(): Boolean = getConfig().fallbackToAlternativePlayer
    fun shouldSkipFailedVideos(): Boolean = getConfig().skipFailedVideos
    fun isEmergencyDisabled(): Boolean = getConfig().emergencyDisablePlayback
    fun isDebugEnabled(): Boolean = getConfig().debugLoggingEnabled
}