package com.shaadow.tunes.auth

import android.content.Context
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages automatic token refresh and expiration handling
 */
class TokenRefreshManager(
    private val context: Context,
    private val tokenStorage: SecureTokenStorage,
    private val tokenGenerator: TokenGenerator,
    private val deviceInfoCollector: DeviceInfoCollector,
    private val config: DeviceTokenConfig = DeviceTokenConfig()
) {
    
    private var refreshJob: Job? = null
    private val isRefreshing = AtomicBoolean(false)
    private var eventListener: AuthEventListener? = null
    
    /**
     * Set event listener for authentication events
     */
    fun setEventListener(listener: AuthEventListener) {
        this.eventListener = listener
    }
    
    /**
     * Start automatic token refresh monitoring
     * @param scope Coroutine scope for background operations
     */
    fun startAutoRefresh(scope: CoroutineScope) {
        if (!config.autoRefreshEnabled) return
        
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (isActive) {
                try {
                    checkAndRefreshToken()
                    delay(24 * 60 * 60 * 1000L) // Check daily
                } catch (e: Exception) {
                    // Log error but continue monitoring
                    delay(60 * 60 * 1000L) // Retry in 1 hour on error
                }
            }
        }
    }
    
    /**
     * Stop automatic token refresh monitoring
     */
    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }
    
    /**
     * Check if token needs refresh and perform refresh if needed
     * @param appVersion Current app version
     * @return True if refresh was performed
     */
    suspend fun checkAndRefreshToken(appVersion: String = "1.0.0"): Boolean {
        if (isRefreshing.get()) return false
        
        return withContext(Dispatchers.IO) {
            try {
                val metadata = tokenStorage.getTokenMetadata()
                if (metadata != null && metadata.needsRefresh() && !metadata.isExpired) {
                    performTokenRefresh(appVersion)
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Force token refresh regardless of expiration status
     * @param appVersion Current app version
     * @return RefreshResult with new token or error
     */
    suspend fun forceRefreshToken(appVersion: String = "1.0.0"): RefreshResult {
        if (isRefreshing.get()) {
            return RefreshResult.failure("Refresh already in progress")
        }
        
        return withContext(Dispatchers.IO) {
            try {
                isRefreshing.set(true)
                
                val currentToken = tokenStorage.getStoredToken(appVersion)
                if (currentToken == null) {
                    return@withContext RefreshResult.failure("No token to refresh")
                }
                
                val newToken = generateRefreshedToken(currentToken, appVersion)
                val stored = tokenStorage.storeToken(newToken)
                
                if (stored) {
                    eventListener?.onTokenRefreshed(currentToken.token, newToken)
                    RefreshResult.success(newToken)
                } else {
                    RefreshResult.failure("Failed to store refreshed token")
                }
            } catch (e: Exception) {
                RefreshResult.failure("Refresh failed: ${e.message}")
            } finally {
                isRefreshing.set(false)
            }
        }
    }
    
    /**
     * Perform token refresh operation
     * @param appVersion Current app version
     * @return True if refresh was successful
     */
    private suspend fun performTokenRefresh(appVersion: String): Boolean {
        if (isRefreshing.get()) return false
        
        return try {
            isRefreshing.set(true)
            
            val currentToken = tokenStorage.getStoredToken(appVersion)
            if (currentToken == null || currentToken.isExpired()) {
                return false
            }
            
            val newToken = generateRefreshedToken(currentToken, appVersion)
            val stored = tokenStorage.storeToken(newToken)
            
            if (stored) {
                eventListener?.onTokenRefreshed(currentToken.token, newToken)
            }
            
            stored
        } catch (e: Exception) {
            false
        } finally {
            isRefreshing.set(false)
        }
    }
    
    /**
     * Generate a new token based on existing token
     * @param currentToken Current token to refresh
     * @param appVersion Current app version
     * @return New refreshed token
     */
    private fun generateRefreshedToken(currentToken: DeviceToken, appVersion: String): DeviceToken {
        val updatedDeviceInfo = deviceInfoCollector.collectDeviceInfo(appVersion)
        
        return tokenGenerator.generateTokenWithExpiration(
            deviceInfo = updatedDeviceInfo,
            expirationDays = config.tokenExpirationDays
        ).copy(
            deviceId = currentToken.deviceId // Keep same device ID
        )
    }
    
    /**
     * Handle token expiration
     * @param expiredToken The expired token
     * @param appVersion Current app version
     * @return ExpirationResult with action taken
     */
    suspend fun handleTokenExpiration(
        expiredToken: DeviceToken,
        appVersion: String
    ): ExpirationResult {
        return withContext(Dispatchers.IO) {
            try {
                // Clear expired token
                tokenStorage.clearStoredToken()
                
                // Generate new token
                val newToken = tokenGenerator.generateTokenWithExpiration(
                    deviceInfo = deviceInfoCollector.collectDeviceInfo(appVersion),
                    expirationDays = config.tokenExpirationDays
                )
                
                val stored = tokenStorage.storeToken(newToken)
                
                if (stored) {
                    eventListener?.onTokenGenerated(newToken)
                    ExpirationResult.newTokenGenerated(newToken)
                } else {
                    ExpirationResult.failure("Failed to generate new token")
                }
            } catch (e: Exception) {
                ExpirationResult.failure("Expiration handling failed: ${e.message}")
            }
        }
    }
    
    /**
     * Get refresh status information
     * @return Current refresh status
     */
    fun getRefreshStatus(): RefreshStatus {
        return RefreshStatus(
            isRefreshing = isRefreshing.get(),
            autoRefreshEnabled = config.autoRefreshEnabled,
            isMonitoring = refreshJob?.isActive == true
        )
    }
    
    /**
     * Schedule token refresh at specific time
     * @param delayMillis Delay before refresh in milliseconds
     * @param scope Coroutine scope
     * @param appVersion App version
     */
    fun scheduleRefresh(delayMillis: Long, scope: CoroutineScope, appVersion: String = "1.0.0") {
        scope.launch {
            delay(delayMillis)
            checkAndRefreshToken(appVersion)
        }
    }
}

/**
 * Result of token refresh operation
 */
sealed class RefreshResult {
    data class Success(val newToken: DeviceToken) : RefreshResult()
    data class Failure(val error: String) : RefreshResult()
    
    companion object {
        fun success(token: DeviceToken) = Success(token)
        fun failure(error: String) = Failure(error)
    }
    
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

/**
 * Result of token expiration handling
 */
sealed class ExpirationResult {
    data class NewTokenGenerated(val newToken: DeviceToken) : ExpirationResult()
    data class Failure(val error: String) : ExpirationResult()
    
    companion object {
        fun newTokenGenerated(token: DeviceToken) = NewTokenGenerated(token)
        fun failure(error: String) = Failure(error)
    }
    
    val isSuccess: Boolean get() = this is NewTokenGenerated
}

/**
 * Current refresh status
 */
data class RefreshStatus(
    val isRefreshing: Boolean,
    val autoRefreshEnabled: Boolean,
    val isMonitoring: Boolean
)