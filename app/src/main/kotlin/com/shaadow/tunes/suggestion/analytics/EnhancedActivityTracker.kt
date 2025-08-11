package com.shaadow.tunes.suggestion.analytics

import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.data.*
import com.shaadow.tunes.suggestion.engine.SuggestionEngine
import com.shaadow.tunes.suggestion.engine.UserFeedback
import java.util.UUID

/**
 * Enhanced activity tracker with advanced behavior analysis
 * Extends the basic ActivityTracker with session management and analytics
 */
class EnhancedActivityTracker(
    private val suggestionEngine: SuggestionEngine,
    private val storage: SuggestionStorage
) {
    private val behaviorAnalyzer = BehaviorAnalyzer()
    private var currentSession: ListeningSession? = null
    private val sessionHistory = mutableListOf<ListeningSession>()
    
    // Session management
    fun startSession(context: SessionContext = SessionContext.UNKNOWN) {
        endCurrentSession()
        currentSession = ListeningSession(
            sessionId = UUID.randomUUID().toString(),
            startTime = System.currentTimeMillis(),
            context = context
        )
    }
    
    fun endCurrentSession() {
        currentSession?.let { session ->
            session.endTime = System.currentTimeMillis()
            sessionHistory.add(session)
            
            // Analyze session and update user preferences
            analyzeAndUpdatePreferences(session)
            
            // Keep only recent sessions (last 50)
            if (sessionHistory.size > 50) {
                sessionHistory.removeAt(0)
            }
        }
        currentSession = null
    }
    
    // Enhanced track tracking
    fun onSongStart(mediaItem: MediaItem) {
        // Ensure we have an active session
        if (currentSession == null) {
            startSession()
        }
        
        // End previous track if exists
        currentSession?.getCurrentTrack()?.let { previousTrack ->
            onSongEnd(EndReason.USER_STOPPED)
        }
        
        // Start new track
        currentSession?.addTrack(mediaItem, System.currentTimeMillis())
        
        // Track basic interaction
        storage.trackSongInteraction(mediaItem.mediaId, InteractionType.PLAY)
    }
    
    fun onSongEnd(reason: EndReason) {
        currentSession?.getCurrentTrack()?.let { trackSession ->
            val currentTime = System.currentTimeMillis()
            trackSession.endTime = currentTime
            trackSession.playDuration = currentTime - trackSession.startTime
            
            // Determine interaction type based on reason and duration
            val interaction = when {
                reason == EndReason.COMPLETED -> InteractionType.COMPLETE
                trackSession.playDuration < 30000 -> InteractionType.SKIP
                else -> InteractionType.PLAY
            }
            
            // Mark as skipped if appropriate
            if (interaction == InteractionType.SKIP) {
                trackSession.wasSkipped = true
            }
            
            trackSession.addInteraction(interaction)
            
            // Update storage and suggestion engine
            storage.trackSongInteraction(
                trackSession.mediaItem.mediaId, 
                interaction, 
                trackSession.playDuration
            )
            
            suggestionEngine.updateWeights(
                UserFeedback(trackSession.mediaItem, interaction, trackSession.playDuration)
            )
            
            // Advanced learning based on engagement
            updateAdvancedLearning(trackSession)
        }
    }
    
    fun onLikePressed(mediaItem: MediaItem) {
        // Update current track session
        currentSession?.getCurrentTrack()?.let { trackSession ->
            if (trackSession.mediaItem.mediaId == mediaItem.mediaId) {
                trackSession.wasLiked = true
                trackSession.addInteraction(InteractionType.LIKE)
            }
        }
        
        // Update storage and boost similar content
        storage.trackSongInteraction(mediaItem.mediaId, InteractionType.LIKE)
        suggestionEngine.boostSimilarContent(mediaItem)
        
        // Advanced preference learning
        updatePreferencesFromLike(mediaItem)
    }
    
    fun onSkipPressed() {
        currentSession?.getCurrentTrack()?.let { trackSession ->
            trackSession.wasSkipped = true
            trackSession.addInteraction(InteractionType.SKIP)
            
            val duration = System.currentTimeMillis() - trackSession.startTime
            storage.trackSongInteraction(trackSession.mediaItem.mediaId, InteractionType.SKIP, duration)
            
            suggestionEngine.updateWeights(
                UserFeedback(trackSession.mediaItem, InteractionType.SKIP, duration)
            )
            
            // Learn from skip patterns
            updatePreferencesFromSkip(trackSession)
        }
    }
    
    // Analytics and insights
    fun getListeningPatterns(): ListeningPatterns {
        return behaviorAnalyzer.analyzeListeningPatterns(sessionHistory)
    }
    
    fun getCurrentSessionStats(): SessionStats? {
        return currentSession?.let { session ->
            SessionStats(
                duration = session.getDuration(),
                tracksPlayed = session.tracks.size,
                completionRate = session.getCompletionRate(),
                skipRate = session.getSkipRate()
            )
        }
    }
    
    fun predictSatisfaction(mediaItem: MediaItem): Float {
        val userProfile = storage.getUserProfile()
        val patterns = getListeningPatterns()
        return behaviorAnalyzer.predictSatisfaction(mediaItem, userProfile, patterns)
    }
    
    // Private helper methods
    private fun analyzeAndUpdatePreferences(session: ListeningSession) {
        val patterns = behaviorAnalyzer.analyzeListeningPatterns(listOf(session))
        
        // Update genre preferences based on session
        patterns.genreAffinities.forEach { (genre, affinity) ->
            if (affinity > 0.5f) {
                storage.updateGenrePreference(genre, affinity * 0.1f)
            }
        }
    }
    
    private fun updateAdvancedLearning(trackSession: TrackSession) {
        val engagementScore = behaviorAnalyzer.calculateEngagementScore(trackSession)
        
        // High engagement - boost similar content
        if (engagementScore > 70f) {
            suggestionEngine.boostSimilarContent(trackSession.mediaItem)
        }
        
        // Low engagement - learn what to avoid
        if (engagementScore < 30f) {
            // Could implement negative learning here
            // For now, just track as a skip pattern
        }
    }
    
    private fun updatePreferencesFromLike(mediaItem: MediaItem) {
        // Extract and boost genre preference
        mediaItem.mediaMetadata.genre?.toString()?.let { genre ->
            storage.updateGenrePreference(genre, 0.15f)
        }
        
        // Could also boost artist preference, mood, etc.
    }
    
    private fun updatePreferencesFromSkip(trackSession: TrackSession) {
        // If skipped very quickly, reduce genre preference slightly
        if (trackSession.playDuration < 15000) { // Less than 15 seconds
            trackSession.mediaItem.mediaMetadata.genre?.toString()?.let { genre ->
                storage.updateGenrePreference(genre, -0.05f)
            }
        }
    }
}

/**
 * Current session statistics
 */
data class SessionStats(
    val duration: Long,
    val tracksPlayed: Int,
    val completionRate: Float,
    val skipRate: Float
)