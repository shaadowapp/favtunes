package com.shaadow.tunes.models

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.shaadow.tunes.R

sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        resourceId = R.string.home,
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )

    data object Songs : Screen(
        route = "songs",
        resourceId = R.string.songs,
        unselectedIcon = Icons.Outlined.MusicNote,
        selectedIcon = Icons.Filled.MusicNote
    )

    data object Explore : Screen(
        route = "explore",
        resourceId = R.string.explore_search,
        unselectedIcon = Icons.Outlined.Explore,
        selectedIcon = Icons.Filled.Explore
    )

    data object Playlists : Screen(
        route = "playlists",
        resourceId = R.string.playlists,
        unselectedIcon = Icons.AutoMirrored.Outlined.QueueMusic,
        selectedIcon = Icons.AutoMirrored.Filled.QueueMusic
    )

    data object Artists : Screen(
        route = "artists",
        resourceId = R.string.artists,
        unselectedIcon = Icons.Outlined.PersonSearch,
        selectedIcon = Icons.Filled.PersonSearch
    )
}