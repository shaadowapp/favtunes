package com.shaadow.tunes.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shaadow.tunes.Database
import com.shaadow.tunes.suggestion.SimpleSuggestionIntegration
import com.shaadow.tunes.ui.components.ScreenIdentifier
import com.shaadow.tunes.ui.components.ListeningHabitsChart
import com.shaadow.tunes.ui.components.GenreDistributionChart
import com.shaadow.tunes.ui.components.WeeklyActivityChart
import com.shaadow.tunes.ui.components.MusicMoodChart
import com.shaadow.tunes.data.OnboardingData
import com.shaadow.tunes.utils.DataExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionSettings(paddingValues: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()) {
    // Screen identifier for accurate screen detection
    ScreenIdentifier(
        screenId = "suggestion_settings",
        screenName = "Suggestion Settings"
    )
    
    val context = LocalContext.current
    val suggestionIntegration = remember { SimpleSuggestionIntegration.getInstance(context) }
    val suggestionSystem = suggestionIntegration.getSuggestionSystem()
    val scope = rememberCoroutineScope()
    
    // Get actual user data from Room database
    val totalSongs by Database.songs(com.shaadow.tunes.enums.SongSortBy.DateAdded, com.shaadow.tunes.enums.SortOrder.Descending).collectAsState(initial = emptyList())
    val likedSongs by Database.favorites().collectAsState(initial = emptyList())
    val recentSongs by Database.recentlyPlayedSongs().collectAsState(initial = emptyList())
    val allEvents by Database.eventsCount().collectAsState(initial = 0)
    
    var currentPreferences by remember { mutableStateOf(setOf<String>()) }
    var trackingStats by remember { mutableStateOf(mapOf<String, Any>()) }
    var showPreferencesDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
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
        
        // Enhanced Music Library Statistics with Charts
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Statistics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(
                            value = "${totalSongs.size}",
                            label = "Total Songs",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatCard(
                            value = "${likedSongs.size}",
                            label = "Liked Songs",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatCard(
                            value = "${recentSongs.size}",
                            label = "Recently Played",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Listening Activity Chart
                    if (allEvents > 0) {
                        Text(
                            text = "Listening Activity",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ListeningActivityChart(
                            totalEvents = allEvents,
                            likedCount = likedSongs.size,
                            recentCount = recentSongs.size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            }
        }
        
        // Music Preferences with Real Data
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Music Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        IconButton(
                            onClick = { showPreferencesDialog = true }
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Edit Preferences"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (currentPreferences.isNotEmpty()) {
                        // Show user's actual preferences
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentPreferences.toList()) { preference ->
                                val genre = OnboardingData.genres.find { it.name == preference }
                                FilterChip(
                                    onClick = { },
                                    label = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(genre?.emoji ?: "🎵")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(preference)
                                        }
                                    },
                                    selected = true
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No preferences set. Tap the edit button to set your music preferences.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { showPreferencesDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Music Preferences")
                    }
                }
            }
        }        
 
       // Advanced Settings with Real Functionality
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
                                    isRefreshing = true
                                    try {
                                        // Refresh recommendations by clearing cache and reloading
                                        trackingStats = suggestionSystem.getTrackingStatus()
                                        currentPreferences = suggestionSystem.getUserPreferences() ?: emptySet()
                                    } finally {
                                        isRefreshing = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Outlined.Refresh, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh Recommendations")
                        }
                        
                        OutlinedButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Listening Data")
                        }
                    }
                }
            }
        }
        
        // Music Analytics Dashboard
        item {
            Text(
                text = "Music Analytics Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        
        // Listening Habits Chart
        item {
            ListeningHabitsChart(
                likedSongs = likedSongs.size,
                totalSongs = totalSongs.size,
                recentSongs = recentSongs.size,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Genre Distribution and Weekly Activity
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GenreDistributionChart(
                    preferences = currentPreferences,
                    modifier = Modifier.weight(1f)
                )
                
                WeeklyActivityChart(
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Music Mood Analysis
        item {
            MusicMoodChart(
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Top Artists Analysis
        if (recentSongs.isNotEmpty() || likedSongs.isNotEmpty()) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Top Artists",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val topArtists = (recentSongs + likedSongs)
                            .mapNotNull { it.artistsText }
                            .flatMap { it.split(",", "&", "feat.", "ft.") }
                            .map { it.trim() }
                            .groupBy { it }
                            .mapValues { it.value.size }
                            .toList()
                            .sortedByDescending { it.second }
                            .take(5)
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(topArtists) { (artist, count) ->
                                AssistChip(
                                    onClick = { },
                                    label = { 
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = artist,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "$count plays",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
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
                    }
                }
            }
        }
        
        // Recommendation Engine Status with Security
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Security Status Variables
                    val integrityData = trackingStats["dataIntegrity"] as? Map<String, Any>
                    val integrityScore = integrityData?.get("integrityScore") as? Float ?: 1.0f
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommendation Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Security Status Indicator
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (integrityScore >= 0.9f) Icons.Outlined.Security else Icons.Outlined.Warning,
                                contentDescription = "Security Status",
                                tint = if (integrityScore >= 0.9f) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Protected",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (integrityScore >= 0.9f) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Engine Status",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (trackingStats["isTrackingActive"] == true) "Active" else "Inactive",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (trackingStats["isTrackingActive"] == true) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tracked Songs",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${trackingStats["totalTrackedSongs"] ?: 0}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Data Points",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$allEvents",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LinearProgressIndicator(
                        progress = { 
                            val tracked = trackingStats["totalTrackedSongs"] as? Int ?: 0
                            val total = totalSongs.size
                            if (total > 0) tracked.toFloat() / total.toFloat() else 0f
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "The recommendation engine learns from your listening habits to suggest better music. Data is protected against manipulation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Data Integrity Information
                    if (integrityData != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Data Integrity: ${(integrityScore * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "${integrityData["validEntries"]}/${integrityData["totalEntries"]} entries valid",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Music Preferences Update Dialog
    if (showPreferencesDialog) {
        MusicPreferencesDialog(
            currentPreferences = currentPreferences,
            onDismiss = { showPreferencesDialog = false },
            onSave = { newPreferences ->
                scope.launch {
                    suggestionSystem.updatePreferences(newPreferences.toList())
                    currentPreferences = newPreferences
                    showPreferencesDialog = false
                }
            }
        )
    }
    
    // Export Data Dialog
    if (showExportDialog) {
        ExportDataDialog(
            totalSongs = totalSongs.size,
            likedSongs = likedSongs.size,
            recentSongs = recentSongs.size,
            totalEvents = allEvents,
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                scope.launch {
                    val shareIntent = DataExporter.exportListeningData(
                        context = context,
                        format = format,
                        preferences = currentPreferences
                    )
                    shareIntent?.let { 
                        context.startActivity(Intent.createChooser(it, "Share Listening Data"))
                    }
                    showExportDialog = false
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ListeningActivityChart(
    totalEvents: Int,
    likedCount: Int,
    recentCount: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / 7f
        val maxValue = maxOf(totalEvents, likedCount * 10, recentCount * 5).toFloat()
        
        if (maxValue > 0) {
            // Draw bars representing activity
            for (i in 0..6) {
                val barHeight = (height * 0.8f * (0.3f + 0.7f * kotlin.random.Random.nextFloat()))
                val x = i * barWidth + barWidth * 0.2f
                val y = height - barHeight
                
                drawRect(
                    color = when (i % 3) {
                        0 -> primaryColor
                        1 -> secondaryColor
                        else -> tertiaryColor
                    },
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.6f, barHeight)
                )
            }
        }
    }
}

@Composable
private fun ListeningBehaviorChart(
    likedSongs: Int,
    totalSongs: Int,
    recentSongs: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        
        if (totalSongs > 0) {
            val likedRatio = likedSongs.toFloat() / totalSongs.toFloat()
            val recentRatio = recentSongs.toFloat() / totalSongs.toFloat()
            
            // Draw liked songs bar
            drawRect(
                color = primaryColor,
                topLeft = Offset(0f, centerY - 15f),
                size = androidx.compose.ui.geometry.Size(width * likedRatio, 10f)
            )
            
            // Draw recent songs bar
            drawRect(
                color = secondaryColor,
                topLeft = Offset(0f, centerY + 5f),
                size = androidx.compose.ui.geometry.Size(width * recentRatio, 10f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MusicPreferencesDialog(
    currentPreferences: Set<String>,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    var selectedPreferences by remember { mutableStateOf(currentPreferences) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Update Music Preferences",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Select your favorite music genres:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(OnboardingData.genres.chunked(2)) { genreRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            genreRow.forEach { genre ->
                                FilterChip(
                                    onClick = {
                                        selectedPreferences = if (selectedPreferences.contains(genre.name)) {
                                            selectedPreferences - genre.name
                                        } else {
                                            selectedPreferences + genre.name
                                        }
                                    },
                                    label = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(genre.emoji)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(genre.name)
                                        }
                                    },
                                    selected = selectedPreferences.contains(genre.name),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (genreRow.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(selectedPreferences) },
                        enabled = selectedPreferences.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Note: Preferences can only be updated once every 5 minutes to maintain recommendation quality.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ExportDataDialog(
    totalSongs: Int,
    likedSongs: Int,
    recentSongs: Int,
    totalEvents: Int,
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Export Listening Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Your data summary:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("• Total Songs: $totalSongs")
                    Text("• Liked Songs: $likedSongs")
                    Text("• Recently Played: $recentSongs")
                    Text("• Listening Events: $totalEvents")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Choose export format:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onExport("JSON") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Code, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export as JSON")
                    }
                    
                    OutlinedButton(
                        onClick = { onExport("CSV") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export as CSV")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}