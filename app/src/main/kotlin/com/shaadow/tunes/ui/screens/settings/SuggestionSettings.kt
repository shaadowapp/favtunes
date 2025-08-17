package com.shaadow.tunes.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.suggestion.SimpleSuggestionIntegration
import kotlinx.coroutines.launch

/**
 * Interactive suggestion settings screen for the lightweight system
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionSettings(paddingValues: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()) {
    val context = LocalContext.current
    val suggestionIntegration = remember { SimpleSuggestionIntegration.getInstance(context) }
    val suggestionSystem = suggestionIntegration.getSuggestionSystem()
    val scope = rememberCoroutineScope()
    
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
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Music Suggestions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "System Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Onboarding Complete:")
                        Text(
                            text = if (suggestionSystem.isOnboardingComplete()) "Yes" else "No",
                            color = if (suggestionSystem.isOnboardingComplete()) 
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tracked Songs:")
                        Text("${trackingStats["totalTrackedSongs"] ?: 0}")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Liked Songs:")
                        Text("${trackingStats["totalLikedSongs"] ?: 0}")
                    }
                }
            }
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (currentPreferences.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentPreferences.toList()) { preference ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(preference) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = {
                                // Reset onboarding to allow re-selection
                                scope.launch {
                                    suggestionSystem.clearAllData()
                                    currentPreferences = emptySet()
                                    trackingStats = suggestionSystem.getTrackingStatus()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Preferences")
                        }
                    } else {
                        Text(
                            text = "No preferences set. Complete onboarding to set your preferences.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Manage your suggestion data and preferences",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val testResults = suggestionSystem.testRecommendationSystem()
                                    // Update tracking stats to show test results
                                    trackingStats = trackingStats + ("testResults" to testResults)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test Recommendation System")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    suggestionSystem.resetOnboarding()
                                    trackingStats = suggestionSystem.getTrackingStatus()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset Onboarding (for testing)")
                        }
                        
                        OutlinedButton(
                            onClick = { showClearDataDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Data")
                        }
                    }
                }
            }
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How It Works",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Tracks your listening behavior (plays, likes, skips)\n" +
                                "• Uses 80% preferences + 20% behavior for recommendations\n" +
                                "• Falls back to YouTube's system when needed\n" +
                                "• All data stored locally on your device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Clear data confirmation dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("This will remove all your preferences and listening history. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            suggestionSystem.clearAllData()
                            currentPreferences = emptySet()
                            trackingStats = suggestionSystem.getTrackingStatus()
                            showClearDataDialog = false
                        }
                    }
                ) {
                    Text("Clear")
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