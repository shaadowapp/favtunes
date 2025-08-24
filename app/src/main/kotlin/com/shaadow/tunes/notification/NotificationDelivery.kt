package com.shaadow.tunes.notification

import android.app.PendingIntent

/**
 * Interface for notification delivery operations
 * Separates delivery logic from content generation
 */
interface NotificationDelivery {
    
    /**
     * Show engagement notification to encourage app usage
     * @param content Notification content with title, body, and action
     */
    fun showEngagementNotification(content: NotificationContent)
    
    /**
     * Show music suggestion notification
     * @param content Notification content with optional song reference
     */
    fun showMusicSuggestion(content: NotificationContent)
    
    /**
     * Show marketing notification
     * @param content Marketing notification content
     */
    fun showMarketingNotification(content: NotificationContent)
    
    /**
     * Show trending music notification
     * @param content Notification content with song information
     */
    fun showTrendingNotification(content: NotificationContent)
    
    /**
     * Show personalized music suggestion
     * @param content Personalized content based on user context
     */
    fun showPersonalizedSuggestion(content: NotificationContent)
    
    /**
     * Show rating request notification
     * @param content Rating request content
     */
    fun showRatingNotification(content: NotificationContent)
    
    /**
     * Show custom notification with specific pending intent
     * @param content Notification content
     * @param pendingIntent Custom pending intent for notification action
     */
    fun showCustomNotification(content: NotificationContent, pendingIntent: PendingIntent)
    
    /**
     * Cancel notification by ID
     * @param notificationId ID of notification to cancel
     */
    fun cancelNotification(notificationId: Int)
    
    /**
     * Cancel all notifications from this app
     */
    fun cancelAllNotifications()
    
    /**
     * Check if notifications are enabled
     * @return True if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean
    
    /**
     * Get next available notification ID
     * @return Unique notification ID
     */
    fun generateNotificationId(): Int
}

/**
 * Interface for notification building operations
 * Handles the technical aspects of creating Android notifications
 */
interface NotificationBuilder {
    
    /**
     * Build basic notification
     * @param content Notification content
     * @param channelId Notification channel ID
     * @param pendingIntent Action pending intent
     * @return Built notification
     */
    fun buildBasicNotification(
        content: NotificationContent,
        channelId: String,
        pendingIntent: PendingIntent
    ): android.app.Notification
    
    /**
     * Build notification with big text style
     * @param content Notification content
     * @param channelId Notification channel ID
     * @param pendingIntent Action pending intent
     * @return Built notification with expanded text
     */
    fun buildBigTextNotification(
        content: NotificationContent,
        channelId: String,
        pendingIntent: PendingIntent
    ): android.app.Notification
    
    /**
     * Build notification with big picture style
     * @param content Notification content
     * @param channelId Notification channel ID
     * @param pendingIntent Action pending intent
     * @param imageUrl Optional image URL for big picture
     * @return Built notification with image
     */
    fun buildBigPictureNotification(
        content: NotificationContent,
        channelId: String,
        pendingIntent: PendingIntent,
        imageUrl: String? = null
    ): android.app.Notification
    
    /**
     * Build notification with action buttons
     * @param content Notification content
     * @param channelId Notification channel ID
     * @param primaryAction Primary action pending intent
     * @param secondaryAction Optional secondary action
     * @return Built notification with actions
     */
    fun buildActionNotification(
        content: NotificationContent,
        channelId: String,
        primaryAction: PendingIntent,
        secondaryAction: NotificationAction? = null
    ): android.app.Notification
}

/**
 * Data class for notification actions
 */
data class NotificationAction(
    val icon: Int,
    val title: String,
    val pendingIntent: PendingIntent
)

/**
 * Interface for notification error handling
 */
interface NotificationErrorHandler {
    
    /**
     * Handle notification delivery error
     * @param error The error that occurred
     * @param content The notification content that failed
     */
    fun handleDeliveryError(error: Throwable, content: NotificationContent)
    
    /**
     * Handle permission denied error
     * @param content The notification content that was blocked
     */
    fun handlePermissionDenied(content: NotificationContent)
    
    /**
     * Handle channel creation error
     * @param channelId The channel that failed to create
     * @param error The error that occurred
     */
    fun handleChannelError(channelId: String, error: Throwable)
    
    /**
     * Log notification metrics
     * @param content Notification content
     * @param deliveryResult Whether delivery was successful
     */
    fun logNotificationMetrics(content: NotificationContent, deliveryResult: Boolean)
    
    /**
     * Log scheduling information
     * @param message Scheduling information message
     */
    fun logSchedulingInfo(message: String)
}

/**
 * Notification delivery result
 */
sealed class NotificationResult {
    object Success : NotificationResult()
    data class Failure(val error: String, val cause: Throwable? = null) : NotificationResult()
    object PermissionDenied : NotificationResult()
    object ChannelDisabled : NotificationResult()
    
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this !is Success
}