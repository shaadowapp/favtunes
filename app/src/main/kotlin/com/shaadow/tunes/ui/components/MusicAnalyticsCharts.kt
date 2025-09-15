package com.shaadow.tunes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ListeningHabitsChart(
    likedSongs: Int,
    totalSongs: Int,
    recentSongs: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Listening Habits",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Liked Songs Percentage
                val likedPercentage = if (totalSongs > 0) (likedSongs.toFloat() / totalSongs.toFloat()) else 0f
                CircularProgressChart(
                    percentage = likedPercentage,
                    label = "Liked",
                    value = "$likedSongs",
                    color = primaryColor,
                    modifier = Modifier.size(80.dp)
                )
                
                // Recent Activity
                val recentPercentage = if (totalSongs > 0) (recentSongs.toFloat() / totalSongs.toFloat()) else 0f
                CircularProgressChart(
                    percentage = recentPercentage,
                    label = "Recent",
                    value = "$recentSongs",
                    color = secondaryColor,
                    modifier = Modifier.size(80.dp)
                )
                
                // Discovery Rate (mock calculation)
                val discoveryRate = if (totalSongs > 0) minOf(1f, (totalSongs - likedSongs).toFloat() / totalSongs.toFloat()) else 0f
                CircularProgressChart(
                    percentage = discoveryRate,
                    label = "Discovery",
                    value = "${(discoveryRate * 100).toInt()}%",
                    color = tertiaryColor,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

@Composable
private fun CircularProgressChart(
    percentage: Float,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background circle
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(strokeWidth)
            )
            
            // Progress arc
            val sweepAngle = percentage * 360f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GenreDistributionChart(
    preferences: Set<String>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Genre Preferences",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (preferences.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Simple pie chart representation
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val total = preferences.size.toFloat()
                            var startAngle = 0f
                            
                            preferences.forEachIndexed { index, _ ->
                                val sweepAngle = 360f / total
                                val color = colors[index % colors.size]
                                
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    topLeft = Offset.Zero,
                                    size = size
                                )
                                
                                startAngle += sweepAngle
                            }
                        }
                        
                        Text(
                            text = "${preferences.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    preferences.take(5).forEachIndexed { index, genre ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(colors[index % colors.size])
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No genre preferences set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun WeeklyActivityChart(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weekly Activity",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mock weekly data - in real implementation, this would come from database
            val weeklyData = listOf(0.3f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.4f)
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val barWidth = size.width / weeklyData.size
                val maxHeight = size.height * 0.8f
                
                weeklyData.forEachIndexed { index, value ->
                    val barHeight = maxHeight * value
                    val x = index * barWidth + barWidth * 0.2f
                    val y = size.height - barHeight
                    
                    drawRect(
                        color = primaryColor.copy(alpha = 0.7f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth * 0.6f, barHeight)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MusicMoodChart(
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )
    
    // Mock mood data - in real implementation, this would be analyzed from listening patterns
    val moods = listOf(
        "Energetic" to 0.4f,
        "Chill" to 0.3f,
        "Focus" to 0.2f,
        "Happy" to 0.1f
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Music Moods",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            moods.forEachIndexed { index, (mood, percentage) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mood,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { percentage },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = colors[index % colors.size],
                        trackColor = colors[index % colors.size].copy(alpha = 0.2f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(30.dp)
                    )
                }
                
                if (index < moods.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}