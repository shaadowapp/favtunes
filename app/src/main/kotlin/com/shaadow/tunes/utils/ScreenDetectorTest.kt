package com.shaadow.tunes.utils

import android.util.Log

/**
 * Test utility for verifying screen detection functionality
 */
object ScreenDetectorTest {
    
    private const val TAG = "ScreenDetectorTest"
    
    /**
     * Test the screen detection system with various route patterns
     */
    fun testScreenDetection() {
        Log.d(TAG, "Starting Screen Detection Tests...")
        
        val testRoutes = listOf(
            "home" to "Home Screen",
            "songs" to "Songs Screen", 
            "artists" to "Artists Screen",
            "playlists" to "Playlists Screen",
            "mytunes" to "My Tunes Screen",
            "explore" to "Explore Screen",
            "search" to "Search Screen",
            "settings" to "Settings Screen",
            "profile" to "Profile Screen",
            "inbox" to "Inbox Screen",
            "bugreport" to "Bug Report Screen",
            "feedback" to "Feedback Screen",
            "album/123" to "Album Screen",
            "artist/456" to "Artist Screen",
            "playlist/789" to "Playlist Screen",
            "localPlaylist/101" to "Local Playlist Screen",
            "builtInPlaylist/2" to "Built-in Playlist Screen",
            "settingsPage/0" to "General Settings",
            "settingsPage/1" to "Player Settings",
            "settingsPage/2" to "Cache Settings",
            "settingsPage/3" to "Database Settings",
            "settingsPage/4" to "About Screen",
            "settingsPage/5" to "Gestures Settings",
            "settingsPage/6" to "Other Settings",
            "settingsPage/7" to "Suggestion Settings",
            "onboarding" to "Onboarding Screen",
            "mytunes" to "My Tunes Screen",
            "player" to "Player Screen",
            "queue" to "Queue Screen",
            "searchResults/test" to "Search Results Screen",
            "search?initialQuery=test" to "Search Screen",
            "unknown_route" to "Unknown Screen"
        )
        
        testRoutes.forEach { (route, expectedName) ->
            val context = ScreenDetector.getScreenContextFromRoute(route)
            val actualName = context?.screenName ?: "null"
            val status = if (actualName == expectedName) "✓ PASS" else "✗ FAIL"
            
            Log.d(TAG, "$status - Route: '$route' -> Expected: '$expectedName', Got: '$actualName'")
        }
        
        Log.d(TAG, "Screen Detection Tests Complete")
    }
    
    /**
     * Test the current screen context functionality
     */
    fun testCurrentScreenContext() {
        Log.d(TAG, "Testing current screen context...")
        
        // Simulate setting different screens
        ScreenDetector.setCurrentScreen("home", "Home Screen")
        val homeContext = ScreenDetector.getCurrentScreenContext()
        Log.d(TAG, "Home context: ${homeContext?.screenName} (${homeContext?.screenId})")
        
        ScreenDetector.setCurrentScreen("search", "Search Screen")
        val searchContext = ScreenDetector.getCurrentScreenContext()
        Log.d(TAG, "Search context: ${searchContext?.screenName} (${searchContext?.screenId})")
        
        ScreenDetector.setCurrentScreen("album", "Album Screen")
        val albumContext = ScreenDetector.getCurrentScreenContext()
        Log.d(TAG, "Album context: ${albumContext?.screenName} (${albumContext?.screenId})")
    }
    
    /**
     * Run all screen detection tests
     */
    fun runAllTests() {
        testScreenDetection()
        testCurrentScreenContext()
    }
}