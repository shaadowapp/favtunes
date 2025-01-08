package com.shaadow.tunes.models

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.shaadow.tunes.models.Playlist
import com.shaadow.tunes.models.Song

@Immutable
@Entity(
    primaryKeys = ["songId", "playlistId"],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SongPlaylistMap(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val playlistId: Long,
    val position: Int
)
