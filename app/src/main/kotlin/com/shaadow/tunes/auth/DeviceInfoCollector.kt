package com.shaadow.tunes.auth

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

/**
 * Utility class for collecting device information for authentication
 */
class DeviceInfoCollector(private val context: Context) {
    
    /**
     * Collect comprehensive device information
     * @param appVersion Current app version
     * @return DeviceInfo object with collected information
     */
    fun collectDeviceInfo(appVersion: String): DeviceInfo {
        return DeviceInfo(
            deviceModel = getDeviceModel(),
            osVersion = getOsVersion(),
            appVersion = appVersion,
            deviceFingerprint = generateDeviceFingerprint(),
            registrationTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Get device model information
     */
    private fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    }
    
    /**
     * Get OS version information
     */
    private fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }
    
    /**
     * Generate a unique device fingerprint
     * Uses multiple device identifiers to create a stable fingerprint
     */
    private fun generateDeviceFingerprint(): String {
        val identifiers = listOf(
            Build.BOARD,
            Build.BRAND,
            Build.DEVICE,
            Build.HARDWARE,
            Build.MANUFACTURER,
            Build.MODEL,
            Build.PRODUCT,
            getAndroidId()
        ).joinToString("|")
        
        return hashString(identifiers)
    }
    
    /**
     * Get Android ID (stable across app installs on same device)
     */
    private fun getAndroidId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    /**
     * Hash a string using SHA-256
     */
    private fun hashString(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input.hashCode().toString()
        }
    }
    
    /**
     * Validate device info matches current device
     * @param deviceInfo The device info to validate
     * @return True if device info matches current device
     */
    fun validateDeviceInfo(deviceInfo: DeviceInfo, appVersion: String): Boolean {
        val currentInfo = collectDeviceInfo(appVersion)
        return deviceInfo.deviceFingerprint == currentInfo.deviceFingerprint &&
               deviceInfo.deviceModel == currentInfo.deviceModel
    }
    
    /**
     * Check if device info is significantly different (device change)
     * @param deviceInfo The device info to check
     * @return True if device appears to have changed
     */
    fun isDeviceChanged(deviceInfo: DeviceInfo, appVersion: String): Boolean {
        return !validateDeviceInfo(deviceInfo, appVersion)
    }
}