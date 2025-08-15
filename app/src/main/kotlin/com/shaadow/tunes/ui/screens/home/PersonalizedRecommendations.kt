package com.shaadow.tunes.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.items.LocalSongItem
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay
import com.shaadow.tunes.viewmodels.HomeSongsViewModel

/**
 * Simple personalized recommendations component using the lightweight suggestion system
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PersonalizedRecommendations() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val viewModel: HomeSongsViewModel = viewModel()
    
    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 8.dp)

    // Load songs for recommendations
    LaunchedEffect(Unit) {
        viewModel.loadSongs(
            sortBy = com.shaadow.tunes.enums.SongSortBy.PlayTime,
            sortOrder = com.shaadow.tunes.enums.SortOrder.Descending
        )
    }

    if (viewModel.items.isNotEmpty()) {
        Spacer(modifier = Modifier.height(Dimensions.spacer))
        
        Text(
            text = stringResource(id = R.string.recommended_for_you),
            style = MaterialTheme.typography.titleMedium,
            modifier = sectionTextModifier
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = viewModel.items.take(10), // Show top 10 most played songs
                key = { song -> song.id }
            ) { song ->
                LocalSongItem(
                    modifier = Modifier.widthIn(max = 280.dp),
                    song = song,
                    onClick = {
                        val mediaItem = song.asMediaItem
                        binder?.stopRadio()
                        binder?.player?.forcePlay(mediaItem)
                        binder?.setupRadio(
                            com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                        )
                    },
                    onLongClick = {
                        menuState.display {
                            NonQueuedMediaItemMenu(
                                onDismiss = menuState::hide,
                                mediaItem = song.asMediaItem,
                                onGoToAlbum = { },
                                onGoToArtist = { }
                            )
                        }
                    }
                )
            }
        }
    }
}