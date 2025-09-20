package com.shaadow.tunes.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey val id: String,
    val songId: String,
    val keywords: String, // JSON string of keywords
    val isInterested: Boolean, // true for interested, false for not interested
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "song_keywords")
data class SongKeywords(
    @PrimaryKey val songId: String,
    val keywords: String, // JSON string of keywords from YouTube API
    val title: String,
    val artist: String,
    val timestamp: Long = System.currentTimeMillis()
)