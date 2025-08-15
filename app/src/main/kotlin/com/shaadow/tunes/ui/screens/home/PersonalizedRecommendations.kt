package com.shaadow.tunes.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.models.Song
import com.shaadow.tunes.suggestion.SimpleSuggestionIntegration
import com.shaadow.tunes.ui.items.LocalSongItem
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.tunes.ui.styling.Dimensions
import com.shaadow.tunes.utils.SnapLayoutInfoProvider
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay
import com.shaadow.tunes.utils.isLandscape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun PersonalizedRecommendations(
    modifier: Modifier = Modifier
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val context = LocalContext.current
    
    var recommendedSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Initialize suggestion system
    val suggestionIntegration = remember { SimpleSuggestionIntegration.getInstance(context) }
    val suggestionSystem = suggestionIntegration.getSuggestionSystem()
    
    // Load recommendations
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            
            // Get recommendations from working system (currently returns empty, but can be enhanced)
            val recommendations = withContext(Dispatchers.IO) {
                suggestionSystem.getRecommendations(16)
            }
            
            // For now, use database songs as fallback since recommendations are empty
            recommendedSongs = Database.songsByRowIdDesc().first()
                .filter { !it.title.isNullOrBlank() && !it.artistsText.isNullOrBlank() }
                .take(16)
            
        } catch (e: Exception) {
            // Fallback to database songs
            recommendedSongs = try {
                Database.songsByRowIdDesc().first()
                    .filter { !it.title.isNullOrBlank() && !it.artistsText.isNullOrBlank() }
                    .take(16)
            } catch (e2: Exception) {
                emptyList()
            }
        } finally {
            isLoading = false
        }
    }
    
    // Only show if we have songs
    if (recommendedSongs.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                text = "Personalized Recommended",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
            
            BoxWithConstraints {
                val quickPicksLazyGridItemWidthFactor =
                    if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f

                val density = LocalDensity.current
                val songThumbnailSizeDp = Dimensions.thumbnails.song
                val personalizedLazyGridState = rememberLazyGridState()

                val snapLayoutInfoProvider = remember(personalizedLazyGridState) {
                    with(density) {
                        SnapLayoutInfoProvider(
                            lazyGridState = personalizedLazyGridState,
                            positionInLayout = { layoutSize, itemSize ->
                                (layoutSize * quickPicksLazyGridItemWidthFactor / 2f - itemSize / 2f)
                            }
                        )
                    }
                }

                val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor
                
                LazyHorizontalGrid(
                    state = personalizedLazyGridState,
                    rows = GridCells.Fixed(count = 4),
                    flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 4)
                ) {
                    items(
                        items = recommendedSongs.take(16),
                        key = { song -> song.id }
                    ) { song ->
                        LocalSongItem(
                            modifier = Modifier
                                .animateItem()
                                .width(itemInHorizontalGridWidth),
                            song = song,
                            onClick = {
                                // Track interaction
                                suggestionSystem.onSongPlayed(song.asMediaItem, 0L)
                                
                                val mediaItem = song.asMediaItem
                                binder?.stopRadio()
                                binder?.player?.forcePlay(mediaItem)
                                binder?.setupRadio(
                                    com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch(
                                        videoId = mediaItem.mediaId
                                    )
                                )
                            },
                            onLongClick = {
                                menuState.display {
                                    NonQueuedMediaItemMenu(
                                        onDismiss = menuState::hide,
                                        mediaItem = song.asMediaItem
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


