package com.shaadow.tunes.suggestion.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.utils.forcePlay

/**
 * Integration helper for replacing existing home screen components
 * Provides both enhanced and fallback modes
 */
object HomeScreenIntegration {
    
    /**
     * Enhanced home screen with dynamic suggestions
     * Drop-in replacement for QuickPicks
     */
    @Composable
    fun EnhancedHomeScreen(
        onAlbumClick: (String) -> Unit,
        onArtistClick: (String) -> Unit,
        onPlaylistClick: (String) -> Unit,
        onOfflinePlaylistClick: () -> Unit,
        modifier: Modifier = Modifier,
        useEnhancedMode: Boolean = true
    ) {
        if (useEnhancedMode) {
            // Use the new dynamic suggestion system
            EnhancedQuickPicks(
                onAlbumClick = onAlbumClick,
                onArtistClick = onArtistClick,
                onPlaylistClick = onPlaylistClick,
                onOfflinePlaylistClick = onOfflinePlaylistClick,
                modifier = modifier
            )
        } else {
            // Fallback to original QuickPicks
            // This would call the original QuickPicks component
            // QuickPicks(onAlbumClick, onArtistClick, onPlaylistClick, onOfflinePlaylistClick)
        }
    }
    
    /**
     * ViewModel-based enhanced home screen
     * For more advanced state management
     */
    @Composable
    fun ViewModelBasedHomeScreen(
        onAlbumClick: (String) -> Unit,
        onArtistClick: (String) -> Unit,
        onPlaylistClick: (String) -> Unit,
        onOfflinePlaylistClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val viewModel: DynamicHomeViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsState()
        val binder = LocalPlayerServiceBinder.current
        
        DynamicHomeContent(
            uiState = uiState,
            onSongClick = { mediaItem ->
                binder?.let { serviceBinder ->
                    serviceBinder.stopRadio()
                    serviceBinder.player.forcePlay(mediaItem)
                    serviceBinder.setupRadio(
                        com.shaadow.innertube.models.NavigationEndpoint.Endpoint.Watch(
                            videoId = mediaItem.mediaId
                        )
                    )
                }
            },
            onRefresh = viewModel::refreshSuggestions,
            modifier = modifier
        )
    }
}

/**
 * Content component for ViewModel-based approach
 */
@Composable
private fun DynamicHomeContent(
    uiState: DynamicHomeUiState,
    onSongClick: (MediaItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simplified implementation - use EnhancedQuickPicks for now
    EnhancedQuickPicks(
        onAlbumClick = { /* Not used in this context */ },
        onArtistClick = { /* Not used in this context */ },
        onPlaylistClick = { /* Not used in this context */ },
        onOfflinePlaylistClick = { /* Not used in this context */ },
        modifier = modifier
    )
}

/**
 * Simple integration for existing HomeScreen
 * Minimal changes required to existing code
 */
@Composable
fun IntegrateWithExistingHomeScreen(
    screenIndex: Int,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onOfflinePlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (screenIndex) {
        0 -> {
            // Replace QuickPicks with EnhancedQuickPicks
            HomeScreenIntegration.EnhancedHomeScreen(
                onAlbumClick = onAlbumClick,
                onArtistClick = onArtistClick,
                onPlaylistClick = onPlaylistClick,
                onOfflinePlaylistClick = onOfflinePlaylistClick,
                modifier = modifier
            )
        }
        // Other screen indices remain unchanged
        // 1 -> HomeSongs(...)
        // 2 -> HomeArtistList(...)
        // etc.
    }
}