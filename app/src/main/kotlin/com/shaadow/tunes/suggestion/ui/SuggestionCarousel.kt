package com.shaadow.tunes.suggestion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.shaadow.tunes.ui.items.SongItem
import com.shaadow.tunes.ui.styling.Dimensions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card

/**
 * Horizontal carousel for displaying suggestion sections
 */
@Composable
fun SuggestionCarousel(
    section: SuggestionSection,
    onSongClick: (MediaItem) -> Unit,
    onSongLongClick: (MediaItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section header
        SectionHeader(
            title = section.title,
            subtitle = section.subtitle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Horizontal song list
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = section.songs,
                key = { it.mediaId }
            ) { mediaItem ->
                SuggestionSongItem(
                    mediaItem = mediaItem,
                    onClick = { onSongClick(mediaItem) },
                    onLongClick = { onSongLongClick(mediaItem) }
                )
            }
        }
    }
}

/**
 * Header for suggestion sections
 */
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Individual song item in the carousel
 */
@Composable
private fun SuggestionSongItem(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use the existing SongItem component with fixed width
    Box(
        modifier = modifier.width(280.dp)
    ) {
        // Use a simplified song item for MediaItem
        MediaItemCard(
            mediaItem = mediaItem,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Compact version for smaller sections
 */
@Composable
fun CompactSuggestionCarousel(
    section: SuggestionSection,
    onSongClick: (MediaItem) -> Unit,
    onSongLongClick: (MediaItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Compact header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (section.subtitle.isNotEmpty()) {
                    Text(
                        text = section.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Optional "View All" button could go here
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Compact horizontal list
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = section.songs.take(8), // Limit for compact view
                key = { it.mediaId }
            ) { mediaItem ->
                Box(modifier = Modifier.width(240.dp)) {
                    MediaItemCard(
                        mediaItem = mediaItem,
                        onClick = { onSongClick(mediaItem) },
                        onLongClick = { onSongLongClick(mediaItem) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
/**
 * 
Simple MediaItem card component
 */
@Composable
private fun MediaItemCard(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = mediaItem.mediaMetadata.title?.toString() ?: "Unknown Title",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}