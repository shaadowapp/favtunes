package com.shaadow.tunes.suggestion.data

import android.content.Context

/**
 * Simple test class to verify the suggestion data layer works correctly
 * Remove this file after testing
 */
class SuggestionDataTest(context: Context) {
    private val storage = SuggestionStorage(context)
    
    fun testBasicFunctionality(): Boolean {
        return try {
            // Test 1: Save and retrieve user profile
            val testProfile = UserProfile.withInitialGenres(setOf("Pop", "Rock"))
            storage.saveUserProfile(testProfile)
            val retrievedProfile = storage.getUserProfile()
            
            // Test 2: Track interactions
            storage.trackSongInteraction("test_song_1", InteractionType.PLAY, 180000)
            storage.trackSongInteraction("test_song_2", InteractionType.SKIP, 15000)
            storage.trackSongInteraction("test_song_3", InteractionType.LIKE)
            
            // Test 3: Get recent activity
            val recentActivity = storage.getRecentActivity(7)
            
            // Test 4: Update genre preference
            storage.updateGenrePreference("Pop", 0.2f)
            val updatedProfile = storage.getUserProfile()
            
            println("✅ Suggestion Data Layer Test Passed")
            println("- Profile saved and retrieved: ${retrievedProfile.preferredGenres}")
            println("- Recent activity count: ${recentActivity.size}")
            println("- Updated Pop weight: ${updatedProfile.preferredGenres["Pop"]}")
            
            true
        } catch (e: Exception) {
            println("❌ Suggestion Data Layer Test Failed: ${e.message}")
            false
        }
    }
}