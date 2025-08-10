package com.shaadow.tunes.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.shaadow.tunes.Database
import com.shaadow.tunes.enums.SongSortBy
import com.shaadow.tunes.enums.SortOrder
import com.shaadow.tunes.models.Song
import kotlinx.coroutines.flow.first

class HomeSongsViewModel : ViewModel() {
    var items: List<Song> by mutableStateOf(emptyList())

    private val defaultSongs = listOf(
        Song(
            id = "4NRXx6U8ABQ",
            title = "Blinding Lights",
            artistsText = "The Weeknd",
            durationText = "3:20",
            thumbnailUrl = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg"
        ),
        Song(
            id = "JGwWNGJdvx8",
            title = "Shape of You",
            artistsText = "Ed Sheeran",
            durationText = "3:53",
            thumbnailUrl = "https://i.ytimg.com/vi/JGwWNGJdvx8/hqdefault.jpg"
        ),
        Song(
            id = "hLQl3WQQoQ0",
            title = "Someone Like You",
            artistsText = "Adele",
            durationText = "4:45",
            thumbnailUrl = "https://i.ytimg.com/vi/hLQl3WQQoQ0/hqdefault.jpg"
        ),
        Song(
            id = "fJ9rUzIMcZQ",
            title = "Bohemian Rhapsody",
            artistsText = "Queen",
            durationText = "5:55",
            thumbnailUrl = "https://i.ytimg.com/vi/fJ9rUzIMcZQ/hqdefault.jpg"
        ),
        Song(
            id = "09839DpTctU",
            title = "Hotel California",
            artistsText = "Eagles",
            durationText = "6:30",
            thumbnailUrl = "https://i.ytimg.com/vi/09839DpTctU/hqdefault.jpg"
        )
    )

    suspend fun loadSongs(
        sortBy: SongSortBy,
        sortOrder: SortOrder
    ) {
        try {
            val recentSongs = Database.songs(sortBy, sortOrder).first()
            items = if (recentSongs.isEmpty()) {
                defaultSongs.take(5) // Only show 5 default songs
            } else {
                recentSongs.take(20) // Limit to 20 recent songs for performance
            }
        } catch (e: Exception) {
            items = defaultSongs.take(5)
        }
    }
}