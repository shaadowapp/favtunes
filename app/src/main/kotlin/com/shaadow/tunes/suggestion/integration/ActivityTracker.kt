package com.shaadow.tunes.suggestion.integration

import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.data.*
import com.shaadow.tunes.suggestion.engine.SuggestionEngine
import com.shaadow.tunes.suggestion.engine.UserFeedback

class ActivityTracker(
    private val suggestionEngine: SuggestionEngine,
    private val storage: SuggestionStorage
) {
    private var playStartTime: Long = 0
    private var currentSong: MediaItem? = null
    
    fun onSongStart(mediaItem: MediaItem) {
        playStartTime = System.currentTimeMillis()
        currentSong = mediaItem
        storage.trackSongInteraction(mediaItem.mediaId, InteractionType.PLAY)
    }
    
    fun onSongEnd(reason: EndReason) {
        currentSong?.let { song ->
            val duration = System.currentTimeMillis() - playStartTime
            val interaction = when {
                reason == EndReason.COMPLETED -> InteractionType.COMPLETE
                duration < 30000 -> InteractionType.SKIP // Less than 30s
                else -> InteractionType.PLAY
            }
            
            storage.trackSongInteraction(song.mediaId, interaction, duration)
            
            // Update suggestion weights based on behavior
            suggestionEngine.updateWeights(
                UserFeedback(song, interaction, duration)
            )
        }
    }
    
    fun onLikePressed(mediaItem: MediaItem) {
        storage.trackSongInteraction(mediaItem.mediaId, InteractionType.LIKE)
        suggestionEngine.boostSimilarContent(mediaItem)
    }
    
    fun onDislikePressed(mediaItem: MediaItem) {
        storage.trackSongInteraction(mediaItem.mediaId, InteractionType.DISLIKE)
    }
    
    fun onSkipPressed() {
        currentSong?.let { song ->
            val duration = System.currentTimeMillis() - playStartTime
            storage.trackSongInteraction(song.mediaId, InteractionType.SKIP, duration)
            
            suggestionEngine.updateWeights(
                UserFeedback(song, InteractionType.SKIP, duration)
            )
        }
    }
}