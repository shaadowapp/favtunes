package com.shaadow.tunes.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.shaadow.tunes.MainActivity


/**
 * Utility class to diagnose activity-related issues during app updates
 */
object ActivityDiagnostics {
    private const val TAG = "ActivityDiagnostics"
    
    /**
     * Verifies that all required activities are properly registered and accessible
     */
    fun verifyActivities(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            
            // Check MainActivity
            val mainActivityName = "${packageName}.MainActivity"
            val mainActivityInfo = packageManager.getActivityInfo(
                android.content.ComponentName(context, MainActivity::class.java),
                PackageManager.GET_META_DATA
            )
            Log.d(TAG, "MainActivity found: ${mainActivityInfo.name}")
            
            // Try to instantiate activities (basic class loading test)
            val mainActivityClass = Class.forName("com.shaadow.tunes.MainActivity")
            
            Log.d(TAG, "All activities verified successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Activity verification failed", e)
            false
        }
    }
    
    /**
     * Clears app data that might cause conflicts during updates
     */
    fun clearAppCache(context: Context) {
        try {
            // Clear shared preferences that might cause conflicts
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            
            // Clear cache directory
            context.cacheDir.deleteRecursively()
            
            Log.d(TAG, "App cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear app cache", e)
        }
    }
    
    /**
     * Logs system information that might be relevant to activity issues
     */
    fun logSystemInfo(context: Context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)
            Log.d(TAG, "App version: ${packageInfo.versionName} (${packageInfo.versionCode})")
            Log.d(TAG, "Target SDK: ${packageInfo.applicationInfo?.targetSdkVersion ?: "unknown"}")
            Log.d(TAG, "Min SDK: ${packageInfo.applicationInfo?.minSdkVersion ?: "unknown"}")
            Log.d(TAG, "Package name: ${context.packageName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log system info", e)
        }
    }
}