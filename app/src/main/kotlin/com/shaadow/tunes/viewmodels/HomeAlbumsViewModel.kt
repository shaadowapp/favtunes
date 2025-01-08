package com.shaadow.tunes.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.shaadow.tunes.Database
import com.shaadow.tunes.enums.AlbumSortBy
import com.shaadow.tunes.enums.SortOrder
import com.shaadow.tunes.models.Album

class HomeAlbumsViewModel : ViewModel() {
    var items: List<Album> by mutableStateOf(emptyList())

    suspend fun loadAlbums(
        sortBy: AlbumSortBy,
        sortOrder: SortOrder
    ) {
        Database
            .albums(sortBy, sortOrder)
            .collect { items = it }
    }
}