package com.shaadow.tunes.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaadow.innertube.Innertube
import com.shaadow.tunes.Database
import com.shaadow.tunes.enums.PlaylistSortBy
import com.shaadow.tunes.enums.SortOrder
import com.shaadow.tunes.models.PlaylistPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomePlaylistsViewModel : ViewModel() {
    var items: List<PlaylistPreview> by mutableStateOf(emptyList())
    var youtubePlaylists: List<Innertube.PlaylistItem> by mutableStateOf(emptyList())
    var isLoading: Boolean by mutableStateOf(true)

    init {
        loadLocalPlaylists()
    }

    private fun loadLocalPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                items = Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending).first().take(20)
                isLoading = false
            } catch (e: Exception) {
                items = emptyList()
                isLoading = false
            }
        }
    }

    fun loadArtists(sortBy: PlaylistSortBy, sortOrder: SortOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                items = Database.playlistPreviews(sortBy, sortOrder).first().take(20)
            } catch (e: Exception) {
                items = emptyList()
            }
        }
    }
}