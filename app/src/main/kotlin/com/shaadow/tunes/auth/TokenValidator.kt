package com.shaadow.tunes.auth

import android.content.Context

/**
 * Token validation utility for device authentication
 */
class TokenValidator(
    private val context: Context,
    private val deviceInfoCollector: DeviceInfoCollector
) {
    
    /**
     * Validate device token comprehensively
     * @param token Token to validate
     * @param appVersion Current app version
     * @return Validation result with details
     */
    fun validateToken(token: DeviceToken, appVersion: String): TokenValidationResult {
        val errors = mutableListOf<String>()
        
        // Check token format
        if (!isValidTokenFormat(token.token)) {
            errors.add("Invalid token format")
        }
        
        // Check expiration
        if (token.isExpired()) {
            errors.add("Token has expired")
        }
        
        // Check if token is active
        if (!token.isActive) {
            errors.add("Token is not active")
        }
        
        // Validate device info
        val deviceValidation = validateDeviceInfo(token.deviceInfo, appVersion)
        if (!deviceValidation.isValid) {
            errors.addAll(deviceValidation.errors)
        }
        
        // Check token age (not too old)
        if (isTokenTooOld(token)) {
            errors.add("Token is too old and should be refreshed")
        }
        
        return TokenValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            needsRefresh = token.needsRefresh(),
            isExpired = token.isExpired(),
            deviceChanged = !deviceValidation.isValid
        )
    }
    
    /**
     * Validate token format and structure
     * @param tokenString Token string to validate
     * @return True if format is valid
     */
    private fun isValidTokenFormat(tokenString: String): Boolean {
        return tokenString.length >= AuthConstants.MIN_TOKEN_LENGTH &&
               tokenString.length <= AuthConstants.MAX_TOKEN_LENGTH &&
               tokenString.matches(Regex("^[a-fA-F0-9]+$"))
    }
    
    /**
     * Validate device information against current device
     * @param deviceInfo Device info to validate
     * @param appVersion Current app version
     * @return Device validation result
     */
    private fun validateDeviceInfo(deviceInfo: DeviceInfo, appVersion: String): DeviceValidationResult {
        val errors = mutableListOf<String>()
        
        try {
            // Check if device fingerprint matches
            if (!deviceInfoCollector.validateDeviceInfo(deviceInfo, appVersion)) {
                errors.add("Device fingerprint mismatch")
            }
            
            // Check if device info is reasonable
            if (deviceInfo.deviceModel.isBlank()) {
                errors.add("Invalid device model")
            }
            
            if (deviceInfo.osVersion.isBlank()) {
                errors.add("Invalid OS version")
            }
            
            if (deviceInfo.deviceFingerprint.length < 32) {
                errors.add("Invalid device fingerprint")
            }
            
        } catch (e: Exception) {
            errors.add("Failed to validate device info: ${e.message}")
        }
        
        return DeviceValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Check if token is too old (beyond reasonable refresh period)
     * @param token Token to check
     * @return True if token is too old
     */
    private fun isTokenTooOld(token: DeviceToken): Boolean {
        val maxAge = 60 * AuthConstants.MILLIS_PER_DAY // 60 days max age
        val age = System.currentTimeMillis() - token.createdAt
        return age > maxAge
    }
    
    /**
     * Validate token string format only (quick validation)
     * @param tokenString Token string to validate
     * @return True if format is valid
     */
    fun quickValidateTokenFormat(tokenString: String?): Boolean {
        return tokenString != null && isValidTokenFormat(tokenString)
    }
    
    /**
     * Check if device ID format is valid
     * @param deviceId Device ID to validate
     * @return True if format is valid
     */
    fun validateDeviceIdFormat(deviceId: String?): Boolean {
        return deviceId != null &&
               deviceId.length >= AuthConstants.MIN_DEVICE_ID_LENGTH &&
               deviceId.matches(Regex("^[A-Za-z0-9_-]+$"))
    }
    
    /**
     * Validate authentication result
     * @param authResult Result to validate
     * @return True if result is valid success
     */
    fun validateAuthResult(authResult: AuthResult): Boolean {
        return authResult.isSuccess &&
               !authResult.userId.isNullOrBlank() &&
               authResult.errorMessage == null
    }
}

/**
 * Result of token validation
 */
data class TokenValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val needsRefresh: Boolean,
    val isExpired: Boolean,
    val deviceChanged: Boolean
) {
    /**
     * Get primary error message
     */
    fun getPrimaryError(): String? = errors.firstOrNull()
    
    /**
     * Check if validation failed due to expiration
     */
    fun isFailedDueToExpiration(): Boolean = !isValid && isExpired
    
    /**
     * Check if validation failed due to device change
     */
    fun isFailedDueToDeviceChange(): Boolean = !isValid && deviceChanged
}

/**
 * Result of device validation
 */
data class DeviceValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)