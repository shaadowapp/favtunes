package com.shaadow.tunes.suggestion.integration

import android.content.Context
import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.SuggestionSystem
import com.shaadow.tunes.suggestion.data.EndReason
import com.shaadow.tunes.suggestion.engine.SuggestionContext

/**
 * Integration helper for PlayerService
 * Shows how to integrate the suggestion system with existing player code
 */
class PlayerServiceIntegration(context: Context) {
    private val suggestionSystem = SuggestionSystem(context)
    
    /**
     * Call this when a song starts playing
     */
    fun onSongStart(mediaItem: MediaItem) {
        suggestionSystem.activityTracker.onSongStart(mediaItem)
    }
    
    /**
     * Call this when a song ends (completed, skipped, etc.)
     */
    fun onSongEnd(reason: EndReason) {
        suggestionSystem.activityTracker.onSongEnd(reason)
    }
    
    /**
     * Call this when user likes a song
     */
    fun onLikePressed(mediaItem: MediaItem) {
        suggestionSystem.activityTracker.onLikePressed(mediaItem)
    }
    
    /**
     * Call this when user skips a song
     */
    fun onSkipPressed() {
        suggestionSystem.activityTracker.onSkipPressed()
    }
    
    /**
     * Get personalized recommendations for radio enhancement
     */
    suspend fun getRadioSuggestions(currentSong: MediaItem?): List<MediaItem> {
        return suggestionSystem.getRecommendations(
            currentSong = currentSong,
            context = SuggestionContext.RADIO,
            limit = 10
        )
    }
    
    /**
     * Get general recommendations for home screen
     */
    suspend fun getHomeSuggestions(): List<MediaItem> {
        return suggestionSystem.getRecommendations(
            context = SuggestionContext.DISCOVERY,
            limit = 20
        )
    }
    
    /**
     * Check if this is the first app launch (for onboarding)
     */
    fun isFirstLaunch(): Boolean = suggestionSystem.isFirstLaunch()
    
    /**
     * Set initial user preferences (called after onboarding)
     */
    fun setInitialPreferences(genres: Set<String>) {
        suggestionSystem.setInitialPreferences(genres)
    }
}