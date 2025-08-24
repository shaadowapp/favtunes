package com.shaadow.tunes.auth

import java.security.SecureRandom
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Secure token generation utility using cryptographically secure methods
 */
class TokenGenerator {
    
    private val secureRandom = SecureRandom()
    
    /**
     * Generate a cryptographically secure device token
     * @param length Token length in characters (default 64)
     * @return Secure random token string
     */
    fun generateSecureToken(length: Int = AuthConstants.TOKEN_LENGTH): String {
        require(length >= AuthConstants.MIN_TOKEN_LENGTH) {
            "Token length must be at least ${AuthConstants.MIN_TOKEN_LENGTH} characters"
        }
        require(length <= AuthConstants.MAX_TOKEN_LENGTH) {
            "Token length must not exceed ${AuthConstants.MAX_TOKEN_LENGTH} characters"
        }
        
        val tokenBytes = ByteArray(length / 2) // Each byte becomes 2 hex characters
        secureRandom.nextBytes(tokenBytes)
        
        return tokenBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Generate a unique device ID
     * @return Secure device ID string
     */
    fun generateDeviceId(): String {
        val deviceIdBytes = ByteArray(16) // 128-bit device ID
        secureRandom.nextBytes(deviceIdBytes)
        
        return Base64.getUrlEncoder().withoutPadding().encodeToString(deviceIdBytes)
    }
    
    /**
     * Generate a secure salt for encryption
     * @param length Salt length in bytes (default 16)
     * @return Random salt bytes
     */
    fun generateSalt(length: Int = AuthConstants.SALT_LENGTH): ByteArray {
        val salt = ByteArray(length)
        secureRandom.nextBytes(salt)
        return salt
    }
    
    /**
     * Generate a secure initialization vector for AES-GCM
     * @param length IV length in bytes (default 12 for GCM)
     * @return Random IV bytes
     */
    fun generateIV(length: Int = AuthConstants.IV_LENGTH): ByteArray {
        val iv = ByteArray(length)
        secureRandom.nextBytes(iv)
        return iv
    }
    
    /**
     * Generate a secure AES key
     * @param keySize Key size in bits (default 256)
     * @return Generated secret key
     */
    fun generateAESKey(keySize: Int = AuthConstants.KEY_LENGTH): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(keySize, secureRandom)
        return keyGenerator.generateKey()
    }
    
    /**
     * Validate token format and security
     * @param token Token to validate
     * @return True if token meets security requirements
     */
    fun validateTokenFormat(token: String): Boolean {
        return token.length >= AuthConstants.MIN_TOKEN_LENGTH &&
               token.length <= AuthConstants.MAX_TOKEN_LENGTH &&
               token.matches(Regex("^[a-fA-F0-9]+$")) // Hex characters only
    }
    
    /**
     * Generate token with expiration
     * @param expirationDays Days until token expires
     * @return DeviceToken with expiration set
     */
    fun generateTokenWithExpiration(
        deviceInfo: DeviceInfo,
        expirationDays: Int = AuthConstants.DEFAULT_TOKEN_EXPIRATION_DAYS
    ): DeviceToken {
        val now = System.currentTimeMillis()
        val expirationTime = now + (expirationDays * AuthConstants.MILLIS_PER_DAY)
        
        return DeviceToken(
            token = generateSecureToken(),
            deviceId = generateDeviceId(),
            createdAt = now,
            expiresAt = expirationTime,
            isActive = true,
            deviceInfo = deviceInfo
        )
    }
}