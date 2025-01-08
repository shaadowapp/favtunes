package com.shaadow.tunes.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import com.shaadow.tunes.models.Song

@Immutable
data class SongWithContentLength(
    @Embedded val song: Song,
    val contentLength: Long?
)
