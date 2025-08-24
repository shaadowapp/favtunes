package com.shaadow.tunes.auth

/**
 * Interface for device token-based authentication system
 * Provides secure, professional, and easy-to-setup authentication
 */
interface DeviceTokenAuth {
    
    /**
     * Generate a new cryptographically secure device token
     * @param request Token generation request with device info
     * @return Generated device token or null if generation fails
     */
    suspend fun generateDeviceToken(request: TokenGenerationRequest): DeviceToken?
    
    /**
     * Authenticate user with existing device token
     * @param token The device token string
     * @return Authentication result with user info or error details
     */
    suspend fun authenticateWithToken(token: String): AuthResult
    
    /**
     * Refresh an existing device token (30-day rotation)
     * @param currentToken The current token to refresh
     * @return New device token or null if refresh fails
     */
    suspend fun refreshToken(currentToken: String): DeviceToken?
    
    /**
     * Refresh token with additional device verification
     * @param request Token refresh request with device info
     * @return Authentication result with new token or error
     */
    suspend fun refreshTokenWithVerification(request: TokenRefreshRequest): AuthResult
    
    /**
     * Revoke a device token (logout)
     * @param token The token to revoke
     * @return True if successfully revoked, false otherwise
     */
    suspend fun revokeToken(token: String): Boolean
    
    /**
     * Validate token without full authentication
     * @param token The token to validate
     * @return True if token is valid and not expired
     */
    suspend fun validateToken(token: String): Boolean
    
    /**
     * Get device info for current device
     * @return DeviceInfo object with current device details
     */
    fun getCurrentDeviceInfo(): com.shaadow.tunes.models.DeviceInfo
    
    /**
     * Check if device token exists locally
     * @return True if a valid token exists in secure storage
     */
    suspend fun hasValidToken(): Boolean
    
    /**
     * Get stored device token from secure storage
     * @return Stored device token or null if none exists
     */
    suspend fun getStoredToken(): DeviceToken?
    
    /**
     * Store device token securely
     * @param token The token to store
     * @return True if successfully stored
     */
    suspend fun storeToken(token: DeviceToken): Boolean
    
    /**
     * Clear stored token from secure storage
     * @return True if successfully cleared
     */
    suspend fun clearStoredToken(): Boolean
}

/**
 * Listener interface for authentication events
 */
interface AuthEventListener {
    fun onTokenGenerated(token: DeviceToken)
    fun onTokenRefreshed(oldToken: String, newToken: DeviceToken)
    fun onTokenRevoked(token: String)
    fun onAuthenticationSuccess(userId: String)
    fun onAuthenticationFailure(error: AuthErrorType, message: String)
}

/**
 * Configuration for device token authentication
 */
data class DeviceTokenConfig(
    val tokenExpirationDays: Int = 30,
    val refreshThresholdDays: Int = 7,
    val maxRetryAttempts: Int = 3,
    val encryptionEnabled: Boolean = true,
    val autoRefreshEnabled: Boolean = true
)