package com.shaadow.tunes.suggestion.integration

import android.content.Context
import androidx.media3.common.MediaItem
import com.shaadow.innertube.models.NavigationEndpoint
import com.shaadow.tunes.suggestion.AdvancedSuggestionSystem
import com.shaadow.tunes.suggestion.analytics.SessionContext
import com.shaadow.tunes.suggestion.data.EndReason

/**
 * Advanced PlayerService enhancement with sophisticated analytics and learning
 * Drop-in replacement for PlayerServiceEnhancer with enhanced capabilities
 */
class AdvancedPlayerServiceEnhancer(context: Context) {
    private val advancedSystem = AdvancedSuggestionSystem(context)
    private val radioEnhancer = RadioEnhancer(advancedSystem.engine)
    
    /**
     * Start a listening session with context awareness
     */
    fun startSession(context: SessionContext) {
        advancedSystem.startListeningSession(context)
    }
    
    /**
     * End current session and analyze patterns
     */
    fun endSession() {
        advancedSystem.endListeningSession()
    }
    
    /**
     * Enhanced radio processing with advanced personalization
     */
    suspend fun processAdvancedRadio(
        endpoint: NavigationEndpoint.Endpoint.Watch?,
        currentSong: MediaItem?
    ): List<MediaItem> {
        // Start radio session if not already started
        if (advancedSystem.enhancedActivityTracker.getCurrentSessionStats() == null) {
            startSession(SessionContext.RADIO)
        }
        
        return radioEnhancer.enhanceRadio(
            endpoint = endpoint,
            currentSong = currentSong,
            strategy = MergeStrategy.INTERLEAVE_WEIGHTED
        )
    }
    
    /**
     * Get contextual recommendations based on current activity
     */
    suspend fun getContextualRecommendations(
        context: SessionContext,
        limit: Int = 20
    ): List<MediaItem> {
        return advancedSystem.getContextualRecommendations(context, limit)
    }
    
    /**
     * Create enhanced radio with advanced learning
     */
    fun createAdvancedRadio(endpoint: NavigationEndpoint.Endpoint.Watch?): EnhancedRadio {
        return EnhancedRadio.fromEndpoint(endpoint, advancedSystem.engine)
    }
    
    // Enhanced activity tracking
    fun onSongStart(mediaItem: MediaItem) {
        advancedSystem.enhancedActivityTracker.onSongStart(mediaItem)
    }
    
    fun onSongEnd(reason: EndReason) {
        advancedSystem.enhancedActivityTracker.onSongEnd(reason)
    }
    
    fun onLikePressed(mediaItem: MediaItem) {
        advancedSystem.enhancedActivityTracker.onLikePressed(mediaItem)
    }
    
    fun onSkipPressed() {
        advancedSystem.enhancedActivityTracker.onSkipPressed()
    }
    
    /**
     * Provide feedback on recommendation quality for learning
     */
    fun provideFeedback(
        recommendedItem: MediaItem,
        wasAccepted: Boolean,
        engagementScore: Float = 50f
    ) {
        advancedSystem.provideFeedback(recommendedItem, wasAccepted, engagementScore)
    }
    
    /**
     * Get comprehensive listening analytics
     */
    fun getListeningAnalytics() = advancedSystem.getListeningAnalytics()
    
    /**
     * Perform periodic learning update (call this occasionally)
     */
    suspend fun performLearningUpdate() {
        advancedSystem.performLearningUpdate()
    }
    
    /**
     * Check for preference changes
     */
    fun checkPreferenceChanges() = advancedSystem.detectPreferenceChanges()
    
    // Utility methods
    fun isFirstLaunch(): Boolean = advancedSystem.isFirstLaunch()
    
    fun setInitialPreferences(genres: Set<String>) {
        advancedSystem.setInitialPreferences(genres)
    }
    
    fun cleanupOldData() {
        advancedSystem.cleanupOldData()
    }
}