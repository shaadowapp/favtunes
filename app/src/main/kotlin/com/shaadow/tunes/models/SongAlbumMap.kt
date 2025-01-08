package com.shaadow.tunes.models

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.shaadow.tunes.models.Album
import com.shaadow.tunes.models.Song

@Immutable
@Entity(
    primaryKeys = ["songId", "albumId"],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Album::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SongAlbumMap(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val albumId: String,
    val position: Int?
)
