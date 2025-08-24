package com.shaadow.tunes.notification

import android.util.Log

/**
 * Default implementation of NotificationErrorHandler
 * Provides basic error handling and logging for notification operations
 */
class DefaultNotificationErrorHandler : NotificationErrorHandler {
    
    companion object {
        private const val TAG = "NotificationError"
    }
    
    override fun handleDeliveryError(error: Throwable, content: NotificationContent) {
        Log.e(TAG, "Failed to deliver notification: ${content.contentType}", error)
        
        // Log specific error details
        when (error) {
            is SecurityException -> {
                Log.e(TAG, "Permission denied for notification delivery")
                handlePermissionDenied(content)
            }
            is IllegalArgumentException -> {
                Log.e(TAG, "Invalid notification parameters: ${error.message}")
            }
            else -> {
                Log.e(TAG, "Unexpected notification error: ${error.message}")
            }
        }
        
        // Record failure metrics
        logNotificationMetrics(content, false)
    }
    
    override fun handlePermissionDenied(content: NotificationContent) {
        Log.w(TAG, "Notification permission denied for content type: ${content.contentType}")
        
        // Could trigger permission request flow here
        // For now, just log the event
        logNotificationMetrics(content, false)
    }
    
    override fun handleChannelError(channelId: String, error: Throwable) {
        Log.e(TAG, "Failed to create or use notification channel: $channelId", error)
        
        // Could attempt channel recreation or fallback to default channel
        when (error) {
            is SecurityException -> {
                Log.e(TAG, "Security error with notification channel: $channelId")
            }
            is IllegalArgumentException -> {
                Log.e(TAG, "Invalid channel configuration: $channelId - ${error.message}")
            }
            else -> {
                Log.e(TAG, "Unexpected channel error: $channelId - ${error.message}")
            }
        }
    }
    
    override fun logNotificationMetrics(content: NotificationContent, deliveryResult: Boolean) {
        val status = if (deliveryResult) "SUCCESS" else "FAILURE"
        Log.d(TAG, "Notification Metrics - Type: ${content.contentType}, Status: $status, Tone: ${content.personalityTone}")
        
        // In a production app, this would send metrics to analytics service
        // For now, we'll just log locally
        if (deliveryResult) {
            Log.d(TAG, "✅ Notification delivered successfully: ${content.emoji} ${content.title}")
        } else {
            Log.w(TAG, "❌ Notification delivery failed: ${content.emoji} ${content.title}")
        }
        
        // Log additional context for debugging
        Log.d(TAG, "Content details - Title length: ${content.title.split(" ").size} words, Body length: ${content.body.split(" ").size} words")
    }
    
    override fun logSchedulingInfo(message: String) {
        Log.i(TAG, "Scheduling Info: $message")
    }
}