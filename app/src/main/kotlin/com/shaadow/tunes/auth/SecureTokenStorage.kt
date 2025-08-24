package com.shaadow.tunes.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Secure storage for device tokens using encrypted SharedPreferences
 */
class SecureTokenStorage(
    private val context: Context,
    private val encryption: TokenEncryption,
    private val deviceInfoCollector: DeviceInfoCollector
) {
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(AuthConstants.PREF_NAME, Context.MODE_PRIVATE)
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Store device token securely with encryption
     * @param token Token to store
     * @return True if successfully stored
     */
    suspend fun storeToken(token: DeviceToken): Boolean = withContext(Dispatchers.IO) {
        try {
            val tokenJson = json.encodeToString(token)
            val password = generateStoragePassword(token.deviceInfo.deviceFingerprint)
            val encryptedToken = encryption.encrypt(tokenJson, password)
            
            prefs.edit()
                .putString(AuthConstants.KEY_DEVICE_TOKEN, encryptedToken)
                .putString(AuthConstants.KEY_DEVICE_ID, token.deviceId)
                .putString(AuthConstants.KEY_USER_ID, "") // Will be set during authentication
                .putLong(AuthConstants.KEY_TOKEN_CREATED_AT, token.createdAt)
                .putLong(AuthConstants.KEY_TOKEN_EXPIRES_AT, token.expiresAt)
                .apply()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retrieve stored device token with decryption
     * @param appVersion Current app version for device validation
     * @return Stored token or null if not found/invalid
     */
    suspend fun getStoredToken(appVersion: String): DeviceToken? = withContext(Dispatchers.IO) {
        try {
            val encryptedToken = prefs.getString(AuthConstants.KEY_DEVICE_TOKEN, null) ?: return@withContext null
            
            // Get current device info for password generation
            val currentDeviceInfo = deviceInfoCollector.collectDeviceInfo(appVersion)
            val password = generateStoragePassword(currentDeviceInfo.deviceFingerprint)
            
            val tokenJson = encryption.decrypt(encryptedToken, password)
            val token = json.decodeFromString<DeviceToken>(tokenJson)
            
            // Validate token is not corrupted
            if (token.token.isBlank() || token.deviceId.isBlank()) {
                return@withContext null
            }
            
            token
        } catch (e: Exception) {
            // If decryption fails, token might be from different device or corrupted
            null
        }
    }
    
    /**
     * Check if valid token exists in storage
     * @param appVersion Current app version
     * @return True if valid token exists
     */
    suspend fun hasValidToken(appVersion: String): Boolean = withContext(Dispatchers.IO) {
        val token = getStoredToken(appVersion)
        token != null && token.isValid()
    }
    
    /**
     * Update stored user ID after successful authentication
     * @param userId User ID to store
     * @return True if successfully updated
     */
    suspend fun updateUserId(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            prefs.edit()
                .putString(AuthConstants.KEY_USER_ID, userId)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get stored user ID
     * @return Stored user ID or null
     */
    suspend fun getStoredUserId(): String? = withContext(Dispatchers.IO) {
        prefs.getString(AuthConstants.KEY_USER_ID, null)
    }
    
    /**
     * Clear all stored token data
     * @return True if successfully cleared
     */
    suspend fun clearStoredToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            prefs.edit()
                .remove(AuthConstants.KEY_DEVICE_TOKEN)
                .remove(AuthConstants.KEY_DEVICE_ID)
                .remove(AuthConstants.KEY_USER_ID)
                .remove(AuthConstants.KEY_TOKEN_CREATED_AT)
                .remove(AuthConstants.KEY_TOKEN_EXPIRES_AT)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get token metadata without decrypting full token
     * @return Token metadata or null
     */
    suspend fun getTokenMetadata(): TokenMetadata? = withContext(Dispatchers.IO) {
        try {
            val deviceId = prefs.getString(AuthConstants.KEY_DEVICE_ID, null)
            val userId = prefs.getString(AuthConstants.KEY_USER_ID, null)
            val createdAt = prefs.getLong(AuthConstants.KEY_TOKEN_CREATED_AT, 0L)
            val expiresAt = prefs.getLong(AuthConstants.KEY_TOKEN_EXPIRES_AT, 0L)
            
            if (deviceId != null && createdAt > 0 && expiresAt > 0) {
                TokenMetadata(
                    deviceId = deviceId,
                    userId = userId,
                    createdAt = createdAt,
                    expiresAt = expiresAt,
                    isExpired = System.currentTimeMillis() > expiresAt
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if stored token needs refresh
     * @return True if token exists and needs refresh
     */
    suspend fun needsRefresh(): Boolean = withContext(Dispatchers.IO) {
        val metadata = getTokenMetadata()
        metadata != null && !metadata.isExpired && 
        (metadata.expiresAt - System.currentTimeMillis()) < AuthConstants.REFRESH_THRESHOLD_MILLIS
    }
    
    /**
     * Generate storage password based on device fingerprint
     * @param deviceFingerprint Device fingerprint
     * @return Secure password for encryption
     */
    private fun generateStoragePassword(deviceFingerprint: String): String {
        return encryption.generateSecurePassword(deviceFingerprint, "token_storage")
    }
    
    /**
     * Migrate old token format if needed
     * @param appVersion Current app version
     * @return True if migration was performed
     */
    suspend fun migrateTokenIfNeeded(appVersion: String): Boolean = withContext(Dispatchers.IO) {
        // Check if old unencrypted token exists
        val oldToken = prefs.getString("old_device_token", null)
        if (oldToken != null) {
            try {
                // Clear old token
                prefs.edit().remove("old_device_token").apply()
                // Force new token generation
                clearStoredToken()
                return@withContext true
            } catch (e: Exception) {
                // Migration failed, continue with existing token
            }
        }
        false
    }
}

/**
 * Token metadata without sensitive data
 */
data class TokenMetadata(
    val deviceId: String,
    val userId: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val isExpired: Boolean
) {
    /**
     * Get remaining time until expiration
     */
    fun getRemainingTime(): Long = maxOf(0, expiresAt - System.currentTimeMillis())
    
    /**
     * Check if token needs refresh
     */
    fun needsRefresh(): Boolean = !isExpired && getRemainingTime() < AuthConstants.REFRESH_THRESHOLD_MILLIS
}