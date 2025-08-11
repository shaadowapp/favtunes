package com.shaadow.tunes.suggestion.data

data class UserProfile(
    val preferredGenres: Map<String, Float> = emptyMap(), // genre -> weight
    val recentArtists: List<WeightedItem> = emptyList(),
    val skipHistory: Map<String, Long> = emptyMap(), // songId -> timestamp
    val likedSongs: Map<String, Long> = emptyMap(),
    val playHistory: List<PlayEvent> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class WeightedItem(
    val id: String, 
    val weight: Float, 
    val timestamp: Long
)

data class PlayEvent(
    val songId: String, 
    val duration: Long, 
    val timestamp: Long
)