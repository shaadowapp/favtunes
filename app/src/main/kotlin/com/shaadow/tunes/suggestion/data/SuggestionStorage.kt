package com.shaadow.tunes.suggestion.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.TimeUnit

class SuggestionStorage(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "suggestion_storage"
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LAST_CLEANUP = "last_cleanup"
    }
    
    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString(KEY_USER_PROFILE, json).apply()
    }
    
    fun getUserProfile(): UserProfile {
        val json = prefs.getString(KEY_USER_PROFILE, null)
        return if (json != null) {
            try {
                gson.fromJson(json, UserProfile::class.java)
            } catch (e: Exception) {
                UserProfile.default()
            }
        } else {
            UserProfile.default()
        }
    }
    
    fun trackSongInteraction(songId: String, action: InteractionType, duration: Long = 0) {
        val currentProfile = getUserProfile()
        val updatedProfile = when (action) {
            InteractionType.PLAY -> currentProfile.addPlayEvent(songId, duration)
            InteractionType.SKIP -> currentProfile.addToSkipHistory(songId)
            InteractionType.LIKE -> currentProfile.addLikedSong(songId)
            InteractionType.COMPLETE -> currentProfile.addPlayEvent(songId, duration)
            InteractionType.DISLIKE -> currentProfile.addToSkipHistory(songId)
        }
        saveUserProfile(updatedProfile)
    }
    
    fun getRecentActivity(days: Int = 7): List<PlayEvent> {
        val profile = getUserProfile()
        val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return profile.playHistory.filter { it.timestamp >= cutoffTime }
    }
    
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
    
    fun cleanOldData() {
        val lastCleanup = prefs.getLong(KEY_LAST_CLEANUP, 0)
        val now = System.currentTimeMillis()
        
        // Clean every 24 hours
        if (now - lastCleanup < TimeUnit.HOURS.toMillis(24)) return
        
        val profile = getUserProfile()
        val cutoffTime = now - TimeUnit.DAYS.toMillis(30)
        
        // Remove old skip history and liked songs
        val cleanedSkipHistory = profile.skipHistory.filterValues { it >= cutoffTime }
        val cleanedLikedSongs = profile.likedSongs.filterValues { it >= cutoffTime }
        val cleanedPlayHistory = profile.playHistory.filter { it.timestamp >= cutoffTime }
        
        val cleanedProfile = profile.copy(
            skipHistory = cleanedSkipHistory,
            likedSongs = cleanedLikedSongs,
            playHistory = cleanedPlayHistory
        )
        
        saveUserProfile(cleanedProfile)
        prefs.edit().putLong(KEY_LAST_CLEANUP, now).apply()
    }
    
    fun updateGenrePreference(genre: String, adjustment: Float) {
        val profile = getUserProfile()
        val updatedProfile = profile.updateGenreWeight(genre, adjustment)
        saveUserProfile(updatedProfile)
    }
    
    fun setInitialGenres(genres: Set<String>) {
        val profile = UserProfile.withInitialGenres(genres)
        saveUserProfile(profile)
        setFirstLaunchComplete()
    }
    
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}