package com.shaadow.tunes.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shaadow.tunes.utils.RemoteConfigManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteConfigDebugScreen(
    paddingValues: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var config by remember { mutableStateOf(RemoteConfigManager.getConfig()) }
    var isLoading by remember { mutableStateOf(false) }
    var lastFetchTime by remember { mutableStateOf("Never") }
    
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
                            text = "Remote Config Debug",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    try {
                                        config = RemoteConfigManager.fetchConfig()
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
                }
            }
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Video ID Mismatch Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ConfigItem("Retry Enabled", config.videoIdMismatchRetryEnabled.toString())
                    ConfigItem("Max Retries", config.videoIdMismatchMaxRetries.toString())
                    ConfigItem("Retry Delay", "${config.videoIdMismatchRetryDelayMs}ms")
                    ConfigItem("Skip Failed Videos", config.skipFailedVideos.toString())
                }
            }
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Fallback Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ConfigItem("Fallback to Alternative", config.fallbackToAlternativePlayer.toString())
                    ConfigItem("Alternative Endpoints", "${config.alternativePlayerEndpoints.size} configured")
                    
                    if (config.alternativePlayerEndpoints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        config.alternativePlayerEndpoints.forEachIndexed { index, endpoint ->
                            Text(
                                text = "${index + 1}. ${endpoint.take(30)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp)
                            )
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
                        text = "System Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ConfigItem("Video ID Validation", config.enableVideoIdValidation.toString())
                    ConfigItem("Debug Logging", config.debugLoggingEnabled.toString())
                    
                    if (config.emergencyDisablePlayback) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EMERGENCY: Playback Disabled",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                                // Test retry logic
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Retry")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                // Clear failed videos
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Failed")
                        }
                    }
                }
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