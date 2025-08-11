package com.shaadow.tunes.suggestion.integration

import androidx.media3.common.MediaItem
import com.shaadow.innertube.models.NavigationEndpoint
import com.shaadow.tunes.suggestion.engine.SuggestionEngine
import com.shaadow.tunes.utils.YouTubeRadio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enhanced radio that combines YouTube radio with intelligent suggestions
 * Drop-in replacement for YouTubeRadio with backward compatibility
 */
class EnhancedRadio(
    private val videoId: String? = null,
    private val playlistId: String? = null,
    private val playlistSetVideoId: String? = null,
    private val parameters: String? = null,
    private val suggestionEngine: SuggestionEngine? = null
) {
    
    private val youtubeRadio = YouTubeRadio(videoId, playlistId, playlistSetVideoId, parameters)
    private val radioEnhancer = suggestionEngine?.let { RadioEnhancer(it) }
    
    /**
     * Process radio with intelligent enhancement
     * Falls back to pure YouTube radio if suggestion engine is not available
     */
    suspend fun process(): List<MediaItem> = withContext(Dispatchers.IO) {
        if (radioEnhancer != null && suggestionEngine != null) {
            // Enhanced mode: merge YouTube + local suggestions
            processEnhanced()
        } else {
            // Fallback mode: pure YouTube radio
            youtubeRadio.process()
        }
    }
    
    /**
     * Enhanced processing with intelligent merging
     */
    private suspend fun processEnhanced(): List<MediaItem> {
        val youtubeSongs = youtubeRadio.process()
        
        if (youtubeSongs.isEmpty()) {
            // If YouTube fails, provide local suggestions as fallback
            return suggestionEngine?.getRecommendations(
                currentSong = null,
                context = com.shaadow.tunes.suggestion.engine.SuggestionContext.RADIO,
                limit = 10
            ) ?: emptyList()
        }
        
        // Get current song context (first song from YouTube radio)
        val currentSong = youtubeSongs.firstOrNull()
        
        // Get local suggestions based on current context
        val localSuggestions = suggestionEngine?.getRecommendations(
            currentSong = currentSong,
            context = com.shaadow.tunes.suggestion.engine.SuggestionContext.RADIO,
            limit = 5
        ) ?: emptyList()
        
        // Merge with light local enhancement (mostly YouTube)
        return SmartMerger.merge(
            local = localSuggestions,
            youtube = youtubeSongs,
            strategy = MergeStrategy.YOUTUBE_FIRST
        )
    }
    
    companion object {
        /**
         * Factory method to create enhanced radio from endpoint
         */
        fun fromEndpoint(
            endpoint: NavigationEndpoint.Endpoint.Watch?,
            suggestionEngine: SuggestionEngine? = null
        ): EnhancedRadio {
            return EnhancedRadio(
                videoId = endpoint?.videoId,
                playlistId = endpoint?.playlistId,
                playlistSetVideoId = endpoint?.playlistSetVideoId,
                parameters = endpoint?.params,
                suggestionEngine = suggestionEngine
            )
        }
    }
}