package com.shaadow.tunes.suggestion.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SuggestionStorage(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("suggestion_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val USER_PROFILE_KEY = "user_profile"
        private const val FIRST_LAUNCH_KEY = "first_launch"
        private const val DATA_VERSION_KEY = "data_version"
        private const val CURRENT_VERSION = 1
    }
    
    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        prefs.edit()
            .putString(USER_PROFILE_KEY, json)
            .putInt(DATA_VERSION_KEY, CURRENT_VERSION)
            .apply()
    }
    
    fun getUserProfile(): UserProfile {
        val json = prefs.getString(USER_PROFILE_KEY, null)
        return if (json != null) {
            try {
                gson.fromJson(json, UserProfile::class.java)
            } catch (e: Exception) {
                UserProfile() // Return default if parsing fails
            }
        } else {
            UserProfile()
        }
    }
    
    fun trackSongInteraction(songId: String, action: InteractionType, duration: Long = 0) {
        val profile = getUserProfile()
        val timestamp = System.currentTimeMillis()
        
        val updatedProfile = when (action) {
            InteractionType.LIKE -> profile.copy(
                likedSongs = profile.likedSongs + (songId to timestamp)
            )
            InteractionType.SKIP -> profile.copy(
                skipHistory = profile.skipHistory + (songId to timestamp)
            )
            InteractionType.PLAY, InteractionType.COMPLETE -> {
                val newPlayEvent = PlayEvent(songId, duration, timestamp)
                profile.copy(
                    playHistory = (profile.playHistory + newPlayEvent).takeLast(1000) // Keep last 1000 plays
                )
            }
            else -> profile
        }
        
        saveUserProfile(updatedProfile.copy(lastUpdated = timestamp))
    }
    
    fun getRecentActivity(days: Int = 7): List<PlayEvent> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return getUserProfile().playHistory.filter { it.timestamp > cutoff }
    }
    
    fun updateGenrePreference(genre: String, weightDelta: Float) {
        val profile = getUserProfile()
        val currentWeight = profile.preferredGenres[genre] ?: 0f
        val newWeight = (currentWeight + weightDelta).coerceIn(0f, 1f)
        
        val updatedGenres = profile.preferredGenres.toMutableMap()
        updatedGenres[genre] = newWeight
        
        saveUserProfile(profile.copy(preferredGenres = updatedGenres))
    }
    
    fun cleanOldData() {
        val profile = getUserProfile()
        val cutoff = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
        
        val cleanedProfile = profile.copy(
            skipHistory = profile.skipHistory.filterValues { it > cutoff },
            playHistory = profile.playHistory.filter { it.timestamp > cutoff },
            recentArtists = profile.recentArtists.filter { it.timestamp > cutoff }
        )
        
        saveUserProfile(cleanedProfile)
    }
    
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(FIRST_LAUNCH_KEY, true)
    }
    
    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply()
    }
}