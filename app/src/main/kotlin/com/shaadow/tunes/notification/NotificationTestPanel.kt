package com.shaadow.tunes.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Updated NotificationTestPanel using new architecture with proper separation of concerns
 */
@Composable
fun NotificationTestPanel() {
    val context = LocalContext.current
    val notificationManager = FavTunesNotificationManager(context)
    val contentGenerator = NotificationContentGenerator()
    val notificationDelivery = LocalNotificationHelper(context)
    val notificationTest = NotificationSystemTest(context)
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Enhanced Notification Test Panel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Test New Architecture Notifications",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Button(
                        onClick = {
                            val content = contentGenerator.generateEngagementContent(48) // 2 days
                            notificationDelivery.showEngagementNotification(content)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Enhanced Engagement")
                    }
                    
                    Button(
                        onClick = {
                            val content = contentGenerator.generateMusicSuggestionContent(emptyList())
                            notificationDelivery.showMusicSuggestion(content)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Enhanced Music Suggestion")
                    }
                    
                    Button(
                        onClick = {
                            val content = contentGenerator.generateMarketingContent()
                            notificationDelivery.showMarketingNotification(content)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Enhanced Marketing")
                    }
                    
                    Button(
                        onClick = {
                            val content = contentGenerator.generatePersonalizedRoast(emptyList())
                            notificationDelivery.showEngagementNotification(content)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Savage Roast")
                    }
                    
                    Button(
                        onClick = {
                            val content = contentGenerator.generateTrendingContent("trending_song_123")
                            notificationDelivery.showTrendingNotification(content)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Trending Notification")
                    }
                    
                    Button(
                        onClick = {
                            val content = contentGenerator.generatePersonalizedSuggestion(emptyList())
                            notificationDelivery.showPersonalizedSuggestion(content)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Personalized Suggestion")
                    }
                }
            }
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Test Intelligent Scheduling",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Button(
                        onClick = {
                            // Test intelligent scheduling with different priorities
                            scope.launch {
                                notificationManager.sendEngagementNotification(72) // 3 days
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Intelligent Engagement Scheduling")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                notificationManager.sendMusicSuggestion(emptyList())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Intelligent Music Scheduling")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                notificationManager.scheduleMarketingNotification()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Intelligent Marketing Scheduling")
                    }
                }
            }
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "System Tests & Diagnostics",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Button(
                        onClick = {
                            scope.launch {
                                val summary = notificationTest.runAllTests()
                                android.util.Log.d("NotificationTest", 
                                    "Tests completed: ${summary.passed}/${summary.totalTests} passed")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Run System Tests")
                    }
                    
                    Button(
                        onClick = {
                            notificationTest.resetThrottling()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Throttling (for testing)")
                    }
                    
                    Button(
                        onClick = {
                            val status = notificationTest.getSystemStatus()
                            android.util.Log.d("NotificationTest", status)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show System Status")
                    }
                }
            }
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Notification Management",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Button(
                        onClick = {
                            notificationDelivery.cancelAllNotifications()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel All Notifications")
                    }
                    
                    Text(
                        text = if (notificationDelivery.areNotificationsEnabled()) {
                            "✅ Notifications Enabled"
                        } else {
                            "❌ Notifications Disabled"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}