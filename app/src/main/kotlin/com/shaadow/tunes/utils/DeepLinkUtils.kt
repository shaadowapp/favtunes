package com.shaadow.tunes.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object DeepLinkUtils {
    
    private const val SCHEME = "favtunes"
    private const val HOST = "app"
    
    /**
     * Create a deep link intent to the bug report screen
     */
    fun createBugReportIntent(context: Context): Intent {
        val uri = Uri.parse("$SCHEME://$HOST/bugreport")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
    
    /**
     * Create a deep link intent to the feedback screen
     */
    fun createFeedbackIntent(context: Context): Intent {
        val uri = Uri.parse("$SCHEME://$HOST/feedback")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
    
    /**
     * Create a deep link intent to the settings screen
     */
    fun createSettingsIntent(context: Context): Intent {
        val uri = Uri.parse("$SCHEME://$HOST/settings")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
    
    /**
     * Parse deep link and return the destination route
     */
    fun parseDeepLink(uri: Uri): String? {
        return if (uri.scheme == SCHEME && uri.host == HOST) {
            uri.path?.removePrefix("/")
        } else {
            null
        }
    }
    
    /**
     * Check if the given URI is a valid deep link for this app
     */
    fun isValidDeepLink(uri: Uri): Boolean {
        return uri.scheme == SCHEME && uri.host == HOST
    }
    
    /**
     * Get all supported deep link routes
     */
    fun getSupportedRoutes(): List<String> {
        return listOf(
            "bugreport",
            "feedback", 
            "settings",
            "home",
            "songs",
            "artists",
            "playlists"
        )
    }
}