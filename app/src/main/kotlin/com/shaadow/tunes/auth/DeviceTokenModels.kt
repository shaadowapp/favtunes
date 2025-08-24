package com.shaadow.tunes.auth

import kotlinx.serialization.Serializable

/**
 * Core data model for device tokens used in authentication
 */
@Serializable
data class DeviceToken(
    val token: String,
    val deviceId: String,
    val createdAt: Long,
    val expiresAt: Long,
    val isActive: Boolean,
    val deviceInfo: DeviceInfo
) {
    /**
     * Check if the token is expired
     */
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
    
    /**
     * Check if the token is valid (active and not expired)
     */
    fun isValid(): Boolean = isActive && !isExpired()
    
    /**
     * Get remaining time until expiration in milliseconds
     */
    fun getRemainingTime(): Long = maxOf(0, expiresAt - System.currentTimeMillis())
    
    /**
     * Check if token needs refresh (expires within 7 days)
     */
    fun needsRefresh(): Boolean = getRemainingTime() < 7 * 24 * 60 * 60 * 1000L
}

/**
 * Device information for token identification
 */
@Serializable
data class DeviceInfo(
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val deviceFingerprint: String,
    val registrationTime: Long = System.currentTimeMillis()
)

/**
 * Result of authentication operations
 */
@Serializable
data class AuthResult(
    val isSuccess: Boolean,
    val userId: String? = null,
    val errorMessage: String? = null,
    val newToken: DeviceToken? = null,
    val errorType: AuthErrorType? = null
) {
    companion object {
        fun success(userId: String, token: DeviceToken? = null) = AuthResult(
            isSuccess = true,
            userId = userId,
            newToken = token
        )
        
        fun failure(error: String, errorType: AuthErrorType) = AuthResult(
            isSuccess = false,
            errorMessage = error,
            errorType = errorType
        )
    }
}

/**
 * Types of authentication errors
 */
enum class AuthErrorType {
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    DEVICE_NOT_RECOGNIZED,
    NETWORK_ERROR,
    ENCRYPTION_ERROR,
    UNKNOWN_ERROR
}

/**
 * Token refresh request data
 */
@Serializable
data class TokenRefreshRequest(
    val currentToken: String,
    val deviceId: String,
    val deviceInfo: DeviceInfo
)

/**
 * Token generation request data
 */
@Serializable
data class TokenGenerationRequest(
    val deviceInfo: DeviceInfo,
    val userId: String? = null
)