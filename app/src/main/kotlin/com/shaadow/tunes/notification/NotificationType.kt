package com.shaadow.tunes.notification

/**
 * Enum representing different types of notifications
 * Used for preference management and scheduling
 */
enum class NotificationType {
    ENGAGEMENT,           // Re-engagement notifications
    MUSIC_SUGGESTIONS,    // Music recommendation notifications
    MARKETING,           // Marketing and promotional notifications
    TRENDING,            // Trending music notifications
    PERSONALIZED_SUGGESTIONS, // AI-powered personalized suggestions
    RATING_REQUESTS,     // App rating request notifications
    GENERAL             // General purpose notifications
}