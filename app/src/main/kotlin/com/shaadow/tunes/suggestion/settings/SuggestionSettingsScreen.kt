package com.shaadow.tunes.suggestion.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.suggestion.AdvancedSuggestionSystem
import com.shaadow.tunes.suggestion.onboarding.OnboardingManager
import kotlinx.coroutines.launch

/**
 * Settings screen for managing suggestion preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val suggestionSystem = remember { AdvancedSuggestionSystem(context) }
    val onboardingManager = remember { OnboardingManager(context) }
    
    var analytics by remember { mutableStateOf(suggestionSystem.getListeningAnalytics()) }
    var showResetDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Refresh analytics
    LaunchedEffect(Unit) {
        analytics = suggestionSystem.getListeningAnalytics()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suggestion Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                suggestionSystem.performLearningUpdate()
                                analytics = suggestionSystem.getListeningAnalytics()
                                isLoading = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Analytics Overview
            item {
                AnalyticsOverviewCard(analytics = analytics)
            }
            
            // Genre Preferences
            item {
                GenrePreferencesCard(
                    preferences = analytics.preferredGenres,
                    onUpdatePreferences = { /* TODO: Implement genre preference updates */ }
                )
            }
            
            // Listening Patterns
            item {
                ListeningPatternsCard(patterns = analytics.patterns)
            }
            
            // System Controls
            item {
                SystemControlsCard(
                    onResetData = { showResetDialog = true },
                    onRunLearningUpdate = {
                        scope.launch {
                            isLoading = true
                            suggestionSystem.performLearningUpdate()
                            analytics = suggestionSystem.getListeningAnalytics()
                            isLoading = false
                        }
                    },
                    isLoading = isLoading
                )
            }
            
            // Advanced Settings
            item {
                AdvancedSettingsCard(
                    onExportData = { /* TODO: Implement data export */ },
                    onImportData = { /* TODO: Implement data import */ }
                )
            }
        }
    }
    
    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Suggestion Data") },
            text = { 
                Text("This will delete all your listening history and preferences. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            suggestionSystem.cleanupOldData()
                            analytics = suggestionSystem.getListeningAnalytics()
                            showResetDialog = false
                        }
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Analytics overview card
 */
@Composable
private fun AnalyticsOverviewCard(analytics: com.shaadow.tunes.suggestion.ListeningAnalytics) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Your Music Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Songs Played",
                    value = analytics.totalSongsPlayed.toString()
                )
                
                StatItem(
                    label = "Liked Songs",
                    value = analytics.totalLikedSongs.toString()
                )
                
                StatItem(
                    label = "Avg Session",
                    value = "${analytics.patterns.averageSessionDuration / (60 * 1000)}m"
                )
            }
            
            analytics.currentSession?.let { session ->
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Current Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Duration: ${session.duration / (60 * 1000)}m")
                    Text("Tracks: ${session.tracksPlayed}")
                    Text("Completion: ${(session.completionRate * 100).toInt()}%")
                }
            }
        }
    }
}

/**
 * Individual stat item
 */
@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Genre preferences card
 */
@Composable
private fun GenrePreferencesCard(
    preferences: Map<String, Float>,
    onUpdatePreferences: (Map<String, Float>) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Genre Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (preferences.isEmpty()) {
                Text(
                    text = "No genre preferences yet. Start listening to build your profile!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                preferences.entries.take(5).forEach { (genre, weight) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        LinearProgressIndicator(
                            progress = { weight },
                            modifier = Modifier
                                .width(100.dp)
                                .padding(horizontal = 8.dp),
                        )
                        
                        Text(
                            text = "${(weight * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Listening patterns card
 */
@Composable
private fun ListeningPatternsCard(patterns: com.shaadow.tunes.suggestion.analytics.ListeningPatterns) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Listening Patterns",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PatternItem(
                label = "Skip Rate",
                value = "${(patterns.skipPatterns.overallSkipRate * 100).toInt()}%",
                description = if (patterns.skipPatterns.isImpatientListener) "Quick skipper" else "Patient listener"
            )
            
            PatternItem(
                label = "Session Length",
                value = patterns.preferredSessionLength.name.lowercase().replaceFirstChar { it.uppercase() },
                description = "Average listening duration preference"
            )
            
            PatternItem(
                label = "Tracks per Session",
                value = patterns.averageTracksPerSession.toInt().toString(),
                description = "How many songs you typically play"
            )
        }
    }
}

/**
 * Individual pattern item
 */
@Composable
private fun PatternItem(
    label: String,
    value: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * System controls card
 */
@Composable
private fun SystemControlsCard(
    onResetData: () -> Unit,
    onRunLearningUpdate: () -> Unit,
    isLoading: Boolean
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "System Controls",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRunLearningUpdate,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Update Recommendations")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onResetData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Data")
            }
        }
    }
}

/**
 * Advanced settings card
 */
@Composable
private fun AdvancedSettingsCard(
    onExportData: () -> Unit,
    onImportData: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onExportData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Listening Data")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onImportData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Listening Data")
            }
        }
    }
}