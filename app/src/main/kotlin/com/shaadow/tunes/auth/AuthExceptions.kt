package com.shaadow.tunes.auth

/**
 * Base exception for authentication-related errors
 */
sealed class AuthException(
    message: String,
    cause: Throwable? = null,
    val errorType: AuthErrorType
) : Exception(message, cause)

/**
 * Token has expired and needs refresh
 */
class TokenExpiredException(
    message: String = "Device token has expired",
    cause: Throwable? = null
) : AuthException(message, cause, AuthErrorType.TOKEN_EXPIRED)

/**
 * Token is invalid or malformed
 */
class InvalidTokenException(
    message: String = "Device token is invalid",
    cause: Throwable? = null
) : AuthException(message, cause, AuthErrorType.TOKEN_INVALID)

/**
 * Device is not recognized or has changed
 */
class DeviceNotRecognizedException(
    message: String = "Device not recognized",
    cause: Throwable? = null
) : AuthException(message, cause, AuthErrorType.DEVICE_NOT_RECOGNIZED)

/**
 * Network-related authentication error
 */
class NetworkAuthException(
    message: String = "Network error during authentication",
    cause: Throwable? = null
) : AuthException(message, cause, AuthErrorType.NETWORK_ERROR)

/**
 * Encryption/decryption error
 */
class EncryptionException(
    message: String = "Encryption error",
    cause: Throwable? = null
) : AuthException(message, cause, AuthErrorType.ENCRYPTION_ERROR)

/**
 * Unknown authentication error
 */
class UnknownAuthException(
    message: String = "Unknown authentication error",
    cause: Throwable? = null
) : AuthException(message, cause, AuthErrorType.UNKNOWN_ERROR)

/**
 * Extension function to convert exceptions to AuthResult
 */
fun AuthException.toAuthResult(): AuthResult {
    return AuthResult.failure(this.message ?: "Authentication failed", this.errorType)
}

/**
 * Extension function to convert general exceptions to AuthResult
 */
fun Exception.toAuthResult(): AuthResult {
    return when (this) {
        is AuthException -> this.toAuthResult()
        else -> AuthResult.failure(
            this.message ?: "Unknown error occurred",
            AuthErrorType.UNKNOWN_ERROR
        )
    }
}