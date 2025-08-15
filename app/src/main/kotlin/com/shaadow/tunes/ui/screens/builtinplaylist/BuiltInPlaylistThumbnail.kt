package com.shaadow.tunes.ui.screens.builtinplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.enums.BuiltInPlaylist

@Composable
fun BuiltInPlaylistThumbnail(builtInPlaylist: BuiltInPlaylist) {
    Box(
        modifier = Modifier
            .padding(all = 16.dp)
            .clip(MaterialTheme.shapes.large)
            .size(120.dp) // Fixed size instead of calculated size
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (builtInPlaylist) {
                BuiltInPlaylist.Favorites -> Icons.Outlined.Bookmark
                BuiltInPlaylist.Offline -> Icons.Default.DownloadForOffline
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(60.dp) // Half of the box size
        )
    }
}