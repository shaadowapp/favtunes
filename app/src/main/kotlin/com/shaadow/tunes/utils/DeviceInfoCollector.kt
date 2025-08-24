package com.shaadow.tunes.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
// BuildConfig import will be handled at runtime
import com.shaadow.tunes.models.DeviceInfo
import java.util.Locale

object DeviceInfoCollector {
    
    fun collectDeviceInfo(context: Context, appVersion: String): DeviceInfo {
        return DeviceInfo(
            deviceModel = getDeviceModel(),
            osVersion = getOSVersion(),
            appVersion = appVersion,
            locale = getLocale(),
            screenResolution = getScreenResolution(context),
            availableMemory = getAvailableMemory(),
            networkType = getNetworkType(context)
        )
    }
    
    private fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
    
    private fun getOSVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }
    

    
    private fun getLocale(): String {
        return Locale.getDefault().toString()
    }
    
    private fun getScreenResolution(context: Context): String {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getAvailableMemory(): Long {
        return try {
            Runtime.getRuntime().maxMemory()
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getNetworkType(context: Context): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            
            when {
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}