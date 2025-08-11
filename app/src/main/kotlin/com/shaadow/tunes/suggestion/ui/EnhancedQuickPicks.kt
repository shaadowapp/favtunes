package com.shaadow.tunes.suggestion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.shaadow.tunes.LocalPlayerServiceBinder
import com.shaadow.tunes.suggestion.AdvancedSuggestionSystem
import com.shaadow.tunes.suggestion.analytics.SessionContext
import com.shaadow.tunes.ui.components.ShimmerHost
import com.shaadow.tunes.ui.components.themed.TextPlaceholder
import com.shaadow.tunes.utils.forcePlay
import kotlinx.coroutines.launch

/**
 * Enhanced QuickPicks with dynamic personalized sections
 * Drop-in replacement for the existing QuickPicks component
 */
@Composable
fun EnhancedQuickPicks(
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onOfflinePlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val scope = rememberCoroutineScope()
    
    // Initialize suggestion system
    val suggestionSystem = remember { AdvancedSuggestionSystem(context) }
    val suggestionProvider = remember { HomeSuggestionProvider(suggestionSystem) }
    
    var sections by remember { mutableStateOf<List<SuggestionSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Load suggestions
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            error = null
            sections = suggestionProvider.getHomeSections()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }
    
    // Start discovery session
    LaunchedEffect(Unit) {
        suggestionSystem.startListeningSession(SessionContext.DISCOVERY)
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            suggestionSystem.endListeningSession()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                LoadingContent()
            }
            error != null -> {
                ErrorContent(
                    error = error!!,
                    onRetry = {
                        scope.launch {
                            try {
                                isLoading = true
                                error = null
                                sections = suggestionProvider.getHomeSections()
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
            sections.isEmpty() -> {
                EmptyContent()
            }
            else -> {
                SuggestionsContent(
                    sections = sections,
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
                    onSongLongClick = { mediaItem ->
                        // Handle long click (show menu, etc.)
                    }
                )
            }
        }
    }
}

/**
 * Main content with suggestion sections
 */
@Composable
private fun SuggestionsContent(
    sections: List<SuggestionSection>,
    onSongClick: (MediaItem) -> Unit,
    onSongLongClick: (MediaItem) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(
            items = sections,
            key = { "${it.type}_${it.title}" }
        ) { section ->
            when (section.type) {
                SectionType.CONTEXTUAL, SectionType.DISCOVERY -> {
                    // Full-width carousel for main sections
                    SuggestionCarousel(
                        section = section,
                        onSongClick = onSongClick,
                        onSongLongClick = onSongLongClick
                    )
                }
                else -> {
                    // Compact carousel for secondary sections
                    CompactSuggestionCarousel(
                        section = section,
                        onSongClick = onSongClick,
                        onSongLongClick = onSongLongClick
                    )
                }
            }
        }
    }
}

/**
 * Loading state
 */
@Composable
private fun LoadingContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(3) { index ->
            ShimmerHost {
                Column {
                    // Section header placeholder
                    TextPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    TextPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Song items placeholder
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(3) {
                            TextPlaceholder(
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(64.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Error state
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unable to load suggestions",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Empty state
 */
@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No suggestions available",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start listening to music to get personalized recommendations",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}