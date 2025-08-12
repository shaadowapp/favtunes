package com.shaadow.tunes.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player

/**
 * Helper class to integrate broadcast notifications with existing PlayerService
 * Add these calls to your PlayerService to enable real-time recommendation updates
 */
class PlayerServiceBroadcastHelper(private val context: Context) {
    
    /**
     * Call this in PlayerService when a song starts playing
     * Example: Add to onPlaybackStateChanged when state becomes Player.STATE_READY
     */
    fun notifySongStarted(mediaItem: MediaItem?) {
        mediaItem?.let {
            RecommendationBroadcaster.broadcastSongPlayed(context, it)
        }
    }
    
    /**
     * Call this in PlayerService when user likes a song
     * Example: Add to your like button handler
     */
    fun notifySongLiked(mediaItem: MediaItem?) {
        mediaItem?.let {
            RecommendationBroadcaster.broadcastSongLiked(context, it)
        }
    }
    
    /**
     * Call this in PlayerService when user skips a song
     * Example: Add to onSkipToNext or when user manually skips
     */
    fun notifySongSkipped(mediaItem: MediaItem?) {
        RecommendationBroadcaster.broadcastSongSkipped(context, mediaItem)
    }
    
    /**
     * Call this when recommendations should be refreshed
     * Example: After user changes preferences or after a certain number of songs
     */
    fun refreshRecommendations() {
        RecommendationBroadcaster.broadcastRefreshRecommendations(context)
    }
    
    /**
     * Player listener to automatically track song changes
     * Add this listener to your ExoPlayer instance
     */
    fun createPlayerListener(): Player.Listener {
        return object : Player.Listener {
            private var lastMediaItem: MediaItem? = null
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                
                // Notify about previous song ending (if skipped)
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK ||
                    reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    if (lastMediaItem != null && reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                        notifySongSkipped(lastMediaItem)
                    }
                }
                
                // Notify about new song starting
                mediaItem?.let {
                    notifySongStarted(it)
                }
                
                lastMediaItem = mediaItem
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                
                // You can add additional logic here if needed
                // For example, track when songs complete naturally
            }
        }
    }
}

/*
INTEGRATION EXAMPLE:

In your PlayerService class, add:

class PlayerService : MediaBrowserServiceCompat() {
    private lateinit var broadcastHelper: PlayerServiceBroadcastHelper
    
    override fun onCreate() {
        super.onCreate()
        broadcastHelper = PlayerServiceBroadcastHelper(this)
        
        // Add the listener to your player
        player.addListener(broadcastHelper.createPlayerListener())
    }
    
    // In your like button handler:
    fun onLikePressed() {
        // Your existing like logic...
        broadcastHelper.notifySongLiked(player.currentMediaItem)
    }
    
    // In your skip handler:
    fun onSkipPressed() {
        // Your existing skip logic...
        broadcastHelper.notifySongSkipped(player.currentMediaItem)
        player.seekToNext()
    }
}
*/