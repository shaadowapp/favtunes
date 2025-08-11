package com.shaadow.tunes.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.shaadow.tunes.R
import com.shaadow.tunes.ui.components.AppIcon
import com.shaadow.tunes.ui.components.TooltipIconButton


@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun HomeScreen(
    navController: NavController,
    screenIndex: Int
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    when (screenIndex) {
                        0 -> {
                            val context = LocalContext.current
                            AppIcon(context)
                        }
                        1 -> {
                            Text(
                                text = "Songs",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        4 -> {
                            Text(
                                text = "Playlists",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        else -> {
                            val context = LocalContext.current
                            AppIcon(context)
                        }
                    }
                },
                actions = {
                    TooltipIconButton(
                        description = R.string.search,
                        onClick = { navController.navigate(route = "search") },
                        icon = Icons.Outlined.Search,
                        inTopBar = true
                    )

                    TooltipIconButton(
                        description = R.string.settings,
                        onClick = { navController.navigate(route = "settings") },
                        icon = Icons.Outlined.Settings,
                        inTopBar = true
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when (screenIndex) {
                0 -> {
                    // Use enhanced home screen with suggestions
                    com.shaadow.tunes.suggestion.ui.HomeScreenIntegration.EnhancedHomeScreen(
                        onAlbumClick = { browseId -> navController.navigate(route = "album/$browseId") },
                        onArtistClick = { browseId -> navController.navigate(route = "artist/$browseId") },
                        onPlaylistClick = { browseId -> navController.navigate(route = "playlist/$browseId") },
                        onOfflinePlaylistClick = { navController.navigate(route = "builtInPlaylist/1") }
                    )
                }

                1 -> HomeSongs(
                    onGoToAlbum = { browseId -> navController.navigate(route = "album/$browseId") },
                    onGoToArtist = { browseId -> navController.navigate(route = "artist/$browseId") }
                )

                2 -> HomeArtistList(
                    onArtistClick = { artist -> navController.navigate(route = "artist/${artist.id}") }
                )

                3 -> HomeAlbums(
                    onAlbumClick = { album -> navController.navigate(route = "album/${album.id}") }
                )

                4 -> HomePlaylists(
                    onBuiltInPlaylist = { playlistIndex -> navController.navigate(route = "builtInPlaylist/$playlistIndex") },
                    onPlaylistClick = { playlist -> navController.navigate(route = "localPlaylist/${playlist.id}") },
                    onYouTubePlaylistClick = { browseId -> navController.navigate(route = "playlist/$browseId") }
                )
            }
        }
    }
}