package com.shaadow.tunes.suggestion.learning

import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.analytics.BehaviorAnalyzer
import com.shaadow.tunes.suggestion.analytics.ListeningPatterns
import com.shaadow.tunes.suggestion.data.SuggestionStorage
import com.shaadow.tunes.suggestion.data.UserProfile
import com.shaadow.tunes.suggestion.engine.SuggestionWeights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Continuous learning system that improves recommendations over time
 */
class LearningSystem(
    private val storage: SuggestionStorage,
    private val behaviorAnalyzer: BehaviorAnalyzer
) {
    
    /**
     * Perform periodic learning updates based on user behavior
     */
    suspend fun performLearningUpdate() = withContext(Dispatchers.IO) {
        val userProfile = storage.getUserProfile()
        
        // Skip if not enough data
        if (userProfile.playHistory.size < 10) return@withContext
        
        // Analyze recent behavior patterns
        val recentActivity = storage.getRecentActivity(days = 7)
        if (recentActivity.isEmpty()) return@withContext
        
        // Update recommendation weights based on patterns
        updateRecommendationWeights(userProfile)
        
        // Clean up old data
        storage.cleanOldData()
    }
    
    /**
     * Learn from recommendation feedback
     */
    fun learnFromRecommendation(
        recommendedItem: MediaItem,
        wasAccepted: Boolean,
        engagementScore: Float
    ) {
        if (wasAccepted && engagementScore > 70f) {
            // Successful recommendation - reinforce similar patterns
            reinforceSuccessfulRecommendation(recommendedItem)
        } else if (!wasAccepted || engagementScore < 30f) {
            // Poor recommendation - learn to avoid similar patterns
            learnFromPoorRecommendation(recommendedItem)
        }
    }
    
    /**
     * Adaptive weight adjustment based on user behavior
     */
    private fun updateRecommendationWeights(userProfile: UserProfile) {
        val recentPlays = userProfile.playHistory.takeLast(50)
        val recentSkips = userProfile.skipHistory.filterValues { 
            System.currentTimeMillis() - it < 7 * 24 * 60 * 60 * 1000L // Last 7 days
        }
        
        // Calculate user's exploration vs exploitation preference
        val explorationRate = calculateExplorationRate(userProfile)
        
        // Adjust weights based on user behavior patterns
        val weights = SuggestionWeights()
        
        when {
            explorationRate > 0.7f -> {
                // User likes discovering new music
                weights.similarity = 0.2f
                weights.popularity = 0.5f
                weights.recency = 0.3f
            }
            explorationRate < 0.3f -> {
                // User prefers familiar music
                weights.similarity = 0.6f
                weights.popularity = 0.2f
                weights.recency = 0.2f
            }
            else -> {
                // Balanced approach
                weights.similarity = 0.4f
                weights.popularity = 0.3f
                weights.recency = 0.3f
            }
        }
        
        // Store updated weights (would need to add this to storage)
        // storage.updateWeights(weights)
    }
    
    /**
     * Calculate how much the user likes exploring new music vs familiar music
     */
    private fun calculateExplorationRate(userProfile: UserProfile): Float {
        val recentPlays = userProfile.playHistory.takeLast(100)
        if (recentPlays.size < 10) return 0.5f // Default balanced
        
        // Count unique artists in recent plays
        val uniqueArtists = mutableSetOf<String>()
        val totalPlays = recentPlays.size
        
        recentPlays.forEach { playEvent ->
            // In a real implementation, you'd extract artist from songId
            // For now, use songId as proxy
            uniqueArtists.add(playEvent.songId.take(10)) // Simplified
        }
        
        // High ratio = more exploration, low ratio = more repetition
        return (uniqueArtists.size.toFloat() / totalPlays).coerceIn(0f, 1f)
    }
    
    /**
     * Reinforce patterns that led to successful recommendations
     */
    private fun reinforceSuccessfulRecommendation(mediaItem: MediaItem) {
        // Boost genre preference
        mediaItem.mediaMetadata.genre?.toString()?.let { genre ->
            storage.updateGenrePreference(genre, 0.1f)
        }
        
        // Could also boost artist, mood, tempo, etc.
    }
    
    /**
     * Learn to avoid patterns that led to poor recommendations
     */
    private fun learnFromPoorRecommendation(mediaItem: MediaItem) {
        // Slightly reduce genre preference
        mediaItem.mediaMetadata.genre?.toString()?.let { genre ->
            storage.updateGenrePreference(genre, -0.05f)
        }
        
        // Could implement more sophisticated negative learning
    }
    
    /**
     * Detect and adapt to changes in user preferences
     */
    fun detectPreferenceShifts(userProfile: UserProfile): PreferenceShift? {
        val recentActivity = userProfile.playHistory.takeLast(50)
        val olderActivity = userProfile.playHistory.dropLast(50).takeLast(50)
        
        if (recentActivity.size < 20 || olderActivity.size < 20) {
            return null // Not enough data
        }
        
        // Compare genre distributions
        val recentGenres = extractGenreDistribution(recentActivity)
        val olderGenres = extractGenreDistribution(olderActivity)
        
        // Calculate shift magnitude
        val shiftMagnitude = calculateDistributionShift(recentGenres, olderGenres)
        
        return if (shiftMagnitude > 0.3f) {
            PreferenceShift(
                magnitude = shiftMagnitude,
                newPreferences = recentGenres,
                oldPreferences = olderGenres
            )
        } else null
    }
    
    private fun extractGenreDistribution(playEvents: List<com.shaadow.tunes.suggestion.data.PlayEvent>): Map<String, Float> {
        // Simplified - in real implementation, extract actual genres
        val genreCounts = mutableMapOf<String, Int>()
        
        playEvents.forEach { event ->
            // Placeholder genre extraction
            val genre = "genre_${event.songId.hashCode() % 5}" // Simplified
            genreCounts[genre] = (genreCounts[genre] ?: 0) + 1
        }
        
        val total = genreCounts.values.sum().toFloat()
        return genreCounts.mapValues { it.value / total }
    }
    
    private fun calculateDistributionShift(
        recent: Map<String, Float>,
        older: Map<String, Float>
    ): Float {
        val allGenres = (recent.keys + older.keys).toSet()
        var totalShift = 0f
        
        allGenres.forEach { genre ->
            val recentWeight = recent[genre] ?: 0f
            val olderWeight = older[genre] ?: 0f
            totalShift += kotlin.math.abs(recentWeight - olderWeight)
        }
        
        return totalShift / 2f // Normalize
    }
}

/**
 * Represents a detected shift in user preferences
 */
data class PreferenceShift(
    val magnitude: Float,
    val newPreferences: Map<String, Float>,
    val oldPreferences: Map<String, Float>
)