package com.shaadow.tunes.notification

import android.content.Context
import com.shaadow.tunes.Database
import com.shaadow.tunes.models.Song
import kotlinx.coroutines.flow.first
import java.util.*



class UserContextAnalyzer(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("user_context", Context.MODE_PRIVATE)
    
    suspend fun analyzeUserContext(): UserContext {
        val hoursSinceLastOpen = getHoursSinceLastOpen()
        val favoriteGenres = getFavoriteGenres()
        val listeningStreak = getListeningStreak()
        val skipRate = getSkipRate()
        val timeOfDay = getCurrentTimeOfDay()
        val musicMood = analyzeMusicMood()
        val totalSongs = getTotalSongs()
        val recentActivity = getRecentActivity()
        
        return UserContext(
            hoursSinceLastOpen = hoursSinceLastOpen,
            favoriteGenres = favoriteGenres,
            listeningStreak = listeningStreak,
            skipRate = skipRate,
            timeOfDay = timeOfDay,
            musicMood = musicMood,
            totalListeningTime = 0L, // Default value
            recentSongs = emptyList() // Default value
        )
    }
    
    suspend fun buildMusicProfile(): MusicProfile {
        val topGenres = getTopGenres()
        val listeningHours = getAverageListeningHours()
        val skipBehavior = analyzeSkipBehavior()
        val discoveryRate = getDiscoveryRate()
        val moodPatterns = analyzeMoodPatterns()
        val socialSharing = getSocialSharingBehavior()
        
        return MusicProfile(
            topGenres = topGenres,
            listeningHours = listeningHours,
            skipBehavior = skipBehavior,
            discoveryRate = discoveryRate,
            moodPatterns = moodPatterns,
            socialSharing = socialSharing
        )
    }
    
    private fun getHoursSinceLastOpen(): Long {
        val lastOpen = prefs.getLong("last_app_open", System.currentTimeMillis())
        return (System.currentTimeMillis() - lastOpen) / (1000 * 60 * 60)
    }
    
    private suspend fun getFavoriteGenres(): List<String> {
        return try {
            val recentSongs = Database.songs(
                sortBy = com.shaadow.tunes.enums.SongSortBy.DateAdded, 
                sortOrder = com.shaadow.tunes.enums.SortOrder.Descending
            ).first().take(20)
            val genres = extractGenresFromSongs(recentSongs)
            genres.take(3)
        } catch (e: Exception) {
            listOf("Pop", "Rock", "Electronic") // Default fallback
        }
    }
    
    private fun getListeningStreak(): Int {
        return prefs.getInt("listening_streak", 0)
    }
    
    private fun getSkipRate(): Double {
        val totalPlays = prefs.getInt("total_plays", 1)
        val totalSkips = prefs.getInt("total_skips", 0)
        return if (totalPlays > 0) totalSkips.toDouble() / totalPlays else 0.0
    }
    
    private fun getCurrentTimeOfDay(): TimeOfDay {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> TimeOfDay.MORNING
            in 12..17 -> TimeOfDay.AFTERNOON
            in 18..21 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }
    
    private suspend fun analyzeMusicMood(): MusicMood {
        return try {
            val recentSongs = Database.recentlyPlayedSongs().first().take(5)
            val timeOfDay = getCurrentTimeOfDay()
            
            // Simple mood analysis based on time and recent activity
            when (timeOfDay) {
                TimeOfDay.MORNING -> if (recentSongs.isNotEmpty()) MusicMood.ENERGETIC else MusicMood.RELAXED
                TimeOfDay.AFTERNOON -> MusicMood.FOCUSED
                TimeOfDay.EVENING -> MusicMood.RELAXED
                TimeOfDay.NIGHT -> if (getSkipRate() > 0.3) MusicMood.MELANCHOLIC else MusicMood.PARTY
            }
        } catch (e: Exception) {
            MusicMood.UNKNOWN
        }
    }
    
    private suspend fun getTotalSongs(): Int {
        return try {
            Database.songs(
                sortBy = com.shaadow.tunes.enums.SongSortBy.Title, 
                sortOrder = com.shaadow.tunes.enums.SortOrder.Ascending
            ).first().size
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getRecentActivity(): String {
        val hoursSinceLastOpen = getHoursSinceLastOpen()
        return when {
            hoursSinceLastOpen < 1 -> "Very Active"
            hoursSinceLastOpen < 24 -> "Recently Active"
            hoursSinceLastOpen < 168 -> "Moderately Active"
            else -> "Inactive"
        }
    }
    
    private suspend fun getTopGenres(): List<String> {
        return try {
            val allSongs = Database.songs(
                sortBy = com.shaadow.tunes.enums.SongSortBy.PlayTime, 
                sortOrder = com.shaadow.tunes.enums.SortOrder.Descending
            ).first().take(100)
            extractGenresFromSongs(allSongs).take(5)
        } catch (e: Exception) {
            listOf("Pop", "Rock", "Hip-Hop")
        }
    }
    
    private fun getAverageListeningHours(): Double {
        val totalMinutes = prefs.getInt("total_listening_minutes", 0)
        val daysTracked = prefs.getInt("days_tracked", 1)
        return (totalMinutes / 60.0) / daysTracked
    }
    
    private fun analyzeSkipBehavior(): SkipBehavior {
        val skipRate = getSkipRate()
        val patienceLevel = when {
            skipRate < 0.2 -> "High"
            skipRate < 0.5 -> "Medium"
            else -> "Low"
        }
        
        val skipReasons = when (patienceLevel) {
            "High" -> listOf("Song doesn't match mood", "Poor audio quality")
            "Medium" -> listOf("Not in the mood", "Heard too recently", "Wrong tempo")
            else -> listOf("Impatient", "Easily bored", "Seeking perfect match")
        }
        
        return SkipBehavior(
            averageSkipTime = 30.0, // Default average skip time in seconds
            skipRate = skipRate,
            genreSkipRates = emptyMap() // Default empty map
        )
    }
    
    private fun getDiscoveryRate(): Double {
        val newSongsThisWeek = prefs.getInt("new_songs_week", 0)
        val totalSongsPlayed = prefs.getInt("total_plays", 1)
        return if (totalSongsPlayed > 0) newSongsThisWeek.toDouble() / totalSongsPlayed else 0.0
    }
    
    private suspend fun analyzeMoodPatterns(): Map<TimeOfDay, List<String>> {
        // Simplified mood pattern analysis
        return mapOf(
            TimeOfDay.MORNING to listOf("Energetic", "Upbeat", "Motivational"),
            TimeOfDay.AFTERNOON to listOf("Focus", "Ambient", "Productive"),
            TimeOfDay.EVENING to listOf("Relaxing", "Chill", "Unwinding"),
            TimeOfDay.NIGHT to listOf("Mellow", "Introspective", "Calm")
        )
    }
    
    private fun getSocialSharingBehavior(): Boolean {
        return prefs.getBoolean("shares_music", false)
    }
    
    private fun extractGenresFromSongs(songs: List<Song>): List<String> {
        // Simple genre extraction based on song characteristics
        // In a real app, you'd have proper genre metadata
        val genres = mutableListOf<String>()
        
        songs.forEach { song ->
            val title = song.title?.lowercase() ?: ""
            val artist = song.artistsText?.lowercase() ?: ""
            
            when {
                title.contains("remix") || title.contains("mix") -> genres.add("Electronic")
                artist.contains("feat") || artist.contains("ft") -> genres.add("Hip-Hop")
                title.contains("acoustic") -> genres.add("Acoustic")
                title.contains("live") -> genres.add("Live")
                song.durationText?.contains("1:") == true || song.durationText?.contains("2:") == true -> genres.add("Pop")
                song.durationText?.contains("5:") == true || song.durationText?.contains("6:") == true -> genres.add("Rock")
                else -> genres.add("Pop") // Default
            }
        }
        
        return genres.groupingBy { it }.eachCount().toList()
            .sortedByDescending { it.second }
            .map { it.first }
            .distinct()
    }
    
    fun updateUserActivity() {
        prefs.edit()
            .putLong("last_app_open", System.currentTimeMillis())
            .putInt("listening_streak", getListeningStreak() + 1)
            .apply()
    }
    
    fun trackSongPlay(song: Song, playDuration: Long) {
        val totalPlays = prefs.getInt("total_plays", 0)
        val totalMinutes = prefs.getInt("total_listening_minutes", 0)
        
        prefs.edit()
            .putInt("total_plays", totalPlays + 1)
            .putInt("total_listening_minutes", totalMinutes + (playDuration / 60000).toInt())
            .apply()
    }
    
    fun trackSongSkip(song: Song) {
        val totalSkips = prefs.getInt("total_skips", 0)
        prefs.edit()
            .putInt("total_skips", totalSkips + 1)
            .apply()
    }
}