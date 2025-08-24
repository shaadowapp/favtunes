package com.shaadow.tunes.auth

/**
 * Constants for device token authentication system
 */
object AuthConstants {
    
    // Token configuration
    const val DEFAULT_TOKEN_EXPIRATION_DAYS = 30
    const val DEFAULT_REFRESH_THRESHOLD_DAYS = 7
    const val TOKEN_LENGTH = 64 // characters
    const val MAX_RETRY_ATTEMPTS = 3
    
    // Storage keys
    const val PREF_NAME = "device_auth_prefs"
    const val KEY_DEVICE_TOKEN = "device_token"
    const val KEY_DEVICE_ID = "device_id"
    const val KEY_USER_ID = "user_id"
    const val KEY_TOKEN_CREATED_AT = "token_created_at"
    const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
    const val KEY_DEVICE_INFO = "device_info"
    
    // Encryption
    const val ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
    const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    const val KEY_DERIVATION_ITERATIONS = 10000
    const val KEY_LENGTH = 256 // bits
    const val IV_LENGTH = 12 // bytes for GCM
    const val SALT_LENGTH = 16 // bytes
    
    // Time constants
    const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    const val TOKEN_EXPIRATION_MILLIS = DEFAULT_TOKEN_EXPIRATION_DAYS * MILLIS_PER_DAY
    const val REFRESH_THRESHOLD_MILLIS = DEFAULT_REFRESH_THRESHOLD_DAYS * MILLIS_PER_DAY
    
    // Network timeouts
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val RETRY_DELAY_MILLIS = 1000L
    
    // Validation
    const val MIN_TOKEN_LENGTH = 32
    const val MAX_TOKEN_LENGTH = 128
    const val MIN_DEVICE_ID_LENGTH = 16
    
    // Error messages
    const val ERROR_TOKEN_EXPIRED = "Device token has expired"
    const val ERROR_TOKEN_INVALID = "Device token is invalid"
    const val ERROR_DEVICE_NOT_RECOGNIZED = "Device not recognized"
    const val ERROR_NETWORK_FAILURE = "Network error during authentication"
    const val ERROR_ENCRYPTION_FAILURE = "Failed to encrypt/decrypt token"
    const val ERROR_STORAGE_FAILURE = "Failed to store/retrieve token"
    const val ERROR_UNKNOWN = "Unknown authentication error"
}