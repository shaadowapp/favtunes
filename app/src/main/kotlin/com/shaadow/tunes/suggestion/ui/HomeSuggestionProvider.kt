package com.shaadow.tunes.suggestion.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import com.shaadow.tunes.suggestion.AdvancedSuggestionSystem
import com.shaadow.tunes.suggestion.analytics.SessionContext
import com.shaadow.tunes.suggestion.analytics.TimeOfDay
import com.shaadow.tunes.suggestion.engine.SuggestionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Provides dynamic home screen sections based on user behavior and preferences
 */
class HomeSuggestionProvider(private val suggestionSystem: AdvancedSuggestionSystem) {
    
    suspend fun getHomeSections(): List<SuggestionSection> = withContext(Dispatchers.IO) {
        val sections = mutableListOf<SuggestionSection>()
        val analytics = suggestionSystem.getListeningAnalytics()
        
        // 1. Contextual time-based section
        val timeContext = getCurrentTimeContext()
        sections.add(
            SuggestionSection(
                title = timeContext.title,
                subtitle = timeContext.subtitle,
                songs = suggestionSystem.getContextualRecommendations(
                    SessionContext.DISCOVERY, 
                    limit = 12
                ),
                type = SectionType.CONTEXTUAL,
                priority = 1
            )
        )
        
        // 2. Continue listening (if recent activity exists)
        if (analytics.patterns.averageSessionDuration > 0) {
            sections.add(
                SuggestionSection(
                    title = "Continue Listening",
                    subtitle = "Pick up where you left off",
                    songs = suggestionSystem.getAdvancedRecommendations(
                        context = SuggestionContext.GENERAL,
                        limit = 8
                    ),
                    type = SectionType.CONTINUE_LISTENING,
                    priority = 2
                )
            )
        }
        
        // 3. Top genre section (based on user preferences)
        val topGenre = analytics.preferredGenres.entries.firstOrNull()
        topGenre?.let { (genre, _) ->
            sections.add(
                SuggestionSection(
                    title = "More $genre",
                    subtitle = "Since you love $genre music",
                    songs = getGenreBasedSuggestions(genre, 10),
                    type = SectionType.GENRE_BASED,
                    priority = 3
                )
            )
        }
        
        // 4. Discovery section (for exploration)
        if (analytics.patterns.averageTracksPerSession > 5) {
            sections.add(
                SuggestionSection(
                    title = "Discover New Music",
                    subtitle = "Expand your musical horizons",
                    songs = suggestionSystem.getAdvancedRecommendations(
                        context = SuggestionContext.DISCOVERY,
                        limit = 15
                    ),
                    type = SectionType.DISCOVERY,
                    priority = 4
                )
            )
        }
        
        // 5. Recently liked artists
        if (analytics.totalLikedSongs > 0) {
            sections.add(
                SuggestionSection(
                    title = "From Artists You Like",
                    subtitle = "More from your favorite artists",
                    songs = getArtistBasedSuggestions(8),
                    type = SectionType.ARTIST_BASED,
                    priority = 5
                )
            )
        }
        
        // 6. Mood-based section (if we have enough data)
        if (analytics.totalSongsPlayed > 50) {
            val moodSection = getMoodBasedSection()
            moodSection?.let { sections.add(it) }
        }
        
        // Sort by priority and return
        sections.sortedBy { it.priority }
    }
    
    private fun getCurrentTimeContext(): TimeContext {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> TimeContext(
                "Good Morning",
                "Start your day with great music",
                TimeOfDay.MORNING
            )
            in 12..17 -> TimeContext(
                "Afternoon Vibes",
                "Keep the energy going",
                TimeOfDay.AFTERNOON
            )
            in 18..22 -> TimeContext(
                "Evening Chill",
                "Wind down with these tracks",
                TimeOfDay.EVENING
            )
            else -> TimeContext(
                "Late Night Sessions",
                "Perfect for night listening",
                TimeOfDay.NIGHT
            )
        }
    }
    
    private suspend fun getGenreBasedSuggestions(genre: String, limit: Int): List<MediaItem> {
        // In a real implementation, this would filter by genre
        // For now, return general recommendations
        return suggestionSystem.getAdvancedRecommendations(
            context = SuggestionContext.DISCOVERY,
            limit = limit
        )
    }
    
    private suspend fun getArtistBasedSuggestions(limit: Int): List<MediaItem> {
        // In a real implementation, this would be based on liked artists
        return suggestionSystem.getAdvancedRecommendations(
            context = SuggestionContext.GENERAL,
            limit = limit
        )
    }
    
    private suspend fun getMoodBasedSection(): SuggestionSection? {
        val analytics = suggestionSystem.getListeningAnalytics()
        
        // Simple mood detection based on listening patterns
        val mood = when {
            analytics.patterns.skipPatterns.overallSkipRate < 0.2f -> "Feel Good Hits"
            analytics.patterns.averageSessionDuration > 30 * 60 * 1000 -> "Deep Listening"
            analytics.patterns.skipPatterns.isImpatientListener -> "Quick Picks"
            else -> "Mixed Vibes"
        }
        
        return SuggestionSection(
            title = mood,
            subtitle = "Curated for your listening style",
            songs = suggestionSystem.getAdvancedRecommendations(
                context = SuggestionContext.DISCOVERY,
                limit = 12
            ),
            type = SectionType.MOOD_BASED,
            priority = 6
        )
    }
}

/**
 * Represents a section of suggestions for the home screen
 */
data class SuggestionSection(
    val title: String,
    val subtitle: String,
    val songs: List<MediaItem>,
    val type: SectionType,
    val priority: Int
)

/**
 * Types of suggestion sections
 */
enum class SectionType {
    CONTEXTUAL,         // Time-based contextual suggestions
    CONTINUE_LISTENING, // Continue from previous session
    GENRE_BASED,        // Based on preferred genres
    ARTIST_BASED,       // Based on liked artists
    DISCOVERY,          // For music discovery
    MOOD_BASED,         // Based on listening mood/patterns
    TRENDING,           // Popular/trending content
    PERSONALIZED        // General personalized recommendations
}

/**
 * Time-based context information
 */
private data class TimeContext(
    val title: String,
    val subtitle: String,
    val timeOfDay: TimeOfDay
)