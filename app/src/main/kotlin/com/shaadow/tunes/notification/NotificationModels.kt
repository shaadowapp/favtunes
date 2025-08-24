package com.shaadow.tunes.notification

/**
 * Data class representing notification content
 * Enforces content length constraints and provides structured notification data
 */
data class NotificationContent(
    val title: String,           // Max 15 words as per requirements
    val body: String,            // Max 25 words as per requirements
    val emoji: String,           // Contextual emoji
    val actionText: String,      // CTA button text
    val songId: String? = null,  // Optional song reference
    val contentType: String,     // Type of notification content
    val personalityTone: PersonalityTone = PersonalityTone.ENCOURAGING_WITTY,
    val imageUrl: String? = null // Optional image URL for rich notifications
) {
    init {
        // Validate content length constraints
        require(title.split(" ").size <= 15) { 
            "Title must be 15 words or less. Current: ${title.split(" ").size} words" 
        }
        require(body.split(" ").size <= 25) { 
            "Body must be 25 words or less. Current: ${body.split(" ").size} words" 
        }
    }
    
    /**
     * Get formatted title with emoji
     */
    val formattedTitle: String
        get() = "$emoji $title"
    
    /**
     * Check if this is a music-related notification
     */
    val isMusicNotification: Boolean
        get() = songId != null || contentType.contains("music", ignoreCase = true)
}

/**
 * Enum representing different personality tones for notifications
 * Used to create engaging, humorous, and savage content
 */
enum class PersonalityTone {
    SAVAGE_FRIENDLY,    // "Your music taste is... interesting 🤔"
    HUMOROUS_ROAST,     // "Even your playlist is judging your choices"
    ENCOURAGING_WITTY,  // "Time to redeem your music reputation"
    PLAYFUL_SARCASTIC   // "Your ears called. They want better music."
}

/**
 * Data class for user context analysis
 * Used to generate personalized and contextually appropriate notifications
 */
data class UserContext(
    val hoursSinceLastOpen: Long,
    val favoriteGenres: List<String>,
    val listeningStreak: Int,
    val skipRate: Double,
    val timeOfDay: TimeOfDay,
    val musicMood: MusicMood,
    val totalListeningTime: Long = 0L,
    val recentSongs: List<String> = emptyList(),
    val isActiveUser: Boolean = hoursSinceLastOpen < 24
)

/**
 * Enum for time of day context
 */
enum class TimeOfDay {
    MORNING,    // 6 AM - 12 PM
    AFTERNOON,  // 12 PM - 6 PM
    EVENING,    // 6 PM - 10 PM
    NIGHT       // 10 PM - 6 AM
}

/**
 * Enum for music mood context
 */
enum class MusicMood {
    ENERGETIC,
    RELAXED,
    FOCUSED,
    PARTY,
    MELANCHOLIC,
    UNKNOWN
}

/**
 * Data class for music profile analysis
 */
data class MusicProfile(
    val topGenres: List<String>,
    val listeningHours: Double,
    val skipBehavior: SkipBehavior,
    val discoveryRate: Double,
    val moodPatterns: Map<TimeOfDay, List<String>>,
    val socialSharing: Boolean
)

/**
 * Data class for skip behavior analysis
 */
data class SkipBehavior(
    val averageSkipTime: Double,  // Average time before skipping in seconds
    val skipRate: Double,         // Percentage of songs skipped
    val genreSkipRates: Map<String, Double> // Skip rates by genre
)