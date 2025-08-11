package com.shaadow.tunes.suggestion.analytics

import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.data.InteractionType

/**
 * Represents a complete listening session with detailed analytics
 */
data class ListeningSession(
    val sessionId: String,
    val startTime: Long,
    var endTime: Long? = null,
    val tracks: MutableList<TrackSession> = mutableListOf(),
    var context: SessionContext = SessionContext.UNKNOWN
) {
    fun addTrack(mediaItem: MediaItem, startTime: Long) {
        tracks.add(TrackSession(mediaItem, startTime))
    }
    
    fun getCurrentTrack(): TrackSession? = tracks.lastOrNull()
    
    fun getDuration(): Long = (endTime ?: System.currentTimeMillis()) - startTime
    
    fun getCompletionRate(): Float {
        if (tracks.isEmpty()) return 0f
        val completedTracks = tracks.count { it.completionRate >= 0.8f }
        return completedTracks.toFloat() / tracks.size
    }
    
    fun getSkipRate(): Float {
        if (tracks.isEmpty()) return 0f
        val skippedTracks = tracks.count { it.wasSkipped }
        return skippedTracks.toFloat() / tracks.size
    }
}

/**
 * Detailed tracking for individual track within a session
 */
data class TrackSession(
    val mediaItem: MediaItem,
    val startTime: Long,
    var endTime: Long? = null,
    var playDuration: Long = 0,
    var interactions: MutableList<TrackInteraction> = mutableListOf(),
    var wasSkipped: Boolean = false,
    var wasLiked: Boolean = false
) {
    val completionRate: Float
        get() {
            val totalDuration = mediaItem.mediaMetadata.durationMs ?: 0
            return if (totalDuration > 0) {
                (playDuration.toFloat() / totalDuration).coerceIn(0f, 1f)
            } else 0f
        }
    
    fun addInteraction(type: InteractionType, timestamp: Long = System.currentTimeMillis()) {
        interactions.add(TrackInteraction(type, timestamp))
        
        when (type) {
            InteractionType.SKIP -> wasSkipped = true
            InteractionType.LIKE -> wasLiked = true
            else -> {}
        }
    }
}

/**
 * Individual interaction within a track session
 */
data class TrackInteraction(
    val type: InteractionType,
    val timestamp: Long
)

/**
 * Context in which the listening session occurred
 */
enum class SessionContext {
    RADIO,           // YouTube radio or enhanced radio
    PLAYLIST,        // User playlist
    ALBUM,           // Album playback
    SEARCH,          // From search results
    DISCOVERY,       // Discovery/recommendation context
    SHUFFLE,         // Shuffle mode
    UNKNOWN
}