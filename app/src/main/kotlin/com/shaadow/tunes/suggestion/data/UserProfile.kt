package com.shaadow.tunes.suggestion.data

data class UserProfile(
    val preferredGenres: Map<String, Float> = emptyMap(),
    val recentArtists: List<WeightedItem> = emptyList(),
    val skipHistory: Map<String, Long> = emptyMap(),
    val likedSongs: Map<String, Long> = emptyMap(),
    val playHistory: List<PlayEvent> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        fun default(): UserProfile = UserProfile()
        
        fun withInitialGenres(genres: Set<String>): UserProfile {
            val genreWeights = genres.associateWith { 0.5f }
            return UserProfile(preferredGenres = genreWeights)
        }
    }
    
    fun updateGenreWeight(genre: String, adjustment: Float): UserProfile {
        val currentWeight = preferredGenres[genre] ?: 0f
        val newWeight = (currentWeight + adjustment).coerceIn(0f, 1f)
        return copy(
            preferredGenres = preferredGenres + (genre to newWeight),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    fun addPlayEvent(songId: String, duration: Long): UserProfile {
        val event = PlayEvent(songId, duration, System.currentTimeMillis())
        val updatedHistory = (playHistory + event).takeLast(100) // Keep last 100 plays
        return copy(
            playHistory = updatedHistory,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    fun addToSkipHistory(songId: String): UserProfile {
        return copy(
            skipHistory = skipHistory + (songId to System.currentTimeMillis()),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    fun addLikedSong(songId: String): UserProfile {
        return copy(
            likedSongs = likedSongs + (songId to System.currentTimeMillis()),
            lastUpdated = System.currentTimeMillis()
        )
    }
}