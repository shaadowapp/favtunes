package com.shaadow.tunes.suggestion

import android.content.Context
import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.engine.SuggestionContext
import com.shaadow.tunes.suggestion.data.SuggestionStorage
import com.shaadow.tunes.suggestion.engine.SuggestionEngine
import com.shaadow.tunes.suggestion.integration.ActivityTracker

/**
 * Main entry point for the suggestion system
 * Provides a simple interface for the rest of the app
 */
class SuggestionSystem(context: Context) {
    private val storage = SuggestionStorage(context)
    internal val engine = SuggestionEngine(context)
    val activityTracker = ActivityTracker(engine, storage)
    
    suspend fun getRecommendations(
        currentSong: MediaItem? = null,
        context: SuggestionContext = SuggestionContext.GENERAL,
        limit: Int = 20
    ): List<MediaItem> {
        return engine.getRecommendations(currentSong, context, limit)
    }
    
    fun isFirstLaunch(): Boolean = storage.isFirstLaunch()
    
    fun setInitialPreferences(genres: Set<String>) {
        engine.setInitialPreferences(genres)
    }
    
    fun cleanupOldData() {
        storage.cleanOldData()
    }
}