package com.shaadow.tunes.suggestion.integration

import androidx.media3.common.MediaItem
import com.shaadow.innertube.models.NavigationEndpoint
import com.shaadow.tunes.suggestion.engine.SuggestionContext
import com.shaadow.tunes.suggestion.engine.SuggestionEngine
import com.shaadow.tunes.utils.YouTubeRadio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enhances YouTube radio with personalized local suggestions
 * Merges YouTube's recommendations with user-specific preferences
 */
class RadioEnhancer(private val suggestionEngine: SuggestionEngine) {
    
    /**
     * Enhances radio with intelligent merging of YouTube and local suggestions
     */
    suspend fun enhanceRadio(
        endpoint: NavigationEndpoint.Endpoint.Watch?,
        currentSong: MediaItem?,
        strategy: MergeStrategy = MergeStrategy.INTERLEAVE_WEIGHTED
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        
        // Get YouTube radio suggestions (existing system)
        val youtubeSuggestions = getYouTubeRadio(endpoint)
        
        // Get personalized local suggestions
        val localSuggestions = suggestionEngine.getRecommendations(
            currentSong, 
            SuggestionContext.RADIO,
            limit = 15
        )
        
        // Merge intelligently
        SmartMerger.merge(
            local = localSuggestions,
            youtube = youtubeSuggestions,
            strategy = strategy
        )
    }
    
    /**
     * Get YouTube radio suggestions using existing system
     */
    private suspend fun getYouTubeRadio(endpoint: NavigationEndpoint.Endpoint.Watch?): List<MediaItem> {
        return try {
            val radio = YouTubeRadio(
                videoId = endpoint?.videoId,
                playlistId = endpoint?.playlistId,
                playlistSetVideoId = endpoint?.playlistSetVideoId,
                parameters = endpoint?.params
            )
            radio.process()
        } catch (e: Exception) {
            // Fallback to empty list if YouTube radio fails
            emptyList()
        }
    }
    
    /**
     * Enhanced radio processing that continues to fetch more songs
     * Maintains compatibility with existing radio system
     */
    suspend fun processEnhancedRadio(
        radio: YouTubeRadio?,
        currentSong: MediaItem?
    ): List<MediaItem> {
        val youtubeSongs = radio?.process() ?: emptyList()
        
        if (youtubeSongs.isEmpty()) {
            // Fallback to local suggestions if YouTube fails
            return suggestionEngine.getRecommendations(
                currentSong,
                SuggestionContext.RADIO,
                limit = 10
            )
        }
        
        // Get a few local suggestions to mix in
        val localSuggestions = suggestionEngine.getRecommendations(
            currentSong,
            SuggestionContext.RADIO,
            limit = 3
        )
        
        // Light mixing - mostly YouTube with some local flavor
        return SmartMerger.merge(
            local = localSuggestions,
            youtube = youtubeSongs,
            strategy = MergeStrategy.YOUTUBE_FIRST
        )
    }
}

/**
 * Smart merging strategies for combining local and YouTube suggestions
 */
object SmartMerger {
    
    fun merge(
        local: List<MediaItem>,
        youtube: List<MediaItem>,
        strategy: MergeStrategy
    ): List<MediaItem> {
        return when (strategy) {
            MergeStrategy.INTERLEAVE_WEIGHTED -> {
                // 70% YouTube (quality), 30% local (personalization)
                interleaveWeighted(youtube, local, youtubeRatio = 0.7f)
            }
            MergeStrategy.LOCAL_FIRST -> {
                // Local suggestions first, then YouTube (avoiding duplicates)
                local + youtube.filterNot { ytSong -> 
                    local.any { localSong -> localSong.mediaId == ytSong.mediaId }
                }
            }
            MergeStrategy.YOUTUBE_FIRST -> {
                // YouTube first, then local (avoiding duplicates)
                youtube + local.filterNot { localSong ->
                    youtube.any { ytSong -> ytSong.mediaId == localSong.mediaId }
                }
            }
        }
    }
    
    /**
     * Intelligently interleaves two lists based on a ratio
     * Ensures good distribution while maintaining quality
     */
    private fun interleaveWeighted(
        primary: List<MediaItem>,
        secondary: List<MediaItem>,
        youtubeRatio: Float
    ): List<MediaItem> {
        val result = mutableListOf<MediaItem>()
        val maxSize = maxOf(primary.size, secondary.size)
        
        var primaryIndex = 0
        var secondaryIndex = 0
        
        for (i in 0 until maxSize * 2) {
            val shouldUsePrimary = (i.toFloat() / (maxSize * 2)) < youtubeRatio
            
            if (shouldUsePrimary && primaryIndex < primary.size) {
                val item = primary[primaryIndex++]
                if (!result.any { it.mediaId == item.mediaId }) {
                    result.add(item)
                }
            } else if (secondaryIndex < secondary.size) {
                val item = secondary[secondaryIndex++]
                if (!result.any { it.mediaId == item.mediaId }) {
                    result.add(item)
                }
            }
            
            // Break if we've used all items from both lists
            if (primaryIndex >= primary.size && secondaryIndex >= secondary.size) {
                break
            }
        }
        
        return result
    }
}

enum class MergeStrategy { 
    INTERLEAVE_WEIGHTED, 
    LOCAL_FIRST, 
    YOUTUBE_FIRST 
}