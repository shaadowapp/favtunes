package com.shaadow.tunes.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.Database
import com.shaadow.tunes.suggestion.SimpleSuggestionIntegration
import com.shaadow.tunes.data.OnboardingData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionSettings(paddingValues: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()) {
    val context = LocalContext.current
    val suggestionIntegration = remember { SimpleSuggestionIntegration.getInstance(context) }
    val suggestionSystem = suggestionIntegration.getSuggestionSystem()
    val scope = rememberCoroutineScope()
    
    // Get actual user data from Room database
    val totalSongs by Database.songs(com.shaadow.tunes.enums.SongSortBy.DateAdded, com.shaadow.tunes.enums.SortOrder.Descending).collectAsState(initial = emptyList())
    val likedSongs by Database.favorites().collectAsState(initial = emptyList())
    val recentSongs by Database.recentlyPlayedSongs().collectAsState(initial = emptyList())
    
    var currentPreferences by remember { mutableStateOf(setOf<String>()) }
    var trackingStats by remember { mutableStateOf(mapOf<String, Any>()) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    // Load current data
    LaunchedEffect(Unit) {
        currentPreferences = suggestionSystem.getUserPreferences() ?: emptySet()
        trackingStats = suggestionSystem.getTrackingStatus()
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        
        // User Music Statistics from Database
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Music Library",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${totalSongs.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Total Songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column {
                            Text(
                                text = "${likedSongs.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Liked Songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column {
                            Text(
                                text = "${recentSongs.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Recently Played",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        

        
        // Music Preferences
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Music Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Show all available genres with current preferences highlighted
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(OnboardingData.genres.take(15)) { genre ->
                            val isSelected = currentPreferences.contains(genre.name)
                            FilterChip(
                                onClick = { },
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(genre.emoji)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(genre.name)
                                    }
                                },
                                selected = isSelected
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                suggestionSystem.resetOnboarding()
                                trackingStats = suggestionSystem.getTrackingStatus()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Music Preferences")
                    }
                }
            }
        }
        
        // Advanced Settings
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Advanced Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    // Refresh recommendations
                                    trackingStats = suggestionSystem.getTrackingStatus()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh Recommendations")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    // Export listening data
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Listening Data")
                        }
                        
                        OutlinedButton(
                            onClick = { showClearDataDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset All Data")
                        }
                    }
                }
            }
        }
        
        // Listening Insights from Database
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Listening Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (recentSongs.isNotEmpty()) {
                        Text(
                            text = "Most Recent Artists",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recentSongs.take(10).mapNotNull { it.artistsText }.distinct().take(5)) { artist ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(artist) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Start listening to music to see your insights here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Reset data confirmation dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Reset All Data?") },
            text = { 
                Text("This will remove all your music preferences, listening history, and suggestion data. You'll need to set up your preferences again.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            suggestionSystem.clearAllData()
                            currentPreferences = emptySet()
                            trackingStats = suggestionSystem.getTrackingStatus()
                            showClearDataDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}