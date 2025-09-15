package com.shaadow.tunes.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.utils.AdvancedRemoteConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedRemoteConfigScreen(
    paddingValues: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var config by remember { mutableStateOf(AdvancedRemoteConfig.getConfig()) }
    var isLoading by remember { mutableStateOf(false) }
    var lastFetchTime by remember { mutableStateOf("Never") }
    var expandedSections by remember { mutableStateOf(setOf<String>()) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Header with refresh button
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
                        Column {
                            Text(
                                text = "Advanced Remote Config",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Version: ${config.version}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        config = AdvancedRemoteConfig.fetchConfig()
                                        lastFetchTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                            .format(java.util.Date())
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fetch Config")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Last fetch: $lastFetchTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (config.lastUpdated.isNotEmpty()) {
                        Text(
                            text = "Config updated: ${config.lastUpdated}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Emergency Status
        if (config.emergency.disablePlayback || config.emergency.emergencyMessage.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EMERGENCY MODE ACTIVE",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (config.emergency.disablePlayback) {
                            Text(
                                text = "• Playback is disabled",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        if (config.emergency.emergencyMessage.isNotEmpty()) {
                            Text(
                                text = "• ${config.emergency.emergencyMessage}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Error Handling Section
        item {
            ConfigSection(
                title = "Error Handling",
                icon = Icons.Default.Error,
                isExpanded = expandedSections.contains("errorHandling"),
                onToggle = { 
                    expandedSections = if (expandedSections.contains("errorHandling")) {
                        expandedSections - "errorHandling"
                    } else {
                        expandedSections + "errorHandling"
                    }
                }
            ) {
                val errorConfig = config.errorHandling
                
                ErrorConfigItem("Video ID Mismatch", errorConfig.videoIdMismatchEnabled, 
                    errorConfig.videoIdMismatchMaxRetries, errorConfig.videoIdMismatchRetryDelayMs)
                ErrorConfigItem("Playable Format Not Found", errorConfig.playableFormatNotFoundEnabled,
                    errorConfig.playableFormatMaxRetries, errorConfig.playableFormatRetryDelayMs)
                ErrorConfigItem("Unplayable Exception", errorConfig.unplayableExceptionEnabled,
                    errorConfig.unplayableMaxRetries, errorConfig.unplayableRetryDelayMs)
                ErrorConfigItem("Login Required", errorConfig.loginRequiredEnabled,
                    errorConfig.loginRequiredMaxRetries, errorConfig.loginRequiredRetryDelayMs)
            }
        }
        
        // Player Service Section
        item {
            ConfigSection(
                title = "Player Service",
                icon = Icons.Default.PlayArrow,
                isExpanded = expandedSections.contains("playerService"),
                onToggle = { 
                    expandedSections = if (expandedSections.contains("playerService")) {
                        expandedSections - "playerService"
                    } else {
                        expandedSections + "playerService"
                    }
                }
            ) {
                val playerConfig = config.playerService
                
                ConfigItem("Cache Enabled", playerConfig.cacheEnabled.toString())
                ConfigItem("Cache Max Size", "${playerConfig.cacheMaxSizeMB} MB")
                ConfigItem("Connect Timeout", "${playerConfig.connectTimeoutMs} ms")
                ConfigItem("Read Timeout", "${playerConfig.readTimeoutMs} ms")
                ConfigItem("Chunk Size", "${playerConfig.chunkSizeKB} KB")
                ConfigItem("Max Concurrent Requests", playerConfig.maxConcurrentRequests.toString())
            }
        }
        
        // API Endpoints Section
        item {
            ConfigSection(
                title = "API Endpoints",
                icon = Icons.Default.Api,
                isExpanded = expandedSections.contains("apiEndpoints"),
                onToggle = { 
                    expandedSections = if (expandedSections.contains("apiEndpoints")) {
                        expandedSections - "apiEndpoints"
                    } else {
                        expandedSections + "apiEndpoints"
                    }
                }
            ) {
                val apiConfig = config.apiEndpoints
                
                ConfigItem("Use Alternative First", apiConfig.useAlternativeFirst.toString())
                ConfigItem("Fallback Timeout", "${apiConfig.fallbackTimeout} ms")
                ConfigItem("Rotate Endpoints", apiConfig.rotateEndpoints.toString())
                
                Text(
                    text = "Alternative Endpoints:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                apiConfig.alternativeEndpoints.forEachIndexed { index, endpoint ->
                    Text(
                        text = "${index + 1}. ${endpoint}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                }
            }
        }
        
        // Debugging Section
        item {
            ConfigSection(
                title = "Debugging",
                icon = Icons.Default.BugReport,
                isExpanded = expandedSections.contains("debugging"),
                onToggle = { 
                    expandedSections = if (expandedSections.contains("debugging")) {
                        expandedSections - "debugging"
                    } else {
                        expandedSections + "debugging"
                    }
                }
            ) {
                val debugConfig = config.debugging
                
                ConfigItem("Detailed Logging", debugConfig.enableDetailedLogging.toString())
                ConfigItem("Network Requests", debugConfig.logNetworkRequests.toString())
                ConfigItem("Error Details", debugConfig.logErrorDetails.toString())
                ConfigItem("Performance Metrics", debugConfig.logPerformanceMetrics.toString())
                ConfigItem("Log Level", debugConfig.logLevel)
            }
        }
        
        // Feature Flags Section
        if (config.featureFlags.isNotEmpty()) {
            item {
                ConfigSection(
                    title = "Feature Flags",
                    icon = Icons.Default.Flag,
                    isExpanded = expandedSections.contains("featureFlags"),
                    onToggle = { 
                        expandedSections = if (expandedSections.contains("featureFlags")) {
                            expandedSections - "featureFlags"
                        } else {
                            expandedSections + "featureFlags"
                        }
                    }
                ) {
                    config.featureFlags.forEach { (key, value) ->
                        ConfigItem(key, value.toString())
                    }
                }
            }
        }
        
        // Quick Actions
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Test error handling
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Errors")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                // Clear cache
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Cache")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                // Export logs
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export Logs")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun ConfigItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorConfigItem(name: String, enabled: Boolean, maxRetries: Int, delayMs: Long) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (enabled) "Enabled" else "Disabled",
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        
        if (enabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Max Retries: $maxRetries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Delay: ${delayMs}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}