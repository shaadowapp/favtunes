package com.shaadow.tunes.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaadow.tunes.utils.ScreenDetector

/**
 * Invisible screen identifier component that registers the current screen
 * with the ScreenDetector for accurate screen detection.
 * 
 * This component should be placed in each major screen to ensure proper
 * screen identification for bug reporting and analytics.
 * 
 * @param screenId Unique identifier for the screen (e.g., "home", "search")
 * @param screenName Human-readable name for the screen (e.g., "Home Screen", "Search Screen")
 */
@Composable
fun ScreenIdentifier(
    screenId: String,
    screenName: String
) {
    // Register this screen as the current active screen
    LaunchedEffect(screenId, screenName) {
        ScreenDetector.setCurrentScreen(screenId, screenName)
    }
    
    // Invisible text element that can be used for testing/debugging
    // This text is effectively invisible to users but can be detected programmatically
    Box(
        modifier = Modifier.size(0.dp)
    ) {
        Text(
            text = "SCREEN_ID:$screenId",
            color = Color.Transparent,
            fontSize = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.size(0.dp)
        )
    }
}