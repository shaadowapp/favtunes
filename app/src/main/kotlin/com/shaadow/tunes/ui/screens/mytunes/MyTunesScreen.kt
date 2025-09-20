package com.shaadow.tunes.ui.screens.mytunes

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.enums.BuiltInPlaylist
import com.shaadow.tunes.enums.PlaylistSortBy
import com.shaadow.tunes.enums.SortOrder

import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.models.Playlist
import com.shaadow.tunes.models.Song
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.components.ScreenIdentifier
import com.shaadow.tunes.ui.components.themed.TextFieldDialog
import com.shaadow.tunes.utils.PlaylistTracker
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.forcePlay
import com.shaadow.tunes.utils.forcePlayAtIndex
import com.shaadow.tunes.utils.forcePlayFromBeginning
import com.shaadow.tunes.utils.playlistSortByKey
import com.shaadow.tunes.utils.playlistSortOrderKey
import com.shaadow.tunes.utils.rememberPreference
import com.shaadow.tunes.utils.thumbnail
import com.shaadow.tunes.viewmodels.HomePlaylistsViewModel
import com.shaadow.tunes.viewmodels.QuickPicksViewModel
import com.shaadow.tunes.enums.QuickPicksSource
import com.shaadow.tunes.ui.items.SongItem
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.ui.components.themed.NonQueuedMediaItemMenu
import com.shaadow.innertube.Innertube
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun MyTunesScreen(
    onGoToAlbum: (String) -> Unit = {},
    onGoToArtist: (String) -> Unit = {},
    onBuiltInPlaylist: (Int) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onSeeAllSongs: (String, String) -> Unit = { _, _ -> } // (screenTitle, sectionType)
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current

    // Existing sections data
    val likedSongs by Database.favorites().collectAsState(initial = emptyList())
    val recentSongs by Database.recentlyPlayedSongs().collectAsState(initial = emptyList())
    val fallbackSongs by Database.songsByRowIdDesc().collectAsState(initial = emptyList())
    
    // Use fallback if no recent songs available
    val displayRecentSongs = if (recentSongs.isEmpty()) fallbackSongs.take(10) else recentSongs.take(10)
    
    // Playlists functionality
    val playlistsViewModel: HomePlaylistsViewModel = viewModel()
    var playlistSortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.Name)
    var playlistSortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Ascending)
    var isCreatingANewPlaylist by remember { mutableStateOf(false) }
    
    LaunchedEffect(playlistSortBy, playlistSortOrder) {
        playlistsViewModel.loadArtists(playlistSortBy, playlistSortOrder)
    }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Screen identifier for accurate screen detection
    ScreenIdentifier(
        screenId = "mytunes",
        screenName = "My Tunes Screen"
    )
    
    // New playlist dialog
    if (isCreatingANewPlaylist) {
        TextFieldDialog(
            title = stringResource(id = R.string.new_playlist),
            hintText = stringResource(id = R.string.playlist_name_hint),
            onDismiss = {
                isCreatingANewPlaylist = false
            },
            onDone = { text ->
                scope.launch {
                    query {
                        Database.insert(Playlist(name = text))
                    }
                    PlaylistTracker.trackPlaylistCreated(text)
                    // Refresh playlists after creation
                    playlistsViewModel.loadArtists(playlistSortBy, playlistSortOrder)
                }
                isCreatingANewPlaylist = false
            }
        )
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },

        contentWindowInsets = WindowInsets(bottom = 0)
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 400.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + playerPadding
            ),
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recently Played Songs Section (max 10 songs)
            item(
                key = "recent_songs",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Played",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = {
                                onSeeAllSongs("Recently Played", "recent")
                            }
                        ) {
                            Text("See All")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (displayRecentSongs.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayRecentSongs.take(10)) { song ->
                                Card(
                                    modifier = Modifier.width(120.dp),
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(
                                            displayRecentSongs.map(Song::asMediaItem),
                                            displayRecentSongs.indexOf(song)
                                        )
                                    }
                                ) {
                                    Column {
                                        AsyncImage(
                                            model = song.thumbnailUrl?.thumbnail(80),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Column(
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = song.artistsText ?: "Unknown Artist",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No recent songs yet - play some music!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Vertical spacing
            item(
                key = "spacing_1",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Liked Songs Section (max 10 songs)
            item(
                key = "liked_songs",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Liked Songs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = {
                                onSeeAllSongs("Liked Songs", "favorites")
                            }
                        ) {
                            Text("See All")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (likedSongs.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(likedSongs.take(10)) { song ->
                                Card(
                                    modifier = Modifier.width(120.dp),
                                    onClick = {
                                        binder?.stopRadio()
                                        binder?.player?.forcePlayAtIndex(
                                            likedSongs.map(Song::asMediaItem),
                                            likedSongs.indexOf(song)
                                        )
                                    }
                                ) {
                                    Column {
                                        AsyncImage(
                                            model = song.thumbnailUrl?.thumbnail(80),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Column(
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = song.artistsText ?: "Unknown Artist",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No liked songs yet - heart some songs!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Vertical spacing
            item(
                key = "spacing_2",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Playlists Section
            item(
                key = "playlists",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playlists",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    isCreatingANewPlaylist = true
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Create Playlist",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            TextButton(
                                onClick = {
                                    onSeeAllSongs("Playlists", "playlists")
                                }
                            ) {
                                Text("See All")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // All playlists (built-in + custom) in square box style, horizontally scrollable
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Favorites square box
                        item {
                            Card(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                onClick = {
                                    onBuiltInPlaylist(BuiltInPlaylist.Favorites.ordinal)
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Outlined.Favorite,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Favorites",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        // Offline square box
                        item {
                            Card(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                onClick = {
                                    onBuiltInPlaylist(BuiltInPlaylist.Offline.ordinal)
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Filled.DownloadForOffline,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.offline),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        // Custom playlists
                        items(playlistsViewModel.items) { playlistPreview ->
                            Card(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                onClick = {
                                    onPlaylistClick(Playlist(id = playlistPreview.id, name = playlistPreview.name))
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Outlined.QueueMusic,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = playlistPreview.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    

                }
            }

            // Recommended for You section (shown when playlists are empty)
            if (likedSongs.isEmpty() && playlistsViewModel.items.isEmpty()) {
                item(
                    key = "empty_state_spacing",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item(
                    key = "recommended_section",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    RecommendedForYouSection(
                        onGoToAlbum = onGoToAlbum,
                        onGoToArtist = onGoToArtist
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
private fun RecommendedForYouSection(
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val context = LocalContext.current
    val quickPicksViewModel: QuickPicksViewModel = viewModel()
    
    LaunchedEffect(Unit) {
        quickPicksViewModel.initialize(context)
    }
    
    Column {
        Text(
            text = "No playlists yet - here are some songs you might like",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Recommended for You",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        quickPicksViewModel.relatedPageResult?.getOrNull()?.let { related ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(related.songs?.take(10) ?: emptyList()) { song ->
                    SongItem(
                        song = song,
                        modifier = Modifier.width(250.dp),
                        onClick = {
                            binder?.stopRadio()
                            binder?.player?.forcePlay(song.asMediaItem)
                        },
                        onLongClick = {
                            menuState.display {
                                NonQueuedMediaItemMenu(
                                    mediaItem = song.asMediaItem,
                                    onDismiss = menuState::hide,
                                    onGoToAlbum = onGoToAlbum,
                                    onGoToArtist = onGoToArtist
                                )
                            }
                        }
                    )
                }
            }
        } ?: run {
            // Loading state
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) {
                    Card(
                        modifier = Modifier
                            .width(250.dp)
                            .height(64.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Loading...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
