package com.shaadow.tunes.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val notificationManager = remember { FavTunesNotificationManager(context) }
    
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    var notificationsEnabled by remember {
        mutableStateOf(
            preferences.getBoolean(FavTunesNotificationManager.PREF_NOTIFICATIONS_ENABLED, false)
        )
    }
    
    var musicSuggestionsEnabled by remember {
        mutableStateOf(
            preferences.getBoolean(FavTunesNotificationManager.PREF_MUSIC_SUGGESTIONS, false)
        )
    }
    
    var engagementRemindersEnabled by remember {
        mutableStateOf(
            preferences.getBoolean(FavTunesNotificationManager.PREF_ENGAGEMENT_REMINDERS, true)
        )
    }
    
    var marketingNotificationsEnabled by remember {
        mutableStateOf(
            preferences.getBoolean(FavTunesNotificationManager.PREF_MARKETING_NOTIFICATIONS, true)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Notification Preferences",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Master Control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Notifications")
                            Text(
                                "Turn off to disable all notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                notificationsEnabled = enabled
                                preferences.edit()
                                    .putBoolean(FavTunesNotificationManager.PREF_NOTIFICATIONS_ENABLED, enabled)
                                    .apply()
                                
                                // Note: Scheduling functions are suspend and should be called from a coroutine
                                // For now, we'll handle this in the notification manager itself
                            }
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Notification Types",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    NotificationToggleItem(
                        title = "Music Suggestions",
                        description = "Personalized song recommendations based on your taste",
                        enabled = musicSuggestionsEnabled && notificationsEnabled,
                        onToggle = { enabled ->
                            musicSuggestionsEnabled = enabled
                            preferences.edit()
                                .putBoolean(FavTunesNotificationManager.PREF_MUSIC_SUGGESTIONS, enabled)
                                .apply()
                        }
                    )
                    
                    Divider()
                    
                    NotificationToggleItem(
                        title = "Engagement Reminders",
                        description = "Gentle reminders when you haven't used the app",
                        enabled = engagementRemindersEnabled && notificationsEnabled,
                        onToggle = { enabled ->
                            engagementRemindersEnabled = enabled
                            preferences.edit()
                                .putBoolean(FavTunesNotificationManager.PREF_ENGAGEMENT_REMINDERS, enabled)
                                .apply()
                        }
                    )
                    
                    Divider()
                    
                    NotificationToggleItem(
                        title = "Fun Updates",
                        description = "Witty music facts and entertaining notifications",
                        enabled = marketingNotificationsEnabled && notificationsEnabled,
                        onToggle = { enabled ->
                            marketingNotificationsEnabled = enabled
                            preferences.edit()
                                .putBoolean(FavTunesNotificationManager.PREF_MARKETING_NOTIFICATIONS, enabled)
                                .apply()
                        }
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Music suggestions are sent daily at random times\n" +
                                "• Engagement reminders appear after 24+ hours of inactivity\n" +
                                "• Fun updates are sent occasionally to brighten your day\n" +
                                "• All notifications respect your device's Do Not Disturb settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}