package com.shaadow.tunes.suggestion.integration

import android.content.Context
import androidx.media3.common.MediaItem
import com.shaadow.innertube.models.NavigationEndpoint
import com.shaadow.tunes.suggestion.SuggestionSystem
import com.shaadow.tunes.suggestion.data.EndReason
import com.shaadow.tunes.suggestion.engine.SuggestionEngine

/**
 * Complete PlayerService enhancement with minimal integration effort
 * Provides drop-in methods to enhance existing PlayerService functionality
 */
class PlayerServiceEnhancer(context: Context) {
    private val suggestionSystem = SuggestionSystem(context)
    private val radioEnhancer = RadioEnhancer(suggestionSystem.engine)
    
    // Expose the suggestion engine for EnhancedRadio
    internal val engine: SuggestionEngine = suggestionSystem.engine
    
    /**
     * Enhanced radio processing - drop-in replacement for existing radio.process()
     */
    suspend fun processEnhancedRadio(
        endpoint: NavigationEndpoint.Endpoint.Watch?,
        currentSong: MediaItem?
    ): List<MediaItem> {
        return radioEnhancer.enhanceRadio(
            endpoint = endpoint,
            currentSong = currentSong,
            strategy = MergeStrategy.INTERLEAVE_WEIGHTED
        )
    }
    
    /**
     * Create enhanced radio instance - drop-in replacement for YouTubeRadio
     */
    fun createEnhancedRadio(endpoint: NavigationEndpoint.Endpoint.Watch?): EnhancedRadio {
        return EnhancedRadio.fromEndpoint(endpoint, engine)
    }
    
    // Activity tracking methods (same as PlayerServiceIntegration)
    fun onSongStart(mediaItem: MediaItem) {
        suggestionSystem.activityTracker.onSongStart(mediaItem)
    }
    
    fun onSongEnd(reason: EndReason) {
        suggestionSystem.activityTracker.onSongEnd(reason)
    }
    
    fun onLikePressed(mediaItem: MediaItem) {
        suggestionSystem.activityTracker.onLikePressed(mediaItem)
    }
    
    fun onSkipPressed() {
        suggestionSystem.activityTracker.onSkipPressed()
    }
    
    // Utility methods
    fun isFirstLaunch(): Boolean = suggestionSystem.isFirstLaunch()
    
    fun setInitialPreferences(genres: Set<String>) {
        suggestionSystem.setInitialPreferences(genres)
    }
    
    fun cleanupOldData() {
        suggestionSystem.cleanupOldData()
    }
}