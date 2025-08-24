package com.shaadow.tunes.notification

import com.shaadow.tunes.models.Song
import kotlin.random.Random

class NotificationContentGenerator {
    
    fun generateEngagementContent(hoursSinceLastOpen: Long): NotificationContent {
        val templates = when {
            hoursSinceLastOpen < 48 -> getSavageFriendlyTemplates()
            hoursSinceLastOpen < 168 -> getHumorousRoastTemplates()
            else -> getPlayfulSarcasticTemplates()
        }
        
        val selectedTemplate = templates.random()
        return selectedTemplate.copy(contentType = "engagement")
    }
    
    private fun getSavageFriendlyTemplates(): List<NotificationContent> = listOf(
        NotificationContent(
            title = "Welcome Back to FavTunes",
            body = "Your music library is ready for you",
            emoji = "🎧",
            actionText = "Start Listening",
            contentType = "engagement",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        ),
        NotificationContent(
            title = "New Music Awaits",
            body = "Discover fresh tracks and trending songs",
            emoji = "🎵",
            actionText = "Explore Music",
            contentType = "engagement",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        ),
        NotificationContent(
            title = "Your Playlists Miss You",
            body = "Continue where you left off",
            emoji = "📱",
            actionText = "Resume Playing",
            contentType = "engagement",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        )
    )
    
    private fun getHumorousRoastTemplates(): List<NotificationContent> = listOf(
        NotificationContent(
            title = "Time for Some Music",
            body = "Rediscover your favorite songs and artists",
            emoji = "🎶",
            actionText = "Play Music",
            contentType = "engagement",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        ),
        NotificationContent(
            title = "Music Recommendations Ready",
            body = "Personalized suggestions based on your taste",
            emoji = "🎯",
            actionText = "See Recommendations",
            contentType = "engagement",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        )
    )
    
    private fun getPlayfulSarcasticTemplates(): List<NotificationContent> = listOf(
        NotificationContent(
            title = "We've Missed You",
            body = "Your favorite music is waiting for your return",
            emoji = "🎵",
            actionText = "Come Back",
            contentType = "engagement",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        ),
        NotificationContent(
            title = "Music Break Over?",
            body = "Ready to dive back into your favorite tunes",
            emoji = "🎧",
            actionText = "Start Listening",
            contentType = "engagement",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        )
    )
    
    fun generateMusicSuggestionContent(recentSongs: List<Song>): NotificationContent {
        val templates = if (recentSongs.isNotEmpty()) {
            getPersonalizedSuggestionTemplates(recentSongs)
        } else {
            getGenericSuggestionTemplates()
        }
        
        val selectedTemplate = templates.random()
        return selectedTemplate.copy(
            contentType = "music_suggestion",
            songId = recentSongs.randomOrNull()?.id
        )
    }
    
    private fun getPersonalizedSuggestionTemplates(recentSongs: List<Song>): List<NotificationContent> {
        return listOf(
            NotificationContent(
                title = "New Music for You",
                body = "Discover songs similar to your recent favorites",
                emoji = "🎵",
                actionText = "Listen Now",
                contentType = "music_suggestion",
                personalityTone = PersonalityTone.ENCOURAGING_WITTY
            ),
            NotificationContent(
                title = "Personalized Recommendations",
                body = "Based on your listening history and preferences",
                emoji = "⭐",
                actionText = "Explore",
                contentType = "music_suggestion",
                personalityTone = PersonalityTone.ENCOURAGING_WITTY
            )
        )
    }
    
    private fun getGenericSuggestionTemplates(): List<NotificationContent> = listOf(
        NotificationContent(
            title = "Trending Music",
            body = "Check out what's popular right now",
            emoji = "🔥",
            actionText = "Listen Now",
            contentType = "music_suggestion",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        ),
        NotificationContent(
            title = "Daily Music Discovery",
            body = "New songs and artists to explore today",
            emoji = "🎵",
            actionText = "Discover",
            contentType = "music_suggestion",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        )
    )
    
    fun generateMarketingContent(): NotificationContent {
        val professionalNotifications = listOf(
            NotificationContent(
                title = "Discover New Music",
                body = "Fresh tracks and trending songs are waiting for you",
                emoji = "🎵",
                actionText = "Explore Now",
                contentType = "marketing",
                personalityTone = PersonalityTone.ENCOURAGING_WITTY
            ),
            NotificationContent(
                title = "Your Music Journey Continues",
                body = "New recommendations based on your listening history",
                emoji = "🎧",
                actionText = "Listen Now",
                contentType = "marketing",
                personalityTone = PersonalityTone.ENCOURAGING_WITTY
            ),
            NotificationContent(
                title = "Weekly Music Highlights",
                body = "Check out what's trending in your favorite genres",
                emoji = "⭐",
                actionText = "See Highlights",
                contentType = "marketing",
                personalityTone = PersonalityTone.ENCOURAGING_WITTY
            )
        )
        
        return professionalNotifications.random()
    }
    
    fun generatePersonalizedRoast(listeningHistory: List<Song>): NotificationContent {
        val roastTemplates = listOf(
            NotificationContent(
                title = "Your music taste is bold",
                body = "Not everyone can pull off that chaos",
                emoji = "🎯",
                actionText = "Own It",
                contentType = "roast",
                personalityTone = PersonalityTone.SAVAGE_FRIENDLY
            ),
            NotificationContent(
                title = "Plot twist unique taste",
                body = "And by unique we mean interesting",
                emoji = "🎭",
                actionText = "Embrace Weird",
                contentType = "roast",
                personalityTone = PersonalityTone.HUMOROUS_ROAST
            )
        )
        
        return roastTemplates.random()
    }
    
    suspend fun generateContextAwareContent(userContext: UserContext): NotificationContent {
        return when {
            userContext.skipRate > 0.7 -> generateSkipRoast(userContext)
            userContext.listeningStreak > 10 -> generateStreakRoast(userContext)
            userContext.favoriteGenres.size == 1 -> generateGenreRoast(userContext)
            userContext.hoursSinceLastOpen > 168 -> generateAbsenceRoast(userContext)
            userContext.musicMood == MusicMood.MELANCHOLIC -> generateMoodRoast(userContext)
            else -> generateGeneralRoast(userContext)
        }
    }
    
    private fun generateSkipRoast(userContext: UserContext): NotificationContent {
        return NotificationContent(
            title = "Skip champion detected",
            body = "Your finger must be getting a workout",
            emoji = "🏆",
            actionText = "Find The One",
            contentType = "context_roast",
            personalityTone = PersonalityTone.SAVAGE_FRIENDLY
        )
    }
    
    private fun generateStreakRoast(userContext: UserContext): NotificationContent {
        return NotificationContent(
            title = "Music addiction confirmed",
            body = "${userContext.listeningStreak} days straight Impressive",
            emoji = "🎵",
            actionText = "Keep Going",
            contentType = "context_roast",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        )
    }
    
    private fun generateGenreRoast(userContext: UserContext): NotificationContent {
        val genre = userContext.favoriteGenres.firstOrNull() ?: "your genre"
        return NotificationContent(
            title = "$genre purist detected",
            body = "Variety is the spice of life you know",
            emoji = "🎯",
            actionText = "Branch Out",
            contentType = "context_roast",
            personalityTone = PersonalityTone.PLAYFUL_SARCASTIC
        )
    }
    
    private fun generateAbsenceRoast(userContext: UserContext): NotificationContent {
        return NotificationContent(
            title = "Missing person alert",
            body = "Last seen enjoying good music",
            emoji = "🚨",
            actionText = "Report Back",
            contentType = "context_roast",
            personalityTone = PersonalityTone.HUMOROUS_ROAST
        )
    }
    
    private fun generateMoodRoast(userContext: UserContext): NotificationContent {
        return NotificationContent(
            title = "Sad music detector activated",
            body = "Need some happy beats to balance it out",
            emoji = "😢",
            actionText = "Cheer Up",
            contentType = "context_roast",
            personalityTone = PersonalityTone.ENCOURAGING_WITTY
        )
    }
    
    private fun generateGeneralRoast(userContext: UserContext): NotificationContent {
        return NotificationContent(
            title = "Your music stats are interesting",
            body = "Endless possibilities await your ears",
            emoji = "📊",
            actionText = "Surprise Me",
            contentType = "context_roast",
            personalityTone = PersonalityTone.SAVAGE_FRIENDLY
        )
    }
    
    /**
     * Generate trending music notification content
     */
    fun generateTrendingContent(songId: String): NotificationContent {
        val trendingTemplates = listOf(
            NotificationContent(
                title = "Trending alert",
                body = "This song is breaking the internet right now",
                emoji = "🔥",
                actionText = "Join the Hype",
                songId = songId,
                contentType = "trending",
                personalityTone = PersonalityTone.ENCOURAGING_WITTY
            ),
            NotificationContent(
                title = "Everyone's obsessed",
                body = "Don't be the last one to discover this banger",
                emoji = "🎵",
                actionText = "Catch Up",
                songId = songId,
                contentType = "trending",
                personalityTone = PersonalityTone.PLAYFUL_SARCASTIC
            )
        )
        
        return trendingTemplates.random()
    }
    
    /**
     * Generate personalized suggestion content
     */
    fun generatePersonalizedSuggestion(userHistory: List<Song>): NotificationContent {
        val personalizedTemplates = listOf(
            NotificationContent(
                title = "Your perfect match found",
                body = "Based on your questionable but lovable taste",
                emoji = "💎",
                actionText = "Meet Your Match",
                contentType = "personalized_suggestion",
                personalityTone = PersonalityTone.SAVAGE_FRIENDLY
            ),
            NotificationContent(
                title = "Algorithm breakthrough",
                body = "We finally figured out your music DNA",
                emoji = "🤖",
                actionText = "See Results",
                contentType = "personalized_suggestion",
                personalityTone = PersonalityTone.HUMOROUS_ROAST
            )
        )
        
        return personalizedTemplates.random().copy(
            songId = userHistory.randomOrNull()?.id
        )
    }
}