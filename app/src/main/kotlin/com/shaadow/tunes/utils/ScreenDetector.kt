package com.shaadow.tunes.utils

import androidx.compose.runtime.*
import androidx.navigation.NavController

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