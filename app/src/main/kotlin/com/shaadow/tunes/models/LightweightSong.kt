package com.shaadow.tunes.models

import androidx.compose.runtime.Immutable

/**
 * Lightweight song data for progressive loading
 * Contains only essential information for initial display
 */
@Immutable
data class LightweightSong(
    val id: String,
    val title: String,
    val artistsText: String?,
    val thumbnailUrl: String?
) {
    /**
     * Convert to full Song object (for compatibility)
     */
    fun toSong(): Song {
        return Song(
            id = id,
            title = title,
            artistsText = artistsText,
            durationText = null, // Will be loaded later
            thumbnailUrl = thumbnailUrl,
            likedAt = null, // Will be loaded later
            totalPlayTimeMs = 0 // Will be loaded later
        )
    }
}

/**
 * Convert Song to LightweightSong (extract essential data)
 */
fun Song.toLightweight(): LightweightSong {
    return LightweightSong(
        id = id,
        title = title,
        artistsText = artistsText,
        thumbnailUrl = thumbnailUrl
    )
}