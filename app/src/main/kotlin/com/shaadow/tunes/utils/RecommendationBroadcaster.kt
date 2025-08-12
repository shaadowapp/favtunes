package com.shaadow.tunes.utils

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaItem

/**
 * Utility class for broadcasting song events to update recommendations
 */
object RecommendationBroadcaster {
    
    /**
     * Broadcast when a song starts playing
     */
    fun broadcastSongPlayed(context: Context, mediaItem: MediaItem) {
        val intent = Intent("SONG_PLAYED").apply {
            putExtra("song_id", mediaItem.mediaId)
            putExtra("song_title", mediaItem.mediaMetadata.title?.toString())
            putExtra("song_artist", mediaItem.mediaMetadata.artist?.toString())
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
    
    /**
     * Broadcast when a song is liked
     */
    fun broadcastSongLiked(context: Context, mediaItem: MediaItem) {
        val intent = Intent("SONG_LIKED").apply {
            putExtra("song_id", mediaItem.mediaId)
            putExtra("song_title", mediaItem.mediaMetadata.title?.toString())
            putExtra("song_artist", mediaItem.mediaMetadata.artist?.toString())
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
    
    /**
     * Broadcast when a song is skipped
     */
    fun broadcastSongSkipped(context: Context, mediaItem: MediaItem?) {
        val intent = Intent("SONG_SKIPPED").apply {
            mediaItem?.let {
                putExtra("song_id", it.mediaId)
                putExtra("song_title", it.mediaMetadata.title?.toString())
                putExtra("song_artist", it.mediaMetadata.artist?.toString())
            }
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
    
    /**
     * Broadcast when recommendations should be refreshed
     */
    fun broadcastRefreshRecommendations(context: Context) {
        val intent = Intent("REFRESH_RECOMMENDATIONS")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}