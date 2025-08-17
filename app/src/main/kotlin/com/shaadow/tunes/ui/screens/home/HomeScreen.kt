package com.shaadow.tunes.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shaadow.tunes.ui.components.BugReportBottomSheet
import com.shaadow.tunes.ui.components.FeedbackBottomSheet
import com.shaadow.tunes.ui.components.SponsoredAppsBottomSheet
import com.shaadow.tunes.utils.rememberShakeDetector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    var showFeedbackSheet by remember { mutableStateOf(false) }
    var showSponsoredSheet by remember { mutableStateOf(false) }
    var showBugReportSheet by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    // Shake detection for bug reporting
    rememberShakeDetector {
        showBugReportSheet = true
    }

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
                                text = "My Tunes",
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
                        description = R.string.more,
                        onClick = { showSponsoredSheet = true },
                        icon = Icons.Outlined.Apps,
                        inTopBar = true
                    )
                    
                    TooltipIconButton(
                        description = R.string.search,
                        onClick = { navController.navigate(route = "search") },
                        icon = Icons.Outlined.Search,
                        inTopBar = true
                    )

                    Box {
                        TooltipIconButton(
                            description = R.string.more,
                            onClick = { showDropdownMenu = true },
                            icon = Icons.Outlined.MoreVert,
                            inTopBar = true
                        )
                        
                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Settings", style = MaterialTheme.typography.bodyLarge)
                                    }
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    navController.navigate(route = "settings")
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Feedback, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Feedback", style = MaterialTheme.typography.bodyLarge)
                                    }
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    showFeedbackSheet = true
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Report Bug", style = MaterialTheme.typography.bodyLarge)
                                    }
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    showBugReportSheet = true
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Inbox", style = MaterialTheme.typography.bodyLarge)
                                    }
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    navController.navigate("inbox")
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
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
                0 -> QuickPicks(
                    onAlbumClick = { browseId -> navController.navigate(route = "album/$browseId") },
                    onArtistClick = { browseId -> navController.navigate(route = "artist/$browseId") },
                    onPlaylistClick = { browseId -> navController.navigate(route = "playlist/$browseId") },
                    onOfflinePlaylistClick = { navController.navigate(route = "builtInPlaylist/1") }
                )

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

                4 -> com.shaadow.tunes.ui.screens.mytunes.MyTunesScreen()
            }
        }
    }
    
    if (showFeedbackSheet) {
        FeedbackBottomSheet(
            onDismiss = { showFeedbackSheet = false }
        )
    }
    
    if (showSponsoredSheet) {
        SponsoredAppsBottomSheet(
            onDismiss = { showSponsoredSheet = false }
        )
    }
    
    if (showBugReportSheet) {
        BugReportBottomSheet(
            onDismiss = { showBugReportSheet = false }
        )
    }
}