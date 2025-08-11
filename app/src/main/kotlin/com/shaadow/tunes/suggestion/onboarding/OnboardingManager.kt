package com.shaadow.tunes.suggestion.onboarding

import android.content.Context
import com.shaadow.tunes.suggestion.AdvancedSuggestionSystem
import com.shaadow.tunes.suggestion.data.SuggestionStorage

/**
 * Manages the onboarding flow for the suggestion system
 */
class OnboardingManager(private val context: Context) {
    private val suggestionSystem = AdvancedSuggestionSystem(context)
    private val storage = SuggestionStorage(context)
    
    /**
     * Check if user needs onboarding
     */
    fun needsOnboarding(): Boolean {
        return storage.isFirstLaunch()
    }
    
    /**
     * Complete onboarding with user preferences
     */
    fun completeOnboarding(preferences: OnboardingPreferences) {
        // Set initial genre preferences
        if (preferences.selectedGenres.isNotEmpty()) {
            suggestionSystem.setInitialPreferences(preferences.selectedGenres)
        }
        
        // Store onboarding completion
        storage.setFirstLaunchComplete()
        
        // Store additional preferences
        storeOnboardingPreferences(preferences)
    }
    
    /**
     * Skip onboarding (use defaults)
     */
    fun skipOnboarding() {
        storage.setFirstLaunchComplete()
        
        // Set some default preferences for better initial experience
        val defaultGenres = setOf("Pop", "Rock", "Hip-Hop")
        suggestionSystem.setInitialPreferences(defaultGenres)
    }
    
    /**
     * Get available genres for selection
     */
    fun getAvailableGenres(): List<Genre> {
        return listOf(
            Genre("Pop", "🎵", "Catchy melodies and mainstream appeal"),
            Genre("Rock", "🎸", "Guitar-driven music with energy"),
            Genre("Hip-Hop", "🎤", "Rhythmic spoken lyrics and beats"),
            Genre("Jazz", "🎺", "Improvisation and complex harmonies"),
            Genre("Classical", "🎼", "Orchestral and traditional compositions"),
            Genre("Electronic", "🎛️", "Synthesized and digital sounds"),
            Genre("Country", "🤠", "Storytelling with acoustic instruments"),
            Genre("R&B", "🎶", "Rhythm and blues with soulful vocals"),
            Genre("Reggae", "🌴", "Jamaican rhythms and laid-back vibes"),
            Genre("Folk", "🪕", "Traditional and acoustic storytelling"),
            Genre("Metal", "⚡", "Heavy guitars and powerful vocals"),
            Genre("Indie", "🎨", "Independent and alternative sounds")
        )
    }
    
    /**
     * Get listening habit options
     */
    fun getListeningHabits(): List<ListeningHabit> {
        return listOf(
            ListeningHabit(
                id = "casual",
                title = "Casual Listener",
                description = "I listen to music occasionally",
                icon = "🎧"
            ),
            ListeningHabit(
                id = "daily",
                title = "Daily Listener",
                description = "Music is part of my daily routine",
                icon = "📱"
            ),
            ListeningHabit(
                id = "enthusiast",
                title = "Music Enthusiast",
                description = "I'm always discovering new music",
                icon = "🎵"
            ),
            ListeningHabit(
                id = "background",
                title = "Background Music",
                description = "I prefer music while doing other things",
                icon = "🔄"
            )
        )
    }
    
    /**
     * Get discovery preferences
     */
    fun getDiscoveryPreferences(): List<DiscoveryPreference> {
        return listOf(
            DiscoveryPreference(
                id = "familiar",
                title = "Stick to Favorites",
                description = "I prefer music similar to what I already like",
                explorationLevel = 0.2f
            ),
            DiscoveryPreference(
                id = "balanced",
                title = "Balanced Mix",
                description = "Mix of familiar and new music",
                explorationLevel = 0.5f
            ),
            DiscoveryPreference(
                id = "adventurous",
                title = "Always Exploring",
                description = "I love discovering completely new music",
                explorationLevel = 0.8f
            )
        )
    }
    
    private fun storeOnboardingPreferences(preferences: OnboardingPreferences) {
        // Store preferences in SharedPreferences for later use
        val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putStringSet("selected_genres", preferences.selectedGenres)
            putString("listening_habit", preferences.listeningHabit?.id)
            putString("discovery_preference", preferences.discoveryPreference?.id)
            putFloat("exploration_level", preferences.discoveryPreference?.explorationLevel ?: 0.5f)
            apply()
        }
    }
}

/**
 * User preferences collected during onboarding
 */
data class OnboardingPreferences(
    val selectedGenres: Set<String> = emptySet(),
    val listeningHabit: ListeningHabit? = null,
    val discoveryPreference: DiscoveryPreference? = null
)

/**
 * Music genre option
 */
data class Genre(
    val name: String,
    val icon: String,
    val description: String
)

/**
 * Listening habit option
 */
data class ListeningHabit(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
)

/**
 * Discovery preference option
 */
data class DiscoveryPreference(
    val id: String,
    val title: String,
    val description: String,
    val explorationLevel: Float // 0.0 = conservative, 1.0 = very exploratory
)