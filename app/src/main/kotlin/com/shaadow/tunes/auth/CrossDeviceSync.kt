package com.shaadow.tunes.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Handles cross-device token synchronization and management
 */
class CrossDeviceSync(
    private val tokenStorage: SecureTokenStorage,
    private val deviceInfoCollector: DeviceInfoCollector
) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Export device token for cross-device sync
     * @param appVersion Current app version
     * @return Exportable token data or null if no token
     */
    suspend fun exportTokenForSync(appVersion: String): ExportableTokenData? = withContext(Dispatchers.IO) {
        try {
            val token = tokenStorage.getStoredToken(appVersion)
            val userId = tokenStorage.getStoredUserId()
            
            if (token != null && userId != null) {
                ExportableTokenData(
                    userId = userId,
                    deviceId = token.deviceId,
                    tokenHash = hashToken(token.token), // Don't export actual token
                    createdAt = token.createdAt,
                    expiresAt = token.expiresAt,
                    deviceInfo = token.deviceInfo,
                    exportedAt = System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Import token data from another device
     * @param exportedData Token data from another device
     * @param newToken New token generated for this device
     * @param appVersion Current app version
     * @return Import result
     */
    suspend fun importTokenFromDevice(
        exportedData: ExportableTokenData,
        newToken: DeviceToken,
        appVersion: String
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            // Validate imported data
            if (!isValidExportedData(exportedData)) {
                return@withContext ImportResult.failure("Invalid exported data")
            }
            
            // Check if devices are compatible
            val compatibility = checkDeviceCompatibility(exportedData.deviceInfo, appVersion)
            if (!compatibility.isCompatible) {
                return@withContext ImportResult.failure("Device compatibility check failed: ${compatibility.reason}")
            }
            
            // Store new token with imported user ID
            val success = tokenStorage.storeToken(newToken) && 
                         tokenStorage.updateUserId(exportedData.userId)
            
            if (success) {
                ImportResult.success(
                    message = "Token imported successfully",
                    syncedUserId = exportedData.userId
                )
            } else {
                ImportResult.failure("Failed to store imported token")
            }
        } catch (e: Exception) {
            ImportResult.failure("Import failed: ${e.message}")
        }
    }
    
    /**
     * Generate sync code for easy device pairing
     * @param appVersion Current app version
     * @return Sync code or null if no token
     */
    suspend fun generateSyncCode(appVersion: String): String? = withContext(Dispatchers.IO) {
        try {
            val exportData = exportTokenForSync(appVersion)
            if (exportData != null) {
                val syncData = SyncCodeData(
                    userId = exportData.userId,
                    deviceModel = exportData.deviceInfo.deviceModel,
                    expiresAt = System.currentTimeMillis() + (5 * 60 * 1000L), // 5 minutes
                    checksum = generateChecksum(exportData)
                )
                
                // Encode as compact string
                encodeSyncCode(syncData)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse sync code from another device
     * @param syncCode Sync code string
     * @return Parsed sync data or null if invalid
     */
    suspend fun parseSyncCode(syncCode: String): SyncCodeData? = withContext(Dispatchers.IO) {
        try {
            val syncData = decodeSyncCode(syncCode)
            
            // Validate sync code hasn't expired
            if (System.currentTimeMillis() > syncData.expiresAt) {
                return@withContext null
            }
            
            syncData
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check device compatibility for sync
     * @param otherDeviceInfo Device info from other device
     * @param appVersion Current app version
     * @return Compatibility result
     */
    private fun checkDeviceCompatibility(
        otherDeviceInfo: DeviceInfo,
        appVersion: String
    ): CompatibilityResult {
        try {
            val currentDeviceInfo = deviceInfoCollector.collectDeviceInfo(appVersion)
            
            // Check app version compatibility
            if (!isAppVersionCompatible(otherDeviceInfo.appVersion, appVersion)) {
                return CompatibilityResult.incompatible("App version mismatch")
            }
            
            // Check OS compatibility
            if (!isOsCompatible(otherDeviceInfo.osVersion, currentDeviceInfo.osVersion)) {
                return CompatibilityResult.incompatible("OS version too different")
            }
            
            return CompatibilityResult.compatible()
        } catch (e: Exception) {
            return CompatibilityResult.incompatible("Compatibility check failed")
        }
    }
    
    /**
     * Validate exported token data
     */
    private fun isValidExportedData(data: ExportableTokenData): Boolean {
        return data.userId.isNotBlank() &&
               data.deviceId.isNotBlank() &&
               data.tokenHash.isNotBlank() &&
               data.createdAt > 0 &&
               data.expiresAt > data.createdAt &&
               data.expiresAt > System.currentTimeMillis()
    }
    
    /**
     * Check app version compatibility
     */
    private fun isAppVersionCompatible(otherVersion: String, currentVersion: String): Boolean {
        // Simple version compatibility check
        return try {
            val otherParts = otherVersion.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
            
            // Major version must match
            otherParts.getOrNull(0) == currentParts.getOrNull(0)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check OS compatibility
     */
    private fun isOsCompatible(otherOs: String, currentOs: String): Boolean {
        // Both should be Android
        return otherOs.contains("Android", ignoreCase = true) && 
               currentOs.contains("Android", ignoreCase = true)
    }
    
    /**
     * Hash token for export (don't expose actual token)
     */
    private fun hashToken(token: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Generate checksum for sync data
     */
    private fun generateChecksum(data: ExportableTokenData): String {
        val combined = "${data.userId}${data.deviceId}${data.tokenHash}${data.createdAt}"
        return hashToken(combined).take(8)
    }
    
    /**
     * Encode sync data as compact string
     */
    private fun encodeSyncCode(data: SyncCodeData): String {
        val jsonString = json.encodeToString(data)
        return android.util.Base64.encodeToString(
            jsonString.toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
    }
    
    /**
     * Decode sync code string
     */
    private fun decodeSyncCode(syncCode: String): SyncCodeData {
        val jsonBytes = android.util.Base64.decode(syncCode, android.util.Base64.URL_SAFE)
        val jsonString = String(jsonBytes)
        return json.decodeFromString<SyncCodeData>(jsonString)
    }
}

/**
 * Exportable token data for cross-device sync
 */
@Serializable
data class ExportableTokenData(
    val userId: String,
    val deviceId: String,
    val tokenHash: String, // Hashed token, not actual token
    val createdAt: Long,
    val expiresAt: Long,
    val deviceInfo: DeviceInfo,
    val exportedAt: Long
)

/**
 * Compact sync code data
 */
@Serializable
data class SyncCodeData(
    val userId: String,
    val deviceModel: String,
    val expiresAt: Long,
    val checksum: String
)

/**
 * Result of token import operation
 */
sealed class ImportResult {
    data class Success(val message: String, val syncedUserId: String) : ImportResult()
    data class Failure(val error: String) : ImportResult()
    
    companion object {
        fun success(message: String, syncedUserId: String) = Success(message, syncedUserId)
        fun failure(error: String) = Failure(error)
    }
    
    val isSuccess: Boolean get() = this is Success
}

/**
 * Device compatibility result
 */
data class CompatibilityResult(
    val isCompatible: Boolean,
    val reason: String? = null
) {
    companion object {
        fun compatible() = CompatibilityResult(true)
        fun incompatible(reason: String) = CompatibilityResult(false, reason)
    }
}