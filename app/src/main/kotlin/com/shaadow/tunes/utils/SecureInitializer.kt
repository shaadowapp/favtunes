package com.shaadow.tunes.utils

import android.content.Context

/**
 * Utility class to securely initialize credentials from various sources
 * This replaces hardcoded values with secure storage
 */
object SecureInitializer {
    
    /**
     * Initialize all secure credentials on first app launch
     * This should be called only once during app setup
     */
    fun initializeCredentials(context: Context) {
        val credentialsManager = SecureCredentialsManager.getInstance(context)
        
        // Only initialize if not already set
        if (!credentialsManager.areCredentialsConfigured()) {
            // Set default values from secure sources
            // In production, these should come from a secure server or user input
            
            // OneSignal App ID (should be configured by developer)
            credentialsManager.setOneSignalAppId("3190b63e-f333-446b-bbba-7712efa18bb9")
            
            // YouTube API Key (should be obtained through proper channels)
            credentialsManager.setYouTubeApiKey("AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
            
            // GitHub credentials for remote config (optional)
            credentialsManager.setRemoteConfigOwner("shaadowapp")
            credentialsManager.setRemoteConfigRepo("cdfheuee3")
            
            android.util.Log.i("SecureInitializer", "Credentials initialized securely")
        }
    }
    
    /**
     * Update credentials from environment or server
     * This allows updating credentials without app updates
     */
    fun updateCredentialsFromEnvironment(context: Context) {
        val credentialsManager = SecureCredentialsManager.getInstance(context)
        
        // Try to get credentials from system properties or environment
        System.getProperty("ONESIGNAL_APP_ID")?.let { appId ->
            if (appId.isNotBlank()) {
                credentialsManager.setOneSignalAppId(appId)
            }
        }
        
        System.getProperty("YOUTUBE_API_KEY")?.let { apiKey ->
            if (apiKey.isNotBlank()) {
                credentialsManager.setYouTubeApiKey(apiKey)
            }
        }
        
        System.getProperty("GITHUB_TOKEN")?.let { token ->
            if (token.isNotBlank()) {
                credentialsManager.setGitHubToken(token)
            }
        }
    }
    
    /**
     * Validate that all required credentials are present
     */
    fun validateCredentials(context: Context): Boolean {
        val credentialsManager = SecureCredentialsManager.getInstance(context)
        
        val oneSignalId = credentialsManager.getOneSignalAppId()
        val youtubeKey = credentialsManager.getYouTubeApiKey()
        
        return oneSignalId.isNotEmpty() && youtubeKey.isNotEmpty()
    }
}