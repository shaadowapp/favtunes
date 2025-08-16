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
import androidx.media3.common.MediaItem
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.suggestion.SimpleSuggestionIntegration
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.items.LocalSongItem
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay
import com.shaadow.tunes.viewmodels.HomeSongsViewModel

/**
 * Personalized recommendations component using the lightweight suggestion system
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PersonalizedRecommendations() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val viewModel: HomeSongsViewModel = viewModel()
    val suggestionIntegration = remember { SimpleSuggestionIntegration.getInstance(context) }
    
    var recommendations by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 8.dp)

    // Load recommendations from suggestion system
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // First try to get recommendations from the suggestion system
            val suggestedItems = suggestionIntegration.getSuggestionSystem().getRecommendations(16)
            
            if (suggestedItems.isNotEmpty()) {
                recommendations = suggestedItems
            } else {
                // Fallback to most played songs if no suggestions available
                viewModel.loadSongs(
                    sortBy = com.shaadow.tunes.enums.SongSortBy.PlayTime,
                    sortOrder = com.shaadow.tunes.enums.SortOrder.Descending
                )
            }
        } catch (e: Exception) {
            // Fallback to viewModel on error
            viewModel.loadSongs(
                sortBy = com.shaadow.tunes.enums.SongSortBy.PlayTime,
                sortOrder = com.shaadow.tunes.enums.SortOrder.Descending
            )
        } finally {
            isLoading = false
        }
    }

    // Show recommendations or fallback to most played songs, ensure at least 8 items
    val itemsToShow = if (recommendations.isNotEmpty()) {
        // If we have recommendations but less than 8, pad with most played songs
        if (recommendations.size < 8) {
            val additionalSongs = viewModel.items
                .take(20)
                .map { it.asMediaItem }
                .filter { mediaItem -> 
                    // Don't duplicate songs already in recommendations
                    recommendations.none { it.mediaId == mediaItem.mediaId }
                }
                .take(8 - recommendations.size)
            recommendations + additionalSongs
        } else {
            recommendations.take(8)
        }
    } else {
        // Fallback to most played songs, ensure we have at least 8
        viewModel.items.take(8).map { it.asMediaItem }
    }

    if (itemsToShow.isNotEmpty() || isLoading) {
        Spacer(modifier = Modifier.height(Dimensions.spacer))
        
        Row(
            modifier = sectionTextModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (recommendations.isNotEmpty()) 
                    stringResource(id = R.string.recommended_for_you)
                else 
                    "Most Played",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = itemsToShow,
                key = { mediaItem -> mediaItem.mediaId }
            ) { mediaItem ->
                // Convert MediaItem back to Song for display if needed
                val song = if (recommendations.isNotEmpty()) {
                    // For suggestion system items, create a minimal song representation
                    viewModel.items.find { it.id == mediaItem.mediaId } ?: run {
                        // Create a minimal song from MediaItem
                        com.shaadow.tunes.models.Song(
                            id = mediaItem.mediaId,
                            title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown Title",
                            artistsText = mediaItem.mediaMetadata.artist?.toString(),
                            durationText = null,
                            thumbnailUrl = mediaItem.mediaMetadata.artworkUri?.toString(),
                            likedAt = null,
                            totalPlayTimeMs = 0
                        )
                    }
                } else {
                    // For fallback items, find the original song
                    viewModel.items.find { it.asMediaItem.mediaId == mediaItem.mediaId }
                }
                
                song?.let { songItem ->
                    LocalSongItem(
                        modifier = Modifier.widthIn(max = 280.dp),
                        song = songItem,
                        onClick = {
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
                                    mediaItem = mediaItem,
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
}