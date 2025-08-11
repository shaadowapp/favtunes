package com.shaadow.tunes.suggestion

import android.content.Context
import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.analytics.*
import com.shaadow.tunes.suggestion.data.SuggestionStorage
import com.shaadow.tunes.suggestion.engine.SuggestionContext
import com.shaadow.tunes.suggestion.engine.SuggestionEngine
import com.shaadow.tunes.suggestion.learning.LearningSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Advanced suggestion system with enhanced analytics and learning
 * Provides more sophisticated personalization and behavior analysis
 */
class AdvancedSuggestionSystem(context: Context) {
    private val storage = SuggestionStorage(context)
    internal val engine = SuggestionEngine(context)
    private val behaviorAnalyzer = BehaviorAnalyzer()
    private val learningSystem = LearningSystem(storage, behaviorAnalyzer)
    
    // Enhanced activity tracker with analytics
    val enhancedActivityTracker = EnhancedActivityTracker(engine, storage)
    
    /**
     * Get recommendations with advanced personalization
     */
    suspend fun getAdvancedRecommendations(
        currentSong: MediaItem? = null,
        context: SuggestionContext = SuggestionContext.GENERAL,
        limit: Int = 20
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        
        // Get base recommendations
        val baseRecommendations = engine.getRecommendations(currentSong, context, limit * 2)
        
        // Apply advanced filtering and ranking
        val rankedRecommendations = baseRecommendations.map { mediaItem ->
            val satisfactionScore = enhancedActivityTracker.predictSatisfaction(mediaItem)
            ScoredRecommendation(mediaItem, satisfactionScore)
        }
        
        // Sort by predicted satisfaction and return top results
        rankedRecommendations
            .sortedByDescending { it.score }
            .take(limit)
            .map { it.mediaItem }
    }
    
    /**
     * Get contextual recommendations based on current listening session
     */
    suspend fun getContextualRecommendations(
        sessionContext: SessionContext,
        limit: Int = 20
    ): List<MediaItem> {
        val suggestionContext = when (sessionContext) {
            SessionContext.RADIO -> SuggestionContext.RADIO
            SessionContext.DISCOVERY -> SuggestionContext.DISCOVERY
            else -> SuggestionContext.GENERAL
        }
        
        return getAdvancedRecommendations(
            context = suggestionContext,
            limit = limit
        )
    }
    
    /**
     * Start a new listening session with context
     */
    fun startListeningSession(context: SessionContext = SessionContext.UNKNOWN) {
        enhancedActivityTracker.startSession(context)
    }
    
    /**
     * End current listening session and analyze patterns
     */
    fun endListeningSession() {
        enhancedActivityTracker.endCurrentSession()
    }
    
    /**
     * Get comprehensive listening analytics
     */
    fun getListeningAnalytics(): ListeningAnalytics {
        val patterns = enhancedActivityTracker.getListeningPatterns()
        val currentStats = enhancedActivityTracker.getCurrentSessionStats()
        val userProfile = storage.getUserProfile()
        
        return ListeningAnalytics(
            patterns = patterns,
            currentSession = currentStats,
            totalSongsPlayed = userProfile.playHistory.size,
            totalLikedSongs = userProfile.likedSongs.size,
            preferredGenres = userProfile.preferredGenres.entries
                .sortedByDescending { it.value }
                .take(5)
                .associate { it.key to it.value }
        )
    }
    
    /**
     * Perform learning update (should be called periodically)
     */
    suspend fun performLearningUpdate() {
        learningSystem.performLearningUpdate()
    }
    
    /**
     * Provide feedback on recommendation quality
     */
    fun provideFeedback(
        recommendedItem: MediaItem,
        wasAccepted: Boolean,
        engagementScore: Float
    ) {
        learningSystem.learnFromRecommendation(recommendedItem, wasAccepted, engagementScore)
    }
    
    /**
     * Detect if user preferences have shifted
     */
    fun detectPreferenceChanges(): com.shaadow.tunes.suggestion.learning.PreferenceShift? {
        val userProfile = storage.getUserProfile()
        return learningSystem.detectPreferenceShifts(userProfile)
    }
    
    // Backward compatibility methods
    fun isFirstLaunch(): Boolean = storage.isFirstLaunch()
    
    fun setInitialPreferences(genres: Set<String>) {
        engine.setInitialPreferences(genres)
    }
    
    fun cleanupOldData() {
        storage.cleanOldData()
    }
}

/**
 * Recommendation with satisfaction score
 */
private data class ScoredRecommendation(
    val mediaItem: MediaItem,
    val score: Float
)

/**
 * Comprehensive listening analytics
 */
data class ListeningAnalytics(
    val patterns: ListeningPatterns,
    val currentSession: SessionStats?,
    val totalSongsPlayed: Int,
    val totalLikedSongs: Int,
    val preferredGenres: Map<String, Float>
)