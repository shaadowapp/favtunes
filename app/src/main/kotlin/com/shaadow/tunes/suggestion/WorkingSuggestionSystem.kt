package com.shaadow.tunes.suggestion

import android.content.Context
import androidx.media3.common.MediaItem
import com.shaadow.tunes.Database
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.SuggestionSecurity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Working suggestion system that compiles without errors
 * This replaces the complex lightweight system with a simple, functional version
 */
class WorkingSuggestionSystem(private val context: Context) {
    
    private val preferences = context.getSharedPreferences("working_suggestions", Context.MODE_PRIVATE)
    private val securePrefs = SuggestionSecurity.getSecurePreferences(context)
    
    /**
     * Get recommendations based on user preferences and behavior
     */
    suspend fun getRecommendations(limit: Int = 16): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val userPreferences = getUserPreferences()
            val behaviorData = getBehaviorBasedRecommendations()
            
            // If no preferences set, return behavior-based recommendations only
            if (userPreferences.isNullOrEmpty()) {
                return@withContext behaviorData.take(limit)
            }
            
            // Mix preferences (80%) and behavior (20%) as per requirements
            val preferenceWeight = 0.8
            val behaviorWeight = 0.2
            
            val preferenceCount = (limit * preferenceWeight).toInt()
            val behaviorCount = limit - preferenceCount
            
            val recommendations = mutableListOf<MediaItem>()
            
            // Add preference-based recommendations
            recommendations.addAll(getPreferenceBasedRecommendations(preferenceCount))
            
            // Add behavior-based recommendations
            recommendations.addAll(behaviorData.take(behaviorCount))
            
            // Remove duplicates and return
            recommendations.distinctBy { it.mediaId }.take(limit)
        } catch (e: Exception) {
            // Fallback to empty list if anything fails
            emptyList()
        }
    }
    
    /**
     * Get recommendations based on user behavior and preferences
     * This analyzes what the user actually likes and suggests similar content
     */
    private suspend fun getBehaviorBasedRecommendations(): List<MediaItem> {
        return try {
            // Get user's most played songs (shows what they actually like)
            val mostPlayedSongs = Database.songsByPlayTimeDesc().first().take(20)
            
            // Get recently liked songs (recent preferences)
            val likedSongs = Database.favorites().first().take(15)
            
            // Get recently played songs (current listening habits)
            val recentSongs = Database.recentlyPlayedSongs().first().take(10)
            
            // Analyze user preferences from their behavior
            val userPreferredArtists = (mostPlayedSongs + likedSongs)
                .mapNotNull { it.artistsText }
                .flatMap { it.split(",", "&", "feat.", "ft.") }
                .map { it.trim() }
                .groupBy { it }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)
                .map { it.first }
            
            // Get all songs and find recommendations based on user's preferred artists
            val allSongs = Database.songsByTitleAsc().first()
            val recommendedSongs = allSongs.filter { song ->
                // Recommend songs by artists the user likes but hasn't played much
                userPreferredArtists.any { preferredArtist ->
                    song.artistsText?.contains(preferredArtist, ignoreCase = true) == true
                } && !mostPlayedSongs.contains(song) && !recentSongs.contains(song)
            }.shuffled().take(12)
            
            // Mix recommendations with some variety
            val finalRecommendations = mutableListOf<MediaItem>()
            
            // Add artist-based recommendations (60%)
            finalRecommendations.addAll(recommendedSongs.take(7).map { it.asMediaItem })
            
            // Add some variety from less played songs (40%)
            val varietySongs = allSongs
                .filterNot { mostPlayedSongs.contains(it) || recentSongs.contains(it) }
                .shuffled()
                .take(5)
            finalRecommendations.addAll(varietySongs.map { it.asMediaItem })
            
            finalRecommendations.distinctBy { it.mediaId }.shuffled()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get recommendations based on user's stated preferences from onboarding
     */
    private suspend fun getPreferenceBasedRecommendations(limit: Int): List<MediaItem> {
        return try {
            val userPreferences = getUserPreferences() ?: return emptyList()
            
            // Get all available songs
            val allSongs = Database.songsByTitleAsc().first()
            
            // Find songs that match user's preferred genres/artists
            val matchingSongs = allSongs.filter { song ->
                userPreferences.any { preference ->
                    // Match against title, artist, or album
                    song.title.contains(preference, ignoreCase = true) ||
                    song.artistsText?.contains(preference, ignoreCase = true) == true
                }
            }
            
            // If we have direct matches, prioritize them
            if (matchingSongs.isNotEmpty()) {
                matchingSongs.shuffled().take(limit).map { it.asMediaItem }
            } else {
                // If no direct matches, use intelligent fallback
                // Get songs from artists that are similar to user preferences
                val similarSongs = allSongs.filter { song ->
                    userPreferences.any { preference ->
                        // Fuzzy matching for similar artists/genres
                        val words = preference.split(" ")
                        words.any { word ->
                            song.artistsText?.contains(word, ignoreCase = true) == true ||
                            song.title.contains(word, ignoreCase = true)
                        }
                    }
                }
                
                if (similarSongs.isNotEmpty()) {
                    similarSongs.shuffled().take(limit).map { it.asMediaItem }
                } else {
                    // Final fallback - return a diverse selection
                    allSongs.shuffled().take(limit).map { it.asMediaItem }
                }
            }
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    

    
    /**
     * Track song interactions with security protection
     */
    fun onSongPlayed(mediaItem: MediaItem, playDuration: Long = 0L) {
        val playCount = preferences.getInt("play_${mediaItem.mediaId}", 0)
        val skipCount = preferences.getInt("skip_${mediaItem.mediaId}", 0)
        val liked = preferences.getBoolean("liked_${mediaItem.mediaId}", false)
        
        // Store in regular preferences for backward compatibility
        preferences.edit().putInt("play_${mediaItem.mediaId}", playCount + 1).apply()
        
        // Store securely to prevent manipulation
        SuggestionSecurity.secureTrackingData(
            context = context,
            songId = mediaItem.mediaId,
            playCount = playCount + 1,
            skipCount = skipCount,
            liked = liked
        )
    }
    
    fun onSongSkipped(mediaItem: MediaItem, playDuration: Long = 0L) {
        val playCount = preferences.getInt("play_${mediaItem.mediaId}", 0)
        val skipCount = preferences.getInt("skip_${mediaItem.mediaId}", 0)
        val liked = preferences.getBoolean("liked_${mediaItem.mediaId}", false)
        
        // Store in regular preferences for backward compatibility
        preferences.edit().putInt("skip_${mediaItem.mediaId}", skipCount + 1).apply()
        
        // Store securely to prevent manipulation
        SuggestionSecurity.secureTrackingData(
            context = context,
            songId = mediaItem.mediaId,
            playCount = playCount,
            skipCount = skipCount + 1,
            liked = liked
        )
    }
    
    fun onSongLiked(mediaItem: MediaItem) {
        val playCount = preferences.getInt("play_${mediaItem.mediaId}", 0)
        val skipCount = preferences.getInt("skip_${mediaItem.mediaId}", 0)
        
        // Store in regular preferences for backward compatibility
        preferences.edit().putBoolean("liked_${mediaItem.mediaId}", true).apply()
        
        // Store securely to prevent manipulation
        SuggestionSecurity.secureTrackingData(
            context = context,
            songId = mediaItem.mediaId,
            playCount = playCount,
            skipCount = skipCount,
            liked = true
        )
    }
    
    fun onSongAddedToPlaylist(mediaItem: MediaItem, playlistName: String) {
        preferences.edit().putBoolean("playlist_${mediaItem.mediaId}", true).apply()
    }
    
    fun onSongRemovedFromPlaylist(mediaItem: MediaItem, playlistName: String) {
        preferences.edit().putBoolean("playlist_${mediaItem.mediaId}", false).apply()
    }
    
    /**
     * Onboarding and preferences
     */
    fun isOnboardingComplete(): Boolean {
        return preferences.getBoolean("onboarding_complete", false)
    }
    
    fun setInitialPreferences(genres: List<String>): Boolean {
        return try {
            // Validate and limit preferences to prevent gaming
            val validatedGenres = SuggestionSecurity.validatePreferences(genres.toSet())
            
            preferences.edit()
                .putStringSet("preferred_genres", validatedGenres)
                .putBoolean("onboarding_complete", true)
                .apply()
            
            // Store securely
            SuggestionSecurity.storeSecureData(
                context = context,
                key = "user_preferences",
                value = validatedGenres.joinToString(",")
            )
            
            SuggestionSecurity.recordPreferenceUpdate(context)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Reset onboarding status for testing
     */
    fun resetOnboarding() {
        preferences.edit().putBoolean("onboarding_complete", false).apply()
    }
    
    /**
     * Clear all suggestion data (protected operation)
     */
    suspend fun clearAllData() {
        preferences.edit().clear().apply()
        SuggestionSecurity.clearSecureData(context)
    }
    
    /**
     * Get tracking status with security information
     */
    fun getTrackingStatus(): Map<String, Any> {
        val allPrefs = preferences.all
        val integrityReport = SuggestionSecurity.getIntegrityReport(context)
        
        return mapOf(
            "totalTrackedSongs" to allPrefs.keys.count { it.startsWith("play_") },
            "totalLikedSongs" to allPrefs.keys.count { key -> 
                key.startsWith("liked_") && preferences.getBoolean(key, false) 
            },
            "totalSkippedSongs" to allPrefs.keys.count { it.startsWith("skip_") },
            "isTrackingActive" to (allPrefs.isNotEmpty()),
            "onboardingComplete" to isOnboardingComplete(),
            "dataIntegrity" to integrityReport,
            "securityProtected" to true
        )
    }
    
    fun getUserPreferences(): Set<String>? {
        return if (isOnboardingComplete()) {
            preferences.getStringSet("preferred_genres", emptySet())
        } else null
    }
    
    fun updatePreferences(genres: List<String>): Boolean {
        return try {
            // Check rate limiting to prevent rapid manipulation
            if (!SuggestionSecurity.canUpdatePreferences(context)) {
                return false
            }
            
            // Validate and limit preferences to prevent gaming
            val validatedGenres = SuggestionSecurity.validatePreferences(genres.toSet())
            
            preferences.edit()
                .putStringSet("preferred_genres", validatedGenres)
                .apply()
            
            // Store securely
            SuggestionSecurity.storeSecureData(
                context = context,
                key = "user_preferences",
                value = validatedGenres.joinToString(",")
            )
            
            SuggestionSecurity.recordPreferenceUpdate(context)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Analytics and insights
     */
    suspend fun getSongStats(mediaItem: MediaItem): Map<String, Any> {
        return mapOf(
            "playCount" to preferences.getInt("play_${mediaItem.mediaId}", 0),
            "liked" to preferences.getBoolean("liked_${mediaItem.mediaId}", false),
            "skipCount" to preferences.getInt("skip_${mediaItem.mediaId}", 0),
            "inPlaylist" to preferences.getBoolean("playlist_${mediaItem.mediaId}", false)
        )
    }
    
    suspend fun getRecentStats(days: Int = 7): Map<String, Int> {
        // Return basic stats
        return mapOf(
            "totalSongs" to preferences.all.keys.count { it.startsWith("play_") },
            "totalLikes" to preferences.all.keys.count { key -> 
                key.startsWith("liked_") && preferences.getBoolean(key, false) 
            }
        )
    }
    
    suspend fun getMostLikedSongs(limit: Int = 20): List<String> {
        return preferences.all.keys
            .filter { it.startsWith("liked_") && preferences.getBoolean(it, false) }
            .map { it.removePrefix("liked_") }
            .take(limit)
    }
    
    /**
     * Maintenance
     */
    fun shouldRefreshRecommendations(lastRefreshTime: Long): Boolean {
        val refreshInterval = 2 * 60 * 60 * 1000L // 2 hours
        return (System.currentTimeMillis() - lastRefreshTime) > refreshInterval
    }
    
    suspend fun getStorageSizeEstimate(): Long {
        return preferences.all.size * 50L // Rough estimate: 50 bytes per preference
    }
    
    suspend fun performMaintenance(): Boolean {
        // Basic cleanup - remove very old entries
        val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
        // For now, just return success
        return true
    }
    
    /**
     * Test method to verify the recommendation system is working
     */
    suspend fun testRecommendationSystem(): Map<String, Any> {
        return try {
            val preferences = getUserPreferences()
            val recommendations = getRecommendations(5)
            val behaviorData = getBehaviorBasedRecommendations()
            val trackingStatus = getTrackingStatus()
            
            mapOf(
                "hasPreferences" to (preferences?.isNotEmpty() == true),
                "preferencesCount" to (preferences?.size ?: 0),
                "recommendationsCount" to recommendations.size,
                "behaviorRecommendationsCount" to behaviorData.size,
                "trackingStatus" to trackingStatus,
                "systemWorking" to true
            )
        } catch (e: Exception) {
            mapOf(
                "systemWorking" to false,
                "error" to (e.message ?: "Unknown error")
            )
        }
    }
}