package com.shaadow.tunes.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity
data class RecentSong(
    @PrimaryKey val songId: String,
    val lastPlayedAt: Long = System.currentTimeMillis()
)