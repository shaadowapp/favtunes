package com.shaadow.tunes.utils

import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.shaadow.tunes.models.BugCategory
import com.shaadow.tunes.models.ScreenContext

object ScreenDetector {
    fun getCurrentScreenName(navController: NavController?): String {
        return navController?.currentDestination?.route?.let { route ->
            when {
                route.contains("home") || route == "0" -> "Home Screen"
                route.contains("songs") || route == "1" -> "Songs Screen"
                route.contains("artists") || route == "2" -> "Artists Screen"
                route.contains("albums") || route == "3" -> "Albums Screen"
                route.contains("playlists") || route == "4" -> "Playlists Screen"
                route.contains("settings") -> "Settings Screen"
                route.contains("player") -> "Player Screen"
                route.contains("search") -> "Search Screen"
                route.contains("album/") -> "Album Details Screen"
                route.contains("artist/") -> "Artist Details Screen"
                route.contains("playlist/") -> "Playlist Details Screen"
                else -> "Unknown Screen"
            }
        } ?: "Home Screen"
    }
    
    fun getCurrentScreenContext(navController: NavController?): ScreenContext {
        val route = navController?.currentDestination?.route
        return when {
            route?.contains("home") == true || route == "0" -> 
                ScreenContext("Home Screen", BugCategory.UI)
            route?.contains("songs") == true || route == "1" -> 
                ScreenContext("Songs Screen", BugCategory.UI)
            route?.contains("artists") == true || route == "2" -> 
                ScreenContext("Artists Screen", BugCategory.UI)
            route?.contains("albums") == true || route == "3" -> 
                ScreenContext("Albums Screen", BugCategory.UI)
            route?.contains("playlists") == true || route == "4" -> 
                ScreenContext("Playlists Screen", BugCategory.UI)
            route?.contains("settings") == true -> 
                ScreenContext("Settings Screen", BugCategory.UI)
            route?.contains("player") == true -> 
                ScreenContext("Player Screen", BugCategory.PLAYBACK)
            route?.contains("search") == true -> 
                ScreenContext("Search Screen", BugCategory.PERFORMANCE)
            route?.contains("album/") == true -> 
                ScreenContext("Album Details Screen", BugCategory.UI)
            route?.contains("artist/") == true -> 
                ScreenContext("Artist Details Screen", BugCategory.UI)
            route?.contains("playlist/") == true -> 
                ScreenContext("Playlist Details Screen", BugCategory.UI)
            else -> ScreenContext("Unknown Screen", BugCategory.OTHER)
        }
    }
    
    fun getSuggestedCategoryForScreen(screenName: String): BugCategory {
        return when {
            screenName.contains("Player") -> BugCategory.PLAYBACK
            screenName.contains("Search") -> BugCategory.PERFORMANCE
            screenName.contains("Sync") -> BugCategory.SYNC
            else -> BugCategory.UI
        }
    }
    
    fun getAllScreenNames(): List<String> {
        return listOf(
            "Home Screen",
            "Songs Screen", 
            "Artists Screen",
            "Albums Screen",
            "Playlists Screen",
            "Settings Screen",
            "Player Screen",
            "Search Screen",
            "Album Details Screen",
            "Artist Details Screen",
            "Playlist Details Screen",
            "Other"
        )
    }
}