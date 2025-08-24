package com.shaadow.tunes.auth

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Secure encryption utility for device tokens using AES-256-GCM
 */
class TokenEncryption {
    
    private val secureRandom = SecureRandom()
    
    /**
     * Encrypt token data using AES-256-GCM with PBKDF2 key derivation
     * @param plaintext Data to encrypt
     * @param password Password for key derivation
     * @return Encrypted data with salt and IV prepended
     */
    fun encrypt(plaintext: String, password: String): String {
        try {
            // Generate salt and IV
            val salt = ByteArray(AuthConstants.SALT_LENGTH)
            val iv = ByteArray(AuthConstants.IV_LENGTH)
            secureRandom.nextBytes(salt)
            secureRandom.nextBytes(iv)
            
            // Derive key using PBKDF2
            val secretKey = deriveKey(password, salt)
            
            // Encrypt using AES-GCM
            val cipher = Cipher.getInstance(AuthConstants.ENCRYPTION_ALGORITHM)
            val gcmSpec = GCMParameterSpec(128, iv) // 128-bit authentication tag
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
            
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            // Combine salt + IV + ciphertext
            val combined = salt + iv + ciphertext
            
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw EncryptionException("Failed to encrypt token data", e)
        }
    }
    
    /**
     * Decrypt token data using AES-256-GCM with PBKDF2 key derivation
     * @param encryptedData Encrypted data with salt and IV
     * @param password Password for key derivation
     * @return Decrypted plaintext
     */
    fun decrypt(encryptedData: String, password: String): String {
        try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            
            // Extract salt, IV, and ciphertext
            val salt = combined.sliceArray(0 until AuthConstants.SALT_LENGTH)
            val iv = combined.sliceArray(AuthConstants.SALT_LENGTH until AuthConstants.SALT_LENGTH + AuthConstants.IV_LENGTH)
            val ciphertext = combined.sliceArray(AuthConstants.SALT_LENGTH + AuthConstants.IV_LENGTH until combined.size)
            
            // Derive key using PBKDF2
            val secretKey = deriveKey(password, salt)
            
            // Decrypt using AES-GCM
            val cipher = Cipher.getInstance(AuthConstants.ENCRYPTION_ALGORITHM)
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            
            val plaintext = cipher.doFinal(ciphertext)
            
            return String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            throw EncryptionException("Failed to decrypt token data", e)
        }
    }
    
    /**
     * Derive encryption key using PBKDF2
     * @param password Password for key derivation
     * @param salt Salt for key derivation
     * @return Derived secret key
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(AuthConstants.KEY_DERIVATION_ALGORITHM)
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            AuthConstants.KEY_DERIVATION_ITERATIONS,
            AuthConstants.KEY_LENGTH
        )
        val key = factory.generateSecret(spec)
        return SecretKeySpec(key.encoded, "AES")
    }
    
    /**
     * Generate a secure password for encryption
     * @param deviceFingerprint Device fingerprint for uniqueness
     * @param additionalEntropy Additional entropy source
     * @return Secure password string
     */
    fun generateSecurePassword(deviceFingerprint: String, additionalEntropy: String = ""): String {
        val combined = deviceFingerprint + additionalEntropy + System.currentTimeMillis()
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(combined.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Validate encrypted data format
     * @param encryptedData Data to validate
     * @return True if data appears to be properly encrypted
     */
    fun validateEncryptedFormat(encryptedData: String): Boolean {
        return try {
            val decoded = Base64.decode(encryptedData, Base64.NO_WRAP)
            decoded.size >= (AuthConstants.SALT_LENGTH + AuthConstants.IV_LENGTH + 16) // Minimum size
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Secure comparison of two strings (constant time to prevent timing attacks)
     * @param a First string
     * @param b Second string
     * @return True if strings are equal
     */
    fun secureEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}