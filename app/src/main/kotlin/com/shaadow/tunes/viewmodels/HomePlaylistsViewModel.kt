package com.shaadow.tunes.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.requests.searchPage
import com.shaadow.innertube.utils.from
import com.shaadow.tunes.Database
import com.shaadow.tunes.enums.PlaylistSortBy
import com.shaadow.tunes.enums.SortOrder
import com.shaadow.tunes.models.PlaylistPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePlaylistsViewModel : ViewModel() {
    var items: List<PlaylistPreview> by mutableStateOf(emptyList())
    var youtubePlaylists: List<Innertube.PlaylistItem> by mutableStateOf(emptyList())

    init {
        loadYoutubePlaylists()
    }

    private fun loadYoutubePlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = Innertube.searchPage(
                    query = "popular playlists",
                    params = Innertube.SearchFilter.FeaturedPlaylist.value,
                    fromMusicShelfRendererContent = Innertube.PlaylistItem::from
                )
                youtubePlaylists = result?.getOrNull()?.items?.take(6) ?: emptyList()
            } catch (e: Exception) {
                youtubePlaylists = emptyList()
            }
        }
    }

    suspend fun loadArtists(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ) {
        try {
            items = Database.playlistPreviews(sortBy, sortOrder).first().take(10) // Limit to 10 playlists
        } catch (e: Exception) {
            items = emptyList()
        }
    }
}