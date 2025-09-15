package com.shaadow.tunes.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

/**
 * Utility to detect current screen context for bug reporting
 */
object ScreenDetector {
    
    // Current screen identifier - updated by screens when they become active
    private var currentScreenId: String? = null
    private var currentScreenName: String? = null
    
    /**
     * Set the current screen identifier (called by screens when they become active)
     */
    fun setCurrentScreen(screenId: String, screenName: String) {
        currentScreenId = screenId
        currentScreenName = screenName
    }
    
    /**
     * Get current screen context from the stored identifier
     */
    fun getCurrentScreenContext(navController: NavController? = null): BugReportScreenContext? {
        return if (currentScreenId != null && currentScreenName != null) {
            BugReportScreenContext(currentScreenName!!, currentScreenId!!)
        } else {
            // Fallback to route-based detection if no identifier is set
            getCurrentScreenContextFromRoute(navController)
        }
    }
    
    /**
     * Fallback method: Get current screen context from NavController route
     */
    private fun getCurrentScreenContextFromRoute(navController: NavController?): BugReportScreenContext? {
        return try {
            val currentDestination = navController?.currentDestination
            val route = currentDestination?.route
            getScreenContextFromRoute(route)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get screen context from route string directly (improved mapping)
     */
    fun getScreenContextFromRoute(route: String?): BugReportScreenContext? {
        return when {
            route == null -> null
            // Exact route matches first
            route == "home" -> BugReportScreenContext("Home Screen", "home")
            route == "songs" -> BugReportScreenContext("Songs Screen", "songs")
            route == "artists" -> BugReportScreenContext("Artists Screen", "artists")
            route == "playlists" -> BugReportScreenContext("Playlists Screen", "playlists")
            route == "mytunes" -> BugReportScreenContext("My Tunes Screen", "mytunes")
            route == "explore" -> BugReportScreenContext("Explore Screen", "explore")
            route == "search" -> BugReportScreenContext("Search Screen", "search")
            route == "settings" -> BugReportScreenContext("Settings Screen", "settings")
            route == "profile" -> BugReportScreenContext("Profile Screen", "profile")
            route == "inbox" -> BugReportScreenContext("Inbox Screen", "inbox")
            route == "bugreport" -> BugReportScreenContext("Bug Report Screen", "bugreport")
            route == "feedback" -> BugReportScreenContext("Feedback Screen", "feedback")
            route == "player" -> BugReportScreenContext("Player Screen", "player")
            route == "queue" -> BugReportScreenContext("Queue Screen", "queue")
            route == "onboarding" -> BugReportScreenContext("Onboarding Screen", "onboarding")
            route == "mytunes" -> BugReportScreenContext("My Tunes Screen", "mytunes")
            // Pattern matches for parameterized routes
            route.startsWith("album/") -> BugReportScreenContext("Album Screen", "album")
            route.startsWith("artist/") -> BugReportScreenContext("Artist Screen", "artist")
            route.startsWith("playlist/") -> BugReportScreenContext("Playlist Screen", "playlist")
            route.startsWith("localPlaylist/") -> BugReportScreenContext("Local Playlist Screen", "localplaylist")
            route.startsWith("builtInPlaylist/") -> BugReportScreenContext("Built-in Playlist Screen", "builtinplaylist")
            route.startsWith("searchResults/") -> BugReportScreenContext("Search Results Screen", "search_results")
            route.startsWith("settingsPage/") -> {
                // Map specific settings pages
                when {
                    route.contains("0") -> BugReportScreenContext("General Settings", "general_settings")
                    route.contains("1") -> BugReportScreenContext("Player Settings", "player_settings")
                    route.contains("2") -> BugReportScreenContext("Cache Settings", "cache_settings")
                    route.contains("3") -> BugReportScreenContext("Database Settings", "database_settings")
                    route.contains("4") -> BugReportScreenContext("About Screen", "about")
                    route.contains("5") -> BugReportScreenContext("Gestures Settings", "gestures_settings")
                    route.contains("6") -> BugReportScreenContext("Other Settings", "other_settings")
                    route.contains("7") -> BugReportScreenContext("Suggestion Settings", "suggestion_settings")
                    else -> BugReportScreenContext("Settings Page", "settingspage")
                }
            }
            route.startsWith("search?") -> BugReportScreenContext("Search Screen", "search")
            // Legacy fallback patterns
            route.contains("onboarding") -> BugReportScreenContext("Onboarding Screen", "onboarding")
            else -> BugReportScreenContext("Unknown Screen", route ?: "unknown")
        }
    }
}

/**
 * Data class representing screen context
 */
data class BugReportScreenContext(
    val screenName: String,
    val screenId: String
)