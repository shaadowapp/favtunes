package com.shaadow.tunes.suggestion.analytics

import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.data.InteractionType
import com.shaadow.tunes.suggestion.data.UserProfile
import kotlin.math.exp

/**
 * Advanced behavior analysis for personalized recommendations
 */
class BehaviorAnalyzer {
    
    /**
     * Analyze listening patterns to extract user preferences
     */
    fun analyzeListeningPatterns(sessions: List<ListeningSession>): ListeningPatterns {
        if (sessions.isEmpty()) return ListeningPatterns()
        
        val totalTracks = sessions.flatMap { it.tracks }
        
        return ListeningPatterns(
            averageSessionDuration = sessions.map { it.getDuration() }.average().toLong(),
            averageTracksPerSession = sessions.map { it.tracks.size }.average(),
            preferredSessionLength = calculatePreferredSessionLength(sessions),
            skipPatterns = analyzeSkipPatterns(totalTracks),
            timeOfDayPreferences = analyzeTimePreferences(sessions),
            genreAffinities = analyzeGenreAffinities(totalTracks),
            artistLoyalty = analyzeArtistLoyalty(totalTracks)
        )
    }
    
    /**
     * Calculate user engagement score for a track
     */
    fun calculateEngagementScore(trackSession: TrackSession): Float {
        var score = 0f
        
        // Completion rate (0-40 points)
        score += trackSession.completionRate * 40f
        
        // Like interaction (+30 points)
        if (trackSession.wasLiked) score += 30f
        
        // Skip penalty (-20 points if skipped quickly)
        if (trackSession.wasSkipped && trackSession.completionRate < 0.3f) {
            score -= 20f
        }
        
        // Replay bonus (if played multiple times in session)
        val playCount = trackSession.interactions.count { it.type == InteractionType.PLAY }
        if (playCount > 1) score += 10f
        
        return score.coerceIn(0f, 100f)
    }
    
    /**
     * Predict user satisfaction with a potential recommendation
     */
    fun predictSatisfaction(
        mediaItem: MediaItem,
        userProfile: UserProfile,
        patterns: ListeningPatterns
    ): Float {
        var satisfaction = 0.5f // Base satisfaction
        
        // Genre matching
        val genre = extractGenre(mediaItem)
        genre?.let { g ->
            val genreAffinity = patterns.genreAffinities[g] ?: 0f
            satisfaction += genreAffinity * 0.3f
        }
        
        // Artist familiarity
        val artist = extractArtist(mediaItem)
        artist?.let { a ->
            val artistLoyalty = patterns.artistLoyalty[a] ?: 0f
            satisfaction += artistLoyalty * 0.2f
        }
        
        // Recent skip history (negative impact)
        if (userProfile.skipHistory.containsKey(mediaItem.mediaId)) {
            satisfaction -= 0.4f
        }
        
        // Like history (positive impact)
        if (userProfile.likedSongs.containsKey(mediaItem.mediaId)) {
            satisfaction += 0.5f
        }
        
        return satisfaction.coerceIn(0f, 1f)
    }
    
    private fun calculatePreferredSessionLength(sessions: List<ListeningSession>): SessionLength {
        val durations = sessions.map { it.getDuration() }
        val averageDuration = durations.average()
        
        return when {
            averageDuration < 10 * 60 * 1000 -> SessionLength.SHORT // < 10 minutes
            averageDuration < 30 * 60 * 1000 -> SessionLength.MEDIUM // < 30 minutes
            else -> SessionLength.LONG // 30+ minutes
        }
    }
    
    private fun analyzeSkipPatterns(tracks: List<TrackSession>): SkipPatterns {
        val skippedTracks = tracks.filter { it.wasSkipped }
        val totalTracks = tracks.size
        
        if (skippedTracks.isEmpty()) return SkipPatterns()
        
        val averageSkipTime = skippedTracks.map { it.playDuration }.average()
        val skipRate = skippedTracks.size.toFloat() / totalTracks
        
        return SkipPatterns(
            overallSkipRate = skipRate,
            averageSkipTime = averageSkipTime.toLong(),
            quickSkipThreshold = 15000L, // 15 seconds
            isImpatientListener = skipRate > 0.3f && averageSkipTime < 20000L
        )
    }
    
    private fun analyzeTimePreferences(sessions: List<ListeningSession>): Map<TimeOfDay, Float> {
        val timePreferences = mutableMapOf<TimeOfDay, Float>()
        
        sessions.forEach { session ->
            val hour = java.util.Calendar.getInstance().apply {
                timeInMillis = session.startTime
            }.get(java.util.Calendar.HOUR_OF_DAY)
            
            val timeOfDay = when (hour) {
                in 6..11 -> TimeOfDay.MORNING
                in 12..17 -> TimeOfDay.AFTERNOON
                in 18..22 -> TimeOfDay.EVENING
                else -> TimeOfDay.NIGHT
            }
            
            timePreferences[timeOfDay] = (timePreferences[timeOfDay] ?: 0f) + 1f
        }
        
        // Normalize to percentages
        val total = timePreferences.values.sum()
        return timePreferences.mapValues { it.value / total }
    }
    
    private fun analyzeGenreAffinities(tracks: List<TrackSession>): Map<String, Float> {
        val genreScores = mutableMapOf<String, Float>()
        
        tracks.forEach { track ->
            val genre = extractGenre(track.mediaItem)
            genre?.let { g ->
                val engagementScore = calculateEngagementScore(track)
                genreScores[g] = (genreScores[g] ?: 0f) + engagementScore
            }
        }
        
        // Normalize scores
        val maxScore = genreScores.values.maxOrNull() ?: 1f
        return genreScores.mapValues { it.value / maxScore }
    }
    
    private fun analyzeArtistLoyalty(tracks: List<TrackSession>): Map<String, Float> {
        val artistScores = mutableMapOf<String, Float>()
        
        tracks.forEach { track ->
            val artist = extractArtist(track.mediaItem)
            artist?.let { a ->
                val engagementScore = calculateEngagementScore(track)
                artistScores[a] = (artistScores[a] ?: 0f) + engagementScore
            }
        }
        
        // Normalize scores
        val maxScore = artistScores.values.maxOrNull() ?: 1f
        return artistScores.mapValues { it.value / maxScore }
    }
    
    private fun extractGenre(mediaItem: MediaItem): String? {
        return mediaItem.mediaMetadata.genre?.toString()
    }
    
    private fun extractArtist(mediaItem: MediaItem): String? {
        return mediaItem.mediaMetadata.artist?.toString()
    }
}

/**
 * Comprehensive listening patterns analysis
 */
data class ListeningPatterns(
    val averageSessionDuration: Long = 0,
    val averageTracksPerSession: Double = 0.0,
    val preferredSessionLength: SessionLength = SessionLength.MEDIUM,
    val skipPatterns: SkipPatterns = SkipPatterns(),
    val timeOfDayPreferences: Map<TimeOfDay, Float> = emptyMap(),
    val genreAffinities: Map<String, Float> = emptyMap(),
    val artistLoyalty: Map<String, Float> = emptyMap()
)

data class SkipPatterns(
    val overallSkipRate: Float = 0f,
    val averageSkipTime: Long = 0,
    val quickSkipThreshold: Long = 15000L,
    val isImpatientListener: Boolean = false
)

enum class SessionLength { SHORT, MEDIUM, LONG }
enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }