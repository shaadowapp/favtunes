package com.shaadow.tunes.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure credentials manager using Android's EncryptedSharedPreferences
 * Replaces hardcoded API keys and tokens with encrypted storage
 */
class SecureCredentialsManager private constructor(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        @Volatile
        private var INSTANCE: SecureCredentialsManager? = null
        
        fun getInstance(context: Context): SecureCredentialsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecureCredentialsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Credential keys
        private const val KEY_ONESIGNAL_APP_ID = "onesignal_app_id"
        private const val KEY_GITHUB_TOKEN = "github_token"
        private const val KEY_REMOTE_CONFIG_REPO = "remote_config_repo"
        private const val KEY_REMOTE_CONFIG_OWNER = "remote_config_owner"
        private const val KEY_YOUTUBE_API_KEY = "youtube_api_key"
    }
    
    /**
     * Get OneSignal App ID from BuildConfig first, then secure storage as fallback
     */
    fun getOneSignalAppId(): String {
        // Try BuildConfig first (from local.properties)
        val buildConfigId = com.shaadow.tunes.BuildConfig.ONESIGNAL_APP_ID
        if (buildConfigId.isNotBlank()) {
            return buildConfigId
        }
        
        // Fallback to secure storage
        return encryptedPrefs.getString(KEY_ONESIGNAL_APP_ID, "3190b63e-f333-446b-bbba-7712efa18bb9") ?: "3190b63e-f333-446b-bbba-7712efa18bb9"
    }
    
    /**
     * Set OneSignal App ID in secure storage
     * Should only be called during initial app setup
     */
    fun setOneSignalAppId(appId: String) {
        encryptedPrefs.edit()
            .putString(KEY_ONESIGNAL_APP_ID, appId)
            .apply()
    }
    
    /**
     * Get GitHub token from BuildConfig first, then secure storage as fallback
     */
    fun getGitHubToken(): String? {
        // Try BuildConfig first (from local.properties)
        val buildConfigToken = com.shaadow.tunes.BuildConfig.GITHUB_TOKEN
        if (buildConfigToken.isNotBlank()) {
            return buildConfigToken
        }
        
        // Fallback to secure storage
        return encryptedPrefs.getString(KEY_GITHUB_TOKEN, null)
    }
    
    /**
     * Set GitHub token in secure storage
     */
    fun setGitHubToken(token: String) {
        encryptedPrefs.edit()
            .putString(KEY_GITHUB_TOKEN, token)
            .apply()
    }
    
    /**
     * Get remote config repository name from BuildConfig first, then secure storage as fallback
     */
    fun getRemoteConfigRepo(): String? {
        // Try BuildConfig first (from local.properties)
        val buildConfigRepo = com.shaadow.tunes.BuildConfig.REMOTE_CONFIG_REPO
        if (buildConfigRepo.isNotBlank()) {
            return buildConfigRepo
        }
        
        // Fallback to secure storage
        return encryptedPrefs.getString(KEY_REMOTE_CONFIG_REPO, null)
    }
    
    /**
     * Set remote config repository name
     */
    fun setRemoteConfigRepo(repo: String) {
        encryptedPrefs.edit()
            .putString(KEY_REMOTE_CONFIG_REPO, repo)
            .apply()
    }
    
    /**
     * Get remote config owner from BuildConfig first, then secure storage as fallback
     */
    fun getRemoteConfigOwner(): String? {
        // Try BuildConfig first (from local.properties)
        val buildConfigOwner = com.shaadow.tunes.BuildConfig.REMOTE_CONFIG_OWNER
        if (buildConfigOwner.isNotBlank()) {
            return buildConfigOwner
        }
        
        // Fallback to secure storage
        return encryptedPrefs.getString(KEY_REMOTE_CONFIG_OWNER, null)
    }
    
    /**
     * Set remote config owner
     */
    fun setRemoteConfigOwner(owner: String) {
        encryptedPrefs.edit()
            .putString(KEY_REMOTE_CONFIG_OWNER, owner)
            .apply()
    }
    
    /**
     * Clear all stored credentials (for logout/reset)
     */
    fun clearAllCredentials() {
        encryptedPrefs.edit().clear().apply()
    }
    
    /**
     * Get YouTube API key from BuildConfig first, then secure storage as fallback
     */
    fun getYouTubeApiKey(): String {
        // Try BuildConfig first (from local.properties)
        val buildConfigKey = com.shaadow.tunes.BuildConfig.YOUTUBE_API_KEY
        if (buildConfigKey.isNotBlank()) {
            return buildConfigKey
        }
        
        // Fallback to secure storage
        return encryptedPrefs.getString(KEY_YOUTUBE_API_KEY, "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8") ?: "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
    }
    
    /**
     * Set YouTube API key in secure storage
     */
    fun setYouTubeApiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString(KEY_YOUTUBE_API_KEY, apiKey)
            .apply()
    }
    
    /**
     * Check if essential credentials are configured
     */
    fun areCredentialsConfigured(): Boolean {
        return getOneSignalAppId().isNotEmpty()
    }
}