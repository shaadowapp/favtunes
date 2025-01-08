package com.shaadow.tunes.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.AlbumSortBy
import com.shaadow.tunes.enums.SortOrder
import com.shaadow.tunes.models.Album
import com.shaadow.tunes.ui.components.SortingHeader
import com.shaadow.tunes.ui.items.LocalAlbumItem
import com.shaadow.tunes.utils.albumSortByKey
import com.shaadow.tunes.utils.albumSortOrderKey
import com.shaadow.tunes.utils.rememberPreference
import com.shaadow.tunes.viewmodels.HomeAlbumsViewModel

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeAlbums(onAlbumClick: (Album) -> Unit) {
    val playerPadding = LocalPlayerPadding.current

    var sortBy by rememberPreference(albumSortByKey, AlbumSortBy.Title)
    var sortOrder by rememberPreference(albumSortOrderKey, SortOrder.Ascending)

    val viewModel: HomeAlbumsViewModel = viewModel()

    LaunchedEffect(sortBy, sortOrder) {
        viewModel.loadAlbums(
            sortBy = sortBy,
            sortOrder = sortOrder
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp + playerPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(
            key = "header",
            span = { GridItemSpan(maxCurrentLineSpan) }
        ) {
            SortingHeader(
                sortBy = sortBy,
                changeSortBy = { sortBy = it },
                sortByEntries = AlbumSortBy.entries.toList(),
                sortOrder = sortOrder,
                toggleSortOrder = { sortOrder = !sortOrder },
                size = viewModel.items.size,
                itemCountText = R.plurals.number_of_albums
            )
        }

        items(
            items = viewModel.items,
            key = Album::id
        ) { album ->
            LocalAlbumItem(
                modifier = Modifier.animateItem(),
                album = album,
                onClick = { onAlbumClick(album) }
            )
        }
    }
}