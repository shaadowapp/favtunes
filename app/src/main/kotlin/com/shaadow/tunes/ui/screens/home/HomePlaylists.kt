package com.shaadow.tunes.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.Database
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.BuiltInPlaylist
import com.shaadow.tunes.models.Playlist
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.components.themed.TextFieldDialog
import com.shaadow.tunes.ui.items.BuiltInPlaylistItem
import com.shaadow.tunes.ui.items.LocalPlaylistItem
import com.shaadow.tunes.utils.PlaylistTracker
import com.shaadow.tunes.viewmodels.HomePlaylistsViewModel

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomePlaylists(
    onBuiltInPlaylist: (Int) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onYouTubePlaylistClick: (String) -> Unit = {}
) {
    var isCreatingANewPlaylist by rememberSaveable { mutableStateOf(false) }
    val viewModel: HomePlaylistsViewModel = viewModel()

    if (isCreatingANewPlaylist) {
        TextFieldDialog(
            title = stringResource(id = R.string.new_playlist),
            hintText = stringResource(id = R.string.playlist_name_hint),
            onDismiss = {
                isCreatingANewPlaylist = false
            },
            onDone = { text ->
                query {
                    Database.insert(Playlist(name = text))
                }
                PlaylistTracker.trackPlaylistCreated(text)
            }
        )
    }

    if (viewModel.isLoading && viewModel.items.isEmpty()) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
        item(span = { GridItemSpan(2) }) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playlists label on the left
                androidx.compose.material3.Text(
                    text = "Playlists",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                
                // + icon and See all on the right
                androidx.compose.material3.IconButton(
                    onClick = {
                        isCreatingANewPlaylist = true
                        PlaylistTracker.trackCreatePlaylistButtonClicked()
                    }
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.new_playlist)
                    )
                }
                
                androidx.compose.material3.TextButton(
                    onClick = { /* TODO: Navigate to all playlists screen */ }
                ) {
                    androidx.compose.material3.Text(text = "See all")
                }
            }
        }

        // Built-in playlists
        item {
            BuiltInPlaylistItem(
                modifier = Modifier.animateItem(),
                icon = Icons.Filled.Favorite,
                name = stringResource(R.string.favorites),
                onClick = {
                    onBuiltInPlaylist(BuiltInPlaylist.Favorites.ordinal)
                    PlaylistTracker.trackBuiltInPlaylistClicked("favorites")
                }
            )
        }

        item {
            BuiltInPlaylistItem(
                modifier = Modifier.animateItem(),
                icon = Icons.Filled.DownloadForOffline,
                name = stringResource(R.string.offline),
                onClick = {
                    onBuiltInPlaylist(BuiltInPlaylist.Offline.ordinal)
                    PlaylistTracker.trackBuiltInPlaylistClicked("offline")
                }
            )
        }

        // YouTube playlists - removed for faster loading

        // User playlists
        items(
            items = viewModel.items,
            key = { it.playlist.id }
        ) { playlistPreview ->
            LocalPlaylistItem(
                modifier = Modifier.animateItem(),
                playlist = playlistPreview,
                onClick = {
                    onPlaylistClick(playlistPreview.playlist)
                    PlaylistTracker.trackUserPlaylistClicked(playlistPreview.playlist.id.toString())
                }
            )
        }
        }
    }
}