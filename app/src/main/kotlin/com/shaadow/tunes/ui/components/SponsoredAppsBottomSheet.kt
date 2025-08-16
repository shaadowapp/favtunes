package com.shaadow.tunes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SponsoredApp(
    val name: String,
    val description: String,
    val packageName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsoredAppsBottomSheet(
    onDismiss: () -> Unit
) {
    val sponsoredApps = listOf(
        SponsoredApp("Music Player Pro", "Advanced music player with equalizer", "com.example.musicpro"),
        SponsoredApp("Audio Editor", "Professional audio editing tools", "com.example.audioeditor"),
        SponsoredApp("Sound Recorder", "High quality voice recorder", "com.example.recorder")
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Sponsored",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sponsoredApps) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            // TODO: Open app in Play Store
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = app.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}