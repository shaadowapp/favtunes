package com.shaadow.tunes.ui.screens.mytunes

import androidx.annotation.OptIn
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.shaadow.tunes.Database
import com.shaadow.tunes.LocalPlayerPadding
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.R
import com.shaadow.tunes.models.ActionInfo
import com.shaadow.tunes.models.LocalMenuState
import com.shaadow.tunes.models.Song
import com.shaadow.tunes.query
import com.shaadow.tunes.ui.components.ScreenIdentifier
import com.shaadow.tunes.ui.components.SwipeToActionBox
import com.shaadow.tunes.ui.components.themed.InHistoryMediaItemMenu
import com.shaadow.tunes.ui.items.LocalSongItem
import com.shaadow.tunes.utils.asMediaItem
import com.shaadow.tunes.utils.enqueue
import com.shaadow.tunes.utils.forcePlayAtIndex
import com.shaadow.tunes.utils.forcePlayFromBeginning
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun LikedSongsScreen(
    onGoToAlbum: (String) -> Unit = {},
    onGoToArtist: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val likedSongs by Database.favorites().collectAsState(initial = emptyList())
    
    val snackbarMessage = stringResource(id = R.string.song_deleted_library)
    val snackBarActionLabel = stringResource(id = R.string.undo)
    
    ScreenIdentifier(
        screenId = "liked_songs",
        screenName = "Liked Songs Screen"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Liked Songs",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (likedSongs.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        binder?.stopRadio()
                        binder?.player?.forcePlayFromBeginning(
                            likedSongs.shuffled().map(Song::asMediaItem)
                        )
                    },
                    modifier = Modifier.padding(bottom = playerPadding)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shuffle,
                        contentDescription = stringResource(id = R.string.shuffle)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (likedSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No liked songs yet - heart some songs!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + playerPadding
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = likedSongs,
                    key = { _, song -> "song_${song.id}" }
                ) { index, song ->
                    SwipeToActionBox(
                        modifier = Modifier.animateItem(),
                        state = rememberSwipeToDismissBoxState(),
                        primaryAction = ActionInfo(
                            onClick = {
                                binder?.player?.enqueue(song.asMediaItem)
                            },
                            icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                            description = R.string.enqueue
                        ),
                        destructiveAction = ActionInfo(
                            onClick = {
                                query {
                                    binder?.cache?.removeResource(song.id)
                                    Database.incrementTotalPlayTimeMs(
                                        id = song.id,
                                        addition = -song.totalPlayTimeMs
                                    )
                                }

                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = snackbarMessage,
                                        actionLabel = snackBarActionLabel,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        query {
                                            Database.insert(song)
                                            Database.incrementTotalPlayTimeMs(
                                                id = song.id,
                                                addition = song.totalPlayTimeMs
                                            )
                                        }
                                    }
                                }
                            },
                            icon = Icons.Outlined.Delete,
                            description = R.string.delete
                        )
                    ) {
                        LocalSongItem(
                            song = song,
                            onClick = {
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(
                                    likedSongs.map(Song::asMediaItem),
                                    index
                                )
                            },
                            onLongClick = {
                                menuState.display {
                                    InHistoryMediaItemMenu(
                                        song = song,
                                        onDismiss = menuState::hide,
                                        onGoToAlbum = onGoToAlbum,
                                        onGoToArtist = onGoToArtist
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