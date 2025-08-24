package com.shaadow.tunes.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.shaadow.tunes.models.DeviceInfo
import java.util.Locale

/**
 * Utility class for collecting device information for bug reports and feedback
 * This collector focuses on gathering diagnostic information useful for debugging
 */
class BugReportDeviceInfoCollector(private val context: Context) {
    
    /**
     * Collect comprehensive device information for bug reporting
     * @param appVersion Current app version
     * @return DeviceInfo object with collected diagnostic information
     */
    fun collectDeviceInfo(appVersion: String): DeviceInfo {
        return DeviceInfo(
            deviceModel = getDeviceModel(),
            osVersion = getOsVersion(),
            appVersion = appVersion,
            locale = getLocale(),
            screenResolution = getScreenResolution(),
            availableMemory = getAvailableMemory(),
            networkType = getNetworkType()
        )
    }
    
    /**
     * Get device model information in a readable format
     */
    private fun getDeviceModel(): String {
        return try {
            "${Build.MANUFACTURER} ${Build.MODEL}".trim()
        } catch (e: Exception) {
            "Unknown Device"
        }
    }
    
    /**
     * Get OS version information including API level
     */
    private fun getOsVersion(): String {
        return try {
            "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        } catch (e: Exception) {
            "Unknown OS"
        }
    }
    
    /**
     * Get current device locale
     */
    private fun getLocale(): String {
        return try {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
            "${locale.language}-${locale.country}"
        } catch (e: Exception) {
            Locale.getDefault().toString()
        }
    }
    
    /**
     * Get screen resolution information
     */
    private fun getScreenResolution(): String {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = windowManager.defaultDisplay
                display?.getRealMetrics(displayMetrics)
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getMetrics(displayMetrics)
            }
            
            "${displayMetrics.widthPixels}x${displayMetrics.heightPixels} (${displayMetrics.densityDpi} dpi)"
        } catch (e: Exception) {
            "Unknown Resolution"
        }
    }
    
    /**
     * Get available memory information
     */
    private fun getAvailableMemory(): Long {
        return try {
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            
            // Return available memory in bytes (max - used)
            maxMemory - (totalMemory - freeMemory)
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Get current network type
     */
    private fun getNetworkType(): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                
                when {
                    capabilities == null -> "No Connection"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
                    else -> "Unknown"
                }
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                when (networkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI -> "WiFi"
                    ConnectivityManager.TYPE_MOBILE -> "Cellular"
                    ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
                    ConnectivityManager.TYPE_BLUETOOTH -> "Bluetooth"
                    else -> if (networkInfo?.isConnected == true) "Unknown" else "No Connection"
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Get additional diagnostic information that might be useful for debugging
     */
    fun getAdditionalDiagnosticInfo(): Map<String, String> {
        return try {
            mapOf<String, String>(
                "buildType" to (if (Build.TYPE == "user") "Release" else "Debug"),
                "abi" to Build.SUPPORTED_ABIS.joinToString(", "),
                "bootloader" to Build.BOOTLOADER,
                "hardware" to Build.HARDWARE,
                "board" to Build.BOARD,
                "brand" to Build.BRAND,
                "device" to Build.DEVICE,
                "product" to Build.PRODUCT,
                "fingerprint" to Build.FINGERPRINT.take(50) + "...", // Truncate for privacy
                "javaVmVersion" to (System.getProperty("java.vm.version") ?: "Unknown")
            )
        } catch (e: Exception) {
            emptyMap<String, String>()
        }
    }
    
    /**
     * Check if the device info collection requires any permissions
     * @return List of permissions that might be needed (currently none for basic info)
     */
    fun getRequiredPermissions(): List<String> {
        // Current implementation doesn't require special permissions
        // All information is available through public APIs
        return emptyList()
    }
    
    /**
     * Validate that collected device info is reasonable
     * @param deviceInfo The device info to validate
     * @return True if the device info appears valid
     */
    fun validateDeviceInfo(deviceInfo: DeviceInfo): Boolean {
        return deviceInfo.deviceModel.isNotBlank() &&
               deviceInfo.osVersion.isNotBlank() &&
               deviceInfo.appVersion.isNotBlank() &&
               deviceInfo.locale.isNotBlank() &&
               deviceInfo.screenResolution.isNotBlank() &&
               deviceInfo.networkType.isNotBlank()
    }
}