package com.shaadow.tunes.auth

import android.content.Context
import android.util.Log
import com.shaadow.tunes.utils.DeviceInfoCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.*
import android.util.Base64

/**
 * Simple device-based authentication using public-private key pairs
 * No complex email/password system - just secure device authentication
 */
class DeviceTokenAuthImpl(
    private val context: Context
) : DeviceTokenAuth {

    companion object {
        private const val TAG = "DeviceAuth"
        private const val PREF_USER_ID = "device_user_id"
        private const val PREF_PRIVATE_KEY = "device_private_key"
        private const val PREF_PUBLIC_KEY = "device_public_key"
        private const val PREF_DEVICE_TOKEN = "device_token"
    }

    private val preferences = context.getSharedPreferences("device_auth", Context.MODE_PRIVATE)

    override suspend fun generateDeviceToken(request: TokenGenerationRequest): DeviceToken? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating device authentication")
            
            // Generate or get user ID
            val userId = getUserId()
            
            // Generate key pair if not exists
            val keyPair = getOrCreateKeyPair()
            
            // Create simple device token with signature
            val deviceInfo = getCurrentDeviceInfo()
            val tokenData = "$userId:${deviceInfo.deviceModel}:${System.currentTimeMillis()}"
            val signature = signData(tokenData, keyPair.private)
            
            val token = DeviceToken(
                token = Base64.encodeToString("$tokenData:$signature".toByteArray(), Base64.NO_WRAP),
                deviceId = userId,
                deviceInfo = DeviceInfo(
                    deviceModel = deviceInfo.deviceModel,
                    osVersion = deviceInfo.osVersion,
                    appVersion = deviceInfo.appVersion,
                    deviceFingerprint = "${deviceInfo.deviceModel}_${deviceInfo.osVersion}"
                ),
                expiresAt = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L), // 30 days
                createdAt = System.currentTimeMillis(),
                isActive = true
            )
            
            // Store token
            preferences.edit().putString(PREF_DEVICE_TOKEN, token.token).apply()
            
            Log.d(TAG, "Device authentication generated for user: $userId")
            return@withContext token
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating device token", e)
            null
        }
    }

    override suspend fun authenticateWithToken(token: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Authenticating with device token")
            
            // Decode token
            val tokenData = String(Base64.decode(token, Base64.NO_WRAP))
            val parts = tokenData.split(":")
            
            if (parts.size != 4) {
                return@withContext AuthResult.failure(
                    "Invalid token format",
                    AuthErrorType.TOKEN_INVALID
                )
            }
            
            val userId = parts[0]
            val deviceModel = parts[1]
            val timestamp = parts[2].toLongOrNull() ?: 0L
            val signature = parts[3]
            
            // Check expiration
            if (System.currentTimeMillis() - timestamp > (30 * 24 * 60 * 60 * 1000L)) {
                return@withContext AuthResult.failure(
                    "Token has expired",
                    AuthErrorType.TOKEN_EXPIRED
                )
            }
            
            // Verify signature
            val keyPair = getOrCreateKeyPair()
            val originalData = "$userId:$deviceModel:$timestamp"
            val isValid = verifySignature(originalData, signature, keyPair.public)
            
            if (!isValid) {
                return@withContext AuthResult.failure(
                    "Invalid token signature",
                    AuthErrorType.TOKEN_INVALID
                )
            }
            
            val currentDeviceInfo = getCurrentDeviceInfo()
            val deviceToken = DeviceToken(
                token = token,
                deviceId = userId,
                deviceInfo = DeviceInfo(
                    deviceModel = currentDeviceInfo.deviceModel,
                    osVersion = currentDeviceInfo.osVersion,
                    appVersion = currentDeviceInfo.appVersion,
                    deviceFingerprint = "${currentDeviceInfo.deviceModel}_${currentDeviceInfo.osVersion}"
                ),
                expiresAt = timestamp + (30 * 24 * 60 * 60 * 1000L),
                createdAt = timestamp,
                isActive = true
            )
            
            Log.d(TAG, "Authentication successful for user: $userId")
            return@withContext AuthResult.success(userId, deviceToken)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during authentication", e)
            AuthResult.failure(
                "Authentication failed: ${e.message}",
                AuthErrorType.UNKNOWN_ERROR
            )
        }
    }

    override suspend fun refreshToken(currentToken: String): DeviceToken? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Refreshing device token")
            
            // Authenticate current token first
            val authResult = authenticateWithToken(currentToken)
            if (!authResult.isSuccess) {
                return@withContext null
            }
            
            // Generate new token with same user ID
            val currentDeviceInfo = getCurrentDeviceInfo()
            val request = TokenGenerationRequest(
                deviceInfo = DeviceInfo(
                    deviceModel = currentDeviceInfo.deviceModel,
                    osVersion = currentDeviceInfo.osVersion,
                    appVersion = currentDeviceInfo.appVersion,
                    deviceFingerprint = "${currentDeviceInfo.deviceModel}_${currentDeviceInfo.osVersion}"
                ),
                userId = authResult.userId
            )
            
            return@withContext generateDeviceToken(request)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token", e)
            null
        }
    }

    override suspend fun refreshTokenWithVerification(request: TokenRefreshRequest): AuthResult = withContext(Dispatchers.IO) {
        try {
            val newToken = refreshToken(request.currentToken)
            if (newToken == null) {
                return@withContext AuthResult.failure(
                    "Failed to refresh token",
                    AuthErrorType.UNKNOWN_ERROR
                )
            }
            
            return@withContext AuthResult.success(newToken.deviceId, newToken)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token with verification", e)
            AuthResult.failure(
                "Token refresh failed: ${e.message}",
                AuthErrorType.UNKNOWN_ERROR
            )
        }
    }

    override suspend fun revokeToken(token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Revoking device token")
            clearStoredToken()
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error revoking token", e)
            false
        }
    }

    override suspend fun validateToken(token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val authResult = authenticateWithToken(token)
            return@withContext authResult.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error validating token", e)
            false
        }
    }

    override fun getCurrentDeviceInfo(): com.shaadow.tunes.models.DeviceInfo {
        return DeviceInfoCollector.collectDeviceInfo(context, "1.0.0")
    }

    override suspend fun hasValidToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            val storedToken = getStoredToken()
            return@withContext storedToken != null && !storedToken.isExpired()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getStoredToken(): DeviceToken? = withContext(Dispatchers.IO) {
        try {
            val tokenString = preferences.getString(PREF_DEVICE_TOKEN, null) ?: return@withContext null
            val authResult = authenticateWithToken(tokenString)
            return@withContext if (authResult.isSuccess) authResult.newToken else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun storeToken(token: DeviceToken): Boolean = withContext(Dispatchers.IO) {
        try {
            preferences.edit().putString(PREF_DEVICE_TOKEN, token.token).apply()
            return@withContext true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun clearStoredToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            preferences.edit()
                .remove(PREF_DEVICE_TOKEN)
                .remove(PREF_USER_ID)
                .remove(PREF_PRIVATE_KEY)
                .remove(PREF_PUBLIC_KEY)
                .apply()
            return@withContext true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Perform seamless login - just authenticate the device
     */
    suspend fun performSeamlessLogin(): AuthResult = withContext(Dispatchers.IO) {
        try {
            // Check if we have a stored token
            val storedTokenString = preferences.getString(PREF_DEVICE_TOKEN, null)
            
            if (storedTokenString != null) {
                // Try to authenticate with existing token
                val authResult = authenticateWithToken(storedTokenString)
                if (authResult.isSuccess) {
                    return@withContext authResult
                }
            }
            
            // Generate new token for first-time or expired token
            val currentDeviceInfo = getCurrentDeviceInfo()
            val request = TokenGenerationRequest(
                deviceInfo = DeviceInfo(
                    deviceModel = currentDeviceInfo.deviceModel,
                    osVersion = currentDeviceInfo.osVersion,
                    appVersion = currentDeviceInfo.appVersion,
                    deviceFingerprint = "${currentDeviceInfo.deviceModel}_${currentDeviceInfo.osVersion}"
                )
            )
            val newToken = generateDeviceToken(request)
            
            if (newToken != null) {
                return@withContext AuthResult.success(newToken.deviceId, newToken)
            } else {
                return@withContext AuthResult.failure(
                    "Failed to generate device token",
                    AuthErrorType.UNKNOWN_ERROR
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during seamless login", e)
            AuthResult.failure(
                "Seamless login failed: ${e.message}",
                AuthErrorType.UNKNOWN_ERROR
            )
        }
    }

    private fun getUserId(): String {
        val existingUserId = preferences.getString(PREF_USER_ID, null)
        if (existingUserId != null) {
            return existingUserId
        }
        
        val newUserId = "user_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        preferences.edit().putString(PREF_USER_ID, newUserId).apply()
        return newUserId
    }

    private fun getOrCreateKeyPair(): KeyPair {
        val privateKeyString = preferences.getString(PREF_PRIVATE_KEY, null)
        val publicKeyString = preferences.getString(PREF_PUBLIC_KEY, null)
        
        if (privateKeyString != null && publicKeyString != null) {
            try {
                // For simplicity, we'll regenerate if needed
                // In production, you'd properly deserialize the keys
            } catch (e: Exception) {
                Log.w(TAG, "Error loading stored keys, generating new ones")
            }
        }
        
        // Generate new key pair
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        val keyPair = keyGen.generateKeyPair()
        
        // Store keys (simplified - in production use proper key serialization)
        val privateKeyEncoded = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
        val publicKeyEncoded = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
        
        preferences.edit()
            .putString(PREF_PRIVATE_KEY, privateKeyEncoded)
            .putString(PREF_PUBLIC_KEY, publicKeyEncoded)
            .apply()
        
        return keyPair
    }

    private fun signData(data: String, privateKey: PrivateKey): String {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data.toByteArray())
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    private fun verifySignature(data: String, signatureString: String, publicKey: PublicKey): Boolean {
        return try {
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(data.toByteArray())
            signature.verify(Base64.decode(signatureString, Base64.NO_WRAP))
        } catch (e: Exception) {
            false
        }
    }
}