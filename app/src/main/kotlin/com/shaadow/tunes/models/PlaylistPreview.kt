package com.shaadow.tunes.models

import androidx.compose.runtime.Immutable
import com.shaadow.tunes.models.Playlist

@Immutable
data class PlaylistPreview(
    val id: Long,
    val name: String,
    val songCount: Int
) {
    val playlist by lazy {
        Playlist(
            id = id,
            name = name
        )
    }
}
