package com.shaadow.tunes.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Implementation of DeviceTokenAuth interface providing secure device authentication
 */
class DeviceTokenAuthImpl(
    private val context: Context,
    private val config: DeviceTokenConfig = DeviceTokenConfig()
) : DeviceTokenAuth {
    
    private val tokenGenerator = TokenGenerator()
    private val encryption = TokenEncryption()
    private val deviceInfoCollector = DeviceInfoCollector(context)
    private val tokenStorage = SecureTokenStorage(context, encryption, deviceInfoCollector)
    private val tokenValidator = TokenValidator(context, deviceInfoCollector)
    private val refreshManager = TokenRefreshManager(
        context, tokenStorage, tokenGenerator, deviceInfoCollector, config
    )
    
    private var eventListener: AuthEventListener? = null
    
    /**
     * Set event listener for authentication events
     */
    fun setEventListener(listener: AuthEventListener) {
        this.eventListener = listener
        refreshManager.setEventListener(listener)
    }
    
    /**
     * Get token storage instance
     */
    fun getTokenStorage(): SecureTokenStorage = tokenStorage
    
    /**
     * Get device info collector instance
     */
    fun getDeviceInfoCollector(): DeviceInfoCollector = deviceInfoCollector
    
    /**
     * Get refresh manager instance
     */
    fun getRefreshManager(): TokenRefreshManager = refreshManager
    
    override suspend fun generateDeviceToken(request: TokenGenerationRequest): DeviceToken? {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenGenerator.generateTokenWithExpiration(
                    deviceInfo = request.deviceInfo,
                    expirationDays = config.tokenExpirationDays
                )
                
                val stored = tokenStorage.storeToken(token)
                if (stored) {
                    request.userId?.let { tokenStorage.updateUserId(it) }
                    eventListener?.onTokenGenerated(token)
                    token
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun authenticateWithToken(token: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // Quick format validation
                if (!tokenValidator.quickValidateTokenFormat(token)) {
                    return@withContext AuthResult.failure(
                        "Invalid token format",
                        AuthErrorType.TOKEN_INVALID
                    )
                }
                
                // Get stored token for comparison
                val storedToken = tokenStorage.getStoredToken(getCurrentAppVersion())
                if (storedToken == null) {
                    return@withContext AuthResult.failure(
                        "No stored token found",
                        AuthErrorType.TOKEN_INVALID
                    )
                }
                
                // Validate token matches stored token
                if (!encryption.secureEquals(token, storedToken.token)) {
                    return@withContext AuthResult.failure(
                        "Token mismatch",
                        AuthErrorType.TOKEN_INVALID
                    )
                }
                
                // Comprehensive token validation
                val validation = tokenValidator.validateToken(storedToken, getCurrentAppVersion())
                if (!validation.isValid) {
                    val errorType = when {
                        validation.isExpired -> AuthErrorType.TOKEN_EXPIRED
                        validation.deviceChanged -> AuthErrorType.DEVICE_NOT_RECOGNIZED
                        else -> AuthErrorType.TOKEN_INVALID
                    }
                    
                    return@withContext AuthResult.failure(
                        validation.getPrimaryError() ?: "Token validation failed",
                        errorType
                    )
                }
                
                // Get user ID
                val userId = tokenStorage.getStoredUserId()
                if (userId.isNullOrBlank()) {
                    return@withContext AuthResult.failure(
                        "User ID not found",
                        AuthErrorType.TOKEN_INVALID
                    )
                }
                
                // Check if token needs refresh
                val newToken = if (validation.needsRefresh) {
                    val refreshResult = refreshManager.forceRefreshToken(getCurrentAppVersion())
                    if (refreshResult.isSuccess) {
                        (refreshResult as RefreshResult.Success).newToken
                    } else {
                        null
                    }
                } else {
                    null
                }
                
                eventListener?.onAuthenticationSuccess(userId)
                AuthResult.success(userId, newToken)
                
            } catch (e: AuthException) {
                eventListener?.onAuthenticationFailure(e.errorType, e.message ?: "Authentication failed")
                e.toAuthResult()
            } catch (e: Exception) {
                eventListener?.onAuthenticationFailure(AuthErrorType.UNKNOWN_ERROR, e.message ?: "Unknown error")
                e.toAuthResult()
            }
        }
    }
    
    override suspend fun refreshToken(currentToken: String): DeviceToken? {
        return withContext(Dispatchers.IO) {
            try {
                val refreshResult = refreshManager.forceRefreshToken(getCurrentAppVersion())
                if (refreshResult.isSuccess) {
                    (refreshResult as RefreshResult.Success).newToken
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun refreshTokenWithVerification(request: TokenRefreshRequest): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // Validate device info
                if (deviceInfoCollector.isDeviceChanged(request.deviceInfo, getCurrentAppVersion())) {
                    return@withContext AuthResult.failure(
                        "Device verification failed",
                        AuthErrorType.DEVICE_NOT_RECOGNIZED
                    )
                }
                
                // Perform refresh
                val refreshResult = refreshManager.forceRefreshToken(getCurrentAppVersion())
                if (refreshResult.isSuccess) {
                    val newToken = (refreshResult as RefreshResult.Success).newToken
                    val userId = tokenStorage.getStoredUserId()
                    
                    if (userId != null) {
                        AuthResult.success(userId, newToken)
                    } else {
                        AuthResult.failure("User ID not found", AuthErrorType.TOKEN_INVALID)
                    }
                } else {
                    val error = (refreshResult as RefreshResult.Failure).error
                    AuthResult.failure(error, AuthErrorType.UNKNOWN_ERROR)
                }
            } catch (e: Exception) {
                e.toAuthResult()
            }
        }
    }
    
    override suspend fun revokeToken(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val success = tokenStorage.clearStoredToken()
                if (success) {
                    eventListener?.onTokenRevoked(token)
                }
                success
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override suspend fun validateToken(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = authenticateWithToken(token)
                authResult.isSuccess
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override fun getCurrentDeviceInfo(): DeviceInfo {
        return deviceInfoCollector.collectDeviceInfo(getCurrentAppVersion())
    }
    
    override suspend fun hasValidToken(): Boolean {
        return tokenStorage.hasValidToken(getCurrentAppVersion())
    }
    
    override suspend fun getStoredToken(): DeviceToken? {
        return tokenStorage.getStoredToken(getCurrentAppVersion())
    }
    
    override suspend fun storeToken(token: DeviceToken): Boolean {
        return tokenStorage.storeToken(token)
    }
    
    override suspend fun clearStoredToken(): Boolean {
        return tokenStorage.clearStoredToken()
    }
    
    /**
     * Perform seamless login with minimal user input
     * @return Authentication result
     */
    suspend fun performSeamlessLogin(): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if valid token exists
                val storedToken = tokenStorage.getStoredToken(getCurrentAppVersion())
                if (storedToken != null && storedToken.isValid()) {
                    // Authenticate with existing token
                    authenticateWithToken(storedToken.token)
                } else {
                    // Generate new token for first-time setup
                    val deviceInfo = getCurrentDeviceInfo()
                    val request = TokenGenerationRequest(deviceInfo = deviceInfo)
                    val newToken = generateDeviceToken(request)
                    
                    if (newToken != null) {
                        // Auto-authenticate with new token
                        val userId = "user_${newToken.deviceId.take(8)}" // Generate temporary user ID
                        tokenStorage.updateUserId(userId)
                        AuthResult.success(userId, newToken)
                    } else {
                        AuthResult.failure("Failed to generate token", AuthErrorType.UNKNOWN_ERROR)
                    }
                }
            } catch (e: Exception) {
                e.toAuthResult()
            }
        }
    }
    
    /**
     * Handle authentication with retry logic
     * @param token Token to authenticate
     * @param maxRetries Maximum retry attempts
     * @return Authentication result
     */
    suspend fun authenticateWithRetry(token: String, maxRetries: Int = config.maxRetryAttempts): AuthResult {
        var lastResult: AuthResult? = null
        
        repeat(maxRetries) { attempt ->
            val result = withTimeoutOrNull(AuthConstants.NETWORK_TIMEOUT_SECONDS * 1000L) {
                authenticateWithToken(token)
            } ?: AuthResult.failure("Authentication timeout", AuthErrorType.NETWORK_ERROR)
            
            lastResult = result
            
            if (result.isSuccess) {
                return result
            }
            
            // Don't retry for certain error types
            if (result.errorType in listOf(
                AuthErrorType.TOKEN_INVALID,
                AuthErrorType.DEVICE_NOT_RECOGNIZED
            )) {
                return result
            }
            
            // Wait before retry
            if (attempt < maxRetries - 1) {
                kotlinx.coroutines.delay(AuthConstants.RETRY_DELAY_MILLIS * (attempt + 1))
            }
        }
        
        return lastResult ?: AuthResult.failure("Authentication failed after retries", AuthErrorType.UNKNOWN_ERROR)
    }
    
    /**
     * Get comprehensive authentication status
     * @return Current authentication status
     */
    suspend fun getAuthStatus(): AuthStatus {
        return withContext(Dispatchers.IO) {
            try {
                val metadata = tokenStorage.getTokenMetadata()
                val refreshStatus = refreshManager.getRefreshStatus()
                
                AuthStatus(
                    hasToken = metadata != null,
                    isTokenValid = metadata != null && !metadata.isExpired,
                    needsRefresh = metadata?.needsRefresh() == true,
                    userId = tokenStorage.getStoredUserId(),
                    tokenExpiresAt = metadata?.expiresAt,
                    isRefreshing = refreshStatus.isRefreshing,
                    autoRefreshEnabled = refreshStatus.autoRefreshEnabled
                )
            } catch (e: Exception) {
                AuthStatus(hasToken = false, isTokenValid = false)
            }
        }
    }
    
    /**
     * Get current app version
     */
    private fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}

/**
 * Comprehensive authentication status
 */
data class AuthStatus(
    val hasToken: Boolean,
    val isTokenValid: Boolean,
    val needsRefresh: Boolean = false,
    val userId: String? = null,
    val tokenExpiresAt: Long? = null,
    val isRefreshing: Boolean = false,
    val autoRefreshEnabled: Boolean = false
) {
    /**
     * Check if user is authenticated
     */
    val isAuthenticated: Boolean get() = hasToken && isTokenValid && !userId.isNullOrBlank()
    
    /**
     * Get time until token expiration
     */
    fun getTimeUntilExpiration(): Long? {
        return tokenExpiresAt?.let { maxOf(0, it - System.currentTimeMillis()) }
    }
}