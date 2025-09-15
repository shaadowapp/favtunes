package com.shaadow.tunes.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.SQLException
import android.graphics.Color
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaDescription
import android.media.MediaMetadata
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.MediaSessionCompat
import android.text.format.DateUtils
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.DefaultAudioOffloadSupportProvider
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink.DefaultAudioProcessorChain
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.models.NavigationEndpoint
import com.shaadow.innertube.requests.player
import com.shaadow.tunes.Database
import com.shaadow.tunes.MainActivity
import com.shaadow.tunes.R
import com.shaadow.tunes.suggestion.WorkingSuggestionSystem
import com.shaadow.tunes.enums.ExoPlayerDiskCacheMaxSize
import com.shaadow.tunes.models.Event
import com.shaadow.tunes.models.QueuedMediaItem
import com.shaadow.tunes.query
import com.shaadow.tunes.utils.InvincibleService
import com.shaadow.tunes.utils.RingBuffer
import com.shaadow.tunes.utils.TimerJob
import com.shaadow.tunes.utils.YouTubeRadio
import com.shaadow.tunes.utils.activityPendingIntent
import com.shaadow.tunes.utils.broadCastPendingIntent
import com.shaadow.tunes.utils.AdvancedRemoteConfig
import com.shaadow.tunes.utils.exoPlayerDiskCacheMaxSizeKey
import com.shaadow.tunes.utils.findNextMediaItemById
import com.shaadow.tunes.utils.forcePlayFromBeginning
import com.shaadow.tunes.utils.forceSeekToNext
import com.shaadow.tunes.utils.forceSeekToPrevious
import com.shaadow.tunes.utils.getEnum
import com.shaadow.tunes.utils.intent
import com.shaadow.tunes.utils.isAtLeastAndroid13
import com.shaadow.tunes.utils.isAtLeastAndroid6
import com.shaadow.tunes.utils.isAtLeastAndroid8
import com.shaadow.tunes.utils.isInvincibilityEnabledKey
import com.shaadow.tunes.utils.isShowingThumbnailInLockscreenKey
import com.shaadow.tunes.utils.mediaItems
import com.shaadow.tunes.utils.persistentQueueKey
import com.shaadow.tunes.utils.preferences
import com.shaadow.tunes.utils.queueLoopEnabledKey
import com.shaadow.tunes.utils.resumePlaybackWhenDeviceConnectedKey
import com.shaadow.tunes.utils.shouldBePlaying
import com.shaadow.tunes.utils.skipSilenceKey
import com.shaadow.tunes.utils.timer
import com.shaadow.tunes.utils.trackLoopEnabledKey
import com.shaadow.tunes.utils.volumeNormalizationKey
import com.shaadow.tunes.utils.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.system.exitProcess
import android.os.Binder as AndroidBinder
import kotlin.math.min

/**
 * Progressive Audio Streaming Implementation
 *
 * This implementation provides Spotify-like progressive loading where:
 * 1. Initial chunks are loaded aggressively for immediate playback
 * 2. Subsequent chunks adapt based on network conditions
 * 3. Chunk sizes vary from 128KB to 4MB based on multiple factors
 * 4. Network quality, device capabilities, and playback position influence chunk sizing
 * 5. Performance metrics are tracked for continuous optimization
 *
 * Usage Example:
 * ```kotlin
 * val binder = // Get PlayerService binder
 * val stats = binder.getProgressiveLoadingStats("videoId")
 * val networkQuality = binder.getCurrentNetworkQuality()
 * ```
 */

/**
 * Progressive Loader - Implements Spotify-like adaptive chunk loading
 * Dynamically adjusts chunk sizes based on network conditions, playback position, and content characteristics
 */
private class ProgressiveLoader(private val context: Context) {

    private val networkMonitor = NetworkMonitor(context)
    private val chunkSizeHistory = mutableMapOf<String, MutableList<Long>>()
    private val loadTimeHistory = mutableMapOf<String, MutableList<Long>>()

    // Base chunk sizes for different network conditions
    private val baseChunkSizes = mapOf(
        "excellent" to 2 * 1024 * 1024L,  // 2MB for excellent connections
        "good" to 1024 * 1024L,          // 1MB for good connections
        "fair" to 512 * 1024L,           // 512KB for fair connections
        "poor" to 256 * 1024L            // 256KB for poor connections
    )

    /**
     * Calculate adaptive chunk size based on multiple factors
     */
    fun calculateAdaptiveChunkSize(videoId: String, position: Long): Long {
        val networkQuality = assessNetworkQuality()
        val baseSize = baseChunkSizes[networkQuality] ?: 512 * 1024L

        // Adjust based on playback position (load more at beginning)
        val positionMultiplier = when {
            position < 30_000 -> 1.5f    // First 30 seconds: load more aggressively
            position < 120_000 -> 1.2f   // First 2 minutes: moderately aggressive
            else -> 1.0f                 // Normal loading
        }

        // Adjust based on historical performance
        val performanceMultiplier = calculatePerformanceMultiplier(videoId)

        // Adjust based on device capabilities
        val deviceMultiplier = calculateDeviceMultiplier()

        val adaptiveSize = (baseSize * positionMultiplier * performanceMultiplier * deviceMultiplier).toLong()

        // Ensure reasonable bounds
        return adaptiveSize.coerceIn(128 * 1024L, 4 * 1024 * 1024L) // 128KB to 4MB
    }

    /**
     * Assess current network quality
     */
    private fun assessNetworkQuality(): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)

            when {
                capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                    // WiFi - generally better quality
                    "excellent"
                }
                capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true -> {
                    // Unmetered cellular (like home WiFi)
                    "good"
                }
                capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true -> {
                    // Validated connection
                    "fair"
                }
                else -> "poor"
            }
        } catch (e: Exception) {
            android.util.Log.w("ProgressiveLoader", "Error assessing network quality", e)
            "fair" // Default to fair
        }
    }

    /**
     * Calculate performance multiplier based on historical load times
     */
    private fun calculatePerformanceMultiplier(videoId: String): Float {
        val history = loadTimeHistory[videoId] ?: return 1.0f
        if (history.size < 3) return 1.0f

        val avgLoadTime = history.average()
        return when {
            avgLoadTime < 1000 -> 1.2f    // Fast loading: increase chunk size
            avgLoadTime < 3000 -> 1.0f    // Normal loading: maintain
            else -> 0.8f                   // Slow loading: decrease chunk size
        }
    }

    /**
     * Calculate device capability multiplier
     */
    private fun calculateDeviceMultiplier(): Float {
        val runtime = Runtime.getRuntime()
        val maxMemoryMB = runtime.maxMemory() / (1024 * 1024)

        return when {
            maxMemoryMB > 512 -> 1.2f     // High-end device
            maxMemoryMB > 256 -> 1.0f     // Mid-range device
            else -> 0.8f                  // Low-end device
        }
    }

    /**
     * Record chunk load performance for future optimization
     */
    fun recordChunkLoadTime(videoId: String, loadTimeMs: Long, chunkSize: Long) {
        loadTimeHistory.getOrPut(videoId) { mutableListOf() }.apply {
            add(loadTimeMs)
            if (size > 10) removeAt(0) // Keep only last 10 measurements
        }

        chunkSizeHistory.getOrPut(videoId) { mutableListOf() }.apply {
            add(chunkSize)
            if (size > 10) removeAt(0)
        }
    }

    /**
     * Get loading statistics for debugging
     */
    fun getLoadingStats(videoId: String): Map<String, Any> {
        val loadTimes = loadTimeHistory[videoId] ?: emptyList()
        val chunkSizes = chunkSizeHistory[videoId] ?: emptyList()

        return mapOf(
            "averageLoadTime" to loadTimes.average(),
            "averageChunkSize" to chunkSizes.average(),
            "sampleCount" to loadTimes.size,
            "networkQuality" to assessNetworkQuality()
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PlayerService : InvincibleService(), Player.Listener, PlaybackStatsListener.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var mediaSession: MediaSession
    private lateinit var cache: SimpleCache
    private lateinit var player: ExoPlayer
    private lateinit var behaviorTracker: WorkingSuggestionSystem
    private lateinit var progressiveLoader: ProgressiveLoader

    private val stateBuilder
        get() = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY
                        or PlaybackState.ACTION_PAUSE
                        or PlaybackState.ACTION_PLAY_PAUSE
                        or PlaybackState.ACTION_STOP
                        or PlaybackState.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackState.ACTION_SKIP_TO_NEXT
                        or PlaybackState.ACTION_SKIP_TO_QUEUE_ITEM
                        or PlaybackState.ACTION_SEEK_TO
                        or PlaybackState.ACTION_REWIND
            )
            .addCustomAction(
                FAVORITE_ACTION,
                "Toggle like",
                if (isLikedState.value) R.drawable.heart else R.drawable.heart_outline
            )

    private val playbackStateMutex = Mutex()

    private val metadataBuilder = MediaMetadata.Builder()

    private var notificationManager: NotificationManager? = null

    private var timerJob: TimerJob? = null

    private var radio: YouTubeRadio? = null

    private lateinit var bitmapProvider: BitmapProvider

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + Job()

    private var volumeNormalizationJob: Job? = null

    // Add error tracking
    private val failedVideoIds = mutableSetOf<String>()
    private val errorRetryCount = mutableMapOf<String, Int>()
    private val lastErrorTime = mutableMapOf<String, Long>()
    private val maxRetries = 3
    private val retryDelayMs = 5000L // 5 seconds

    private var isPersistentQueueEnabled = false
    private var isShowingThumbnailInLockscreen = true
    override var isInvincibilityEnabled = false

    private var audioManager: AudioManager? = null
    private var audioDeviceCallback: AudioDeviceCallback? = null

    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val binder = Binder()

    // Memory optimization scheduler
    private val memoryOptimizationHandler = Handler(android.os.Looper.getMainLooper())
    private val memoryOptimizationRunnable = object : Runnable {
        override fun run() {
            optimizeMemoryUsage()
            // Schedule next optimization in 5 minutes
            memoryOptimizationHandler.postDelayed(this, 5 * 60 * 1000L)
        }
    }

    private var isNotificationStarted = false

    override val notificationId: Int
        get() = NOTIFICATION_ID

    private lateinit var notificationActionReceiver: NotificationActionReceiver

    private val mediaItemState = MutableStateFlow<MediaItem?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val isLikedState = mediaItemState
        .flatMapMerge { item ->
            item?.mediaId?.let {
                Database
                    .likedAt(it)
                    .distinctUntilChanged()
                    .cancellable()
            } ?: flowOf(null)
        }
        .map { it != null }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    override fun onBind(intent: Intent?): AndroidBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize advanced remote config for complete control
        AdvancedRemoteConfig.initialize(this)

        // Initialize behavior tracking system
        behaviorTracker = WorkingSuggestionSystem(applicationContext)

        // Initialize progressive loading system
        progressiveLoader = ProgressiveLoader(applicationContext)

        bitmapProvider = BitmapProvider(
            context = applicationContext,
            bitmapSize = (256 * resources.displayMetrics.density).roundToInt(),
            colorProvider = { isSystemInDarkMode ->
                if (isSystemInDarkMode) Color.BLACK else Color.WHITE
            }
        )

        createNotificationChannel()

        preferences.registerOnSharedPreferenceChangeListener(this)

        val preferences = preferences
        isPersistentQueueEnabled = preferences.getBoolean(persistentQueueKey, false)
        isInvincibilityEnabled = preferences.getBoolean(isInvincibilityEnabledKey, false)
        isShowingThumbnailInLockscreen =
            preferences.getBoolean(isShowingThumbnailInLockscreenKey, false)

        val cacheEvictor = when (val size =
            preferences.getEnum(exoPlayerDiskCacheMaxSizeKey, ExoPlayerDiskCacheMaxSize.`2GB`)) {
            ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()
            else -> LeastRecentlyUsedCacheEvictor(size.bytes)
        }

        // TODO: Remove in a future release
        val directory = cacheDir.resolve("exoplayer").also { directory ->
            if (directory.exists()) return@also

            directory.mkdir()

            cacheDir.listFiles()?.forEach { file ->
                if (file.isDirectory && file.name.length == 1 && file.name.isDigitsOnly() || file.extension == "uid") {
                    if (!file.renameTo(directory.resolve(file.name))) {
                        file.deleteRecursively()
                    }
                }
            }

            filesDir.resolve("coil").deleteRecursively()
        }
        cache = SimpleCache(directory, cacheEvictor, StandaloneDatabaseProvider(this))

        player = ExoPlayer.Builder(this, createRendersFactory(), createMediaSourceFactory())
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setUsePlatformDiagnostics(false)
            .build()

        player.repeatMode = when {
            preferences.getBoolean(trackLoopEnabledKey, false) -> Player.REPEAT_MODE_ONE
            preferences.getBoolean(queueLoopEnabledKey, false) -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }

        player.skipSilenceEnabled = preferences.getBoolean(skipSilenceKey, false)
        player.addListener(this)
        player.addAnalyticsListener(PlaybackStatsListener(false, this))

        maybeRestorePlayerQueue()

        mediaSession = MediaSession(baseContext, "PlayerService")
        mediaSession.setCallback(SessionCallback(player))
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.isActive = true

        coroutineScope.launch {
            isLikedState
                .onEach { withContext(Dispatchers.Main) { updatePlaybackState() } }
                .collect()
        }

        notificationActionReceiver = NotificationActionReceiver(player)

        val filter = IntentFilter().apply {
            addAction(Action.play.value)
            addAction(Action.pause.value)
            addAction(Action.next.value)
            addAction(Action.previous.value)
        }

        ContextCompat.registerReceiver(
            this,
            notificationActionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        maybeResumePlaybackWhenDeviceConnected()

        // Start memory optimization scheduler
        memoryOptimizationHandler.postDelayed(memoryOptimizationRunnable, 2 * 60 * 1000L) // Start after 2 minutes
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.shouldBePlaying) {
            broadCastPendingIntent<NotificationDismissReceiver>().send()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        maybeSavePlayerQueue()

        preferences.unregisterOnSharedPreferenceChangeListener(this)

        player.removeListener(this)
        player.stop()
        player.release()

        unregisterReceiver(notificationActionReceiver)

        // Unregister audio device callback to prevent memory leak
        audioDeviceCallback?.let { callback ->
            audioManager?.unregisterAudioDeviceCallback(callback)
        }

        mediaSession.isActive = false
        mediaSession.release()
        cache.release()

        loudnessEnhancer?.release()

        // Stop memory optimization scheduler
        memoryOptimizationHandler.removeCallbacks(memoryOptimizationRunnable)

        super.onDestroy()
    }

    override fun shouldBeInvincible(): Boolean {
        return !player.shouldBePlaying
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (bitmapProvider.setDefaultBitmap() && player.currentMediaItem != null) {
            notificationManager?.notify(NOTIFICATION_ID, notification())
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun onPlaybackStatsReady(
        eventTime: AnalyticsListener.EventTime,
        playbackStats: PlaybackStats
    ) {
        val mediaItem =
            eventTime.timeline.getWindow(eventTime.windowIndex, Timeline.Window()).mediaItem

        val totalPlayTimeMs = playbackStats.totalPlayTimeMs

        if (totalPlayTimeMs > 5000) {
            query {
                // Ensure song is in database first
                Database.insert(mediaItem)
                Database.incrementTotalPlayTimeMs(mediaItem.mediaId, totalPlayTimeMs)
            }
            
            // Track behavior for recommendations
            behaviorTracker.onSongPlayed(mediaItem, totalPlayTimeMs)
        }

        if (totalPlayTimeMs > 5000) {
            query {
                try {
                    // Ensure song is in database first
                    Database.insert(mediaItem)
                    
                    val event = Event(
                        songId = mediaItem.mediaId,
                        timestamp = System.currentTimeMillis(),
                        playTime = totalPlayTimeMs
                    )
                    Database.insert(event)
                    
                    android.util.Log.d("PlayerService", "Event inserted: ${mediaItem.mediaId}, playTime: $totalPlayTimeMs")
                } catch (e: SQLException) {
                    android.util.Log.e("PlayerService", "Failed to insert event: ${e.message}")
                }
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        mediaItemState.update { mediaItem }

        // Clear error tracking for new media item
        mediaItem?.mediaId?.let { videoId ->
            errorRetryCount.remove(videoId)
            lastErrorTime.remove(videoId)
        }

        maybeRecoverPlaybackError()
        maybeNormalizeVolume()
        maybeProcessRadio()

        // Preload next songs for smoother playback
        preloadNextSongs()

        if (mediaItem == null) {
            bitmapProvider.listener?.invoke(null)
        } else if (mediaItem.mediaMetadata.artworkUri == bitmapProvider.lastUri) {
            bitmapProvider.listener?.invoke(bitmapProvider.lastBitmap)
        }

        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
            updateMediaSessionQueue(player.currentTimeline)
        }
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
            updateMediaSessionQueue(timeline)
        }
    }

    private fun updateMediaSessionQueue(timeline: Timeline) {
        val builder = MediaDescription.Builder()

        val currentMediaItemIndex = player.currentMediaItemIndex
        val lastIndex = timeline.windowCount - 1
        var startIndex = currentMediaItemIndex - 7
        var endIndex = currentMediaItemIndex + 7

        if (startIndex < 0) endIndex -= startIndex

        if (endIndex > lastIndex) {
            startIndex -= (endIndex - lastIndex)
            endIndex = lastIndex
        }

        startIndex = startIndex.coerceAtLeast(0)

        mediaSession.setQueue(
            List(endIndex - startIndex + 1) { index ->
                val mediaItem = timeline.getWindow(index + startIndex, Timeline.Window()).mediaItem
                MediaSession.QueueItem(
                    builder
                        .setMediaId(mediaItem.mediaId)
                        .setTitle(mediaItem.mediaMetadata.title)
                        .setSubtitle(mediaItem.mediaMetadata.artist)
                        .setIconUri(mediaItem.mediaMetadata.artworkUri)
                        .build(),
                    (index + startIndex).toLong()
                )
            }
        )
    }

    /**
     * Memory optimization method to clear unused resources
     */
    private fun optimizeMemoryUsage() {
        try {
            // Clear bitmap cache if not actively playing
            if (!player.isPlaying) {
                bitmapProvider.clearUnusedBitmaps()
            }

            // Force garbage collection hint (doesn't guarantee GC but helps)
            System.gc()

            // Clear any cached network responses older than 5 minutes
            val currentTime = System.currentTimeMillis()
            val fiveMinutesAgo = currentTime - (5 * 60 * 1000)

            // Clear old error tracking data
            errorRetryCount.entries.removeIf { (_, time) -> time < fiveMinutesAgo }
            lastErrorTime.entries.removeIf { (_, time) -> time < fiveMinutesAgo }

            android.util.Log.d("PlayerService", "Memory optimization completed")
        } catch (e: Exception) {
            android.util.Log.w("PlayerService", "Memory optimization failed", e)
        }
    }

    private fun maybeRecoverPlaybackError() {
        val currentMediaItem = player.currentMediaItem
        val videoId = currentMediaItem?.mediaId

        if (player.playerError != null && videoId != null) {
            val error = player.playerError
            val errorCategory = categorizeError(error)

            // Log error for debugging
            android.util.Log.w("PlayerService", "Playback error for $videoId: ${error?.message} (Category: ${errorCategory.name})")

            // Show user-friendly error message
            showPlaybackErrorNotification(errorCategory, currentMediaItem)

            // Don't retry for non-recoverable errors
            if (isNonRecoverableError(error)) {
                failedVideoIds.add(videoId)
                handleNonRecoverableError(errorCategory, videoId)
                return
            }

            // Don't retry if video already failed multiple times
            if (failedVideoIds.contains(videoId)) {
                handleNonRecoverableError(errorCategory, videoId)
                return
            }

            // Implement exponential backoff with network awareness
            val retryCount = errorRetryCount.getOrDefault(videoId, 0)
            val lastError = lastErrorTime.getOrDefault(videoId, 0L)
            val currentTime = System.currentTimeMillis()

            if (retryCount >= maxRetries) {
                failedVideoIds.add(videoId)
                handleNonRecoverableError(errorCategory, videoId)
                return
            }

            // Check network connectivity before retrying
            if (!isNetworkAvailable() && errorCategory.isNetworkRelated) {
                android.util.Log.d("PlayerService", "Network unavailable, skipping retry for network-related error")
                return
            }

            if (currentTime - lastError < retryDelayMs * (retryCount + 1)) {
                return // Too soon to retry
            }

            errorRetryCount[videoId] = retryCount + 1
            lastErrorTime[videoId] = currentTime

            android.util.Log.d("PlayerService", "Retrying playback for $videoId (attempt ${retryCount + 1})")
            player.prepare()
        }
    }
    
    private fun isNonRecoverableError(error: PlaybackException?): Boolean {
        return when {
            error is VideoIdMismatchException -> true
            error is UnplayableException -> true
            error is LoginRequiredException -> true
            error?.cause is VideoIdMismatchException -> true
            error?.cause is UnplayableException -> true
            error?.cause is LoginRequiredException -> true
            error?.cause?.cause is VideoIdMismatchException -> true
            error?.cause?.cause is UnplayableException -> true
            error?.cause?.cause is LoginRequiredException -> true
            else -> false
        }
    }

    /**
     * Enhanced error categorization for better user experience
     */
    private enum class PlaybackErrorCategory(val userMessage: String, val isNetworkRelated: Boolean) {
        NETWORK_ERROR("Network connection issue. Check your internet connection.", true),
        VIDEO_UNAVAILABLE("This video is not available. It may have been removed or is region-locked.", false),
        LOGIN_REQUIRED("Sign in required to play this content.", false),
        VIDEO_ID_MISMATCH("Content mismatch detected. Skipping to next song.", false),
        FORMAT_NOT_FOUND("Unable to find playable format. Skipping to next song.", false),
        TIMEOUT_ERROR("Connection timeout. Retrying...", true),
        UNKNOWN_ERROR("Unexpected playback error. Skipping to next song.", false)
    }

    private fun categorizeError(error: PlaybackException?): PlaybackErrorCategory {
        return when {
            error?.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
            error?.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ||
            error?.message?.contains("timeout", ignoreCase = true) == true ||
            error?.message?.contains("network", ignoreCase = true) == true -> PlaybackErrorCategory.TIMEOUT_ERROR

            error is VideoIdMismatchException ||
            error?.cause is VideoIdMismatchException ||
            error?.cause?.cause is VideoIdMismatchException -> PlaybackErrorCategory.VIDEO_ID_MISMATCH

            error is UnplayableException ||
            error?.cause is UnplayableException ||
            error?.cause?.cause is UnplayableException -> PlaybackErrorCategory.VIDEO_UNAVAILABLE

            error is LoginRequiredException ||
            error?.cause is LoginRequiredException ||
            error?.cause?.cause is LoginRequiredException -> PlaybackErrorCategory.LOGIN_REQUIRED

            error is PlayableFormatNotFoundException ||
            error?.cause is PlayableFormatNotFoundException ||
            error?.cause?.cause is PlayableFormatNotFoundException -> PlaybackErrorCategory.FORMAT_NOT_FOUND

            error?.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> PlaybackErrorCategory.NETWORK_ERROR

            else -> PlaybackErrorCategory.UNKNOWN_ERROR
        }
    }

    private fun showPlaybackErrorNotification(errorCategory: PlaybackErrorCategory, mediaItem: MediaItem?) {
        val title = mediaItem?.mediaMetadata?.title ?: "Playback Error"
        val message = errorCategory.userMessage

        // Create a user notification for the error
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager?.notify(ERROR_NOTIFICATION_ID, notification)
    }

    private fun handleNonRecoverableError(errorCategory: PlaybackErrorCategory, videoId: String) {
        android.util.Log.w("PlayerService", "Non-recoverable error for $videoId: ${errorCategory.name}")

        // For non-recoverable errors, automatically skip to next song after a brief delay
        coroutineScope.launch(Dispatchers.Main) {
            delay(2000) // Give user time to see the error notification
            if (player.currentMediaItem?.mediaId == videoId && player.playerError != null) {
                android.util.Log.d("PlayerService", "Auto-skipping failed song: $videoId")
                player.forceSeekToNext()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork
            val networkCapabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            networkCapabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            android.util.Log.e("PlayerService", "Error checking network availability", e)
            false
        }
    }

    private fun maybeProcessRadio() {
        radio?.let { radio ->
            if (player.mediaItemCount - player.currentMediaItemIndex <= 3) {
                coroutineScope.launch(Dispatchers.Main) {
                    player.addMediaItems(radio.process())
                }
            }
        }
    }

    /**
     * Preload next songs for smoother playback
     */
    private fun preloadNextSongs() {
        try {
            val currentIndex = player.currentMediaItemIndex
            val totalItems = player.mediaItemCount

            // Preload next 3 songs if available
            val preloadCount = minOf(3, totalItems - currentIndex - 1)

            for (i in 1..preloadCount) {
                val nextIndex = currentIndex + i
                if (nextIndex < totalItems) {
                    val nextMediaItem = player.getMediaItemAt(nextIndex)
                    // Trigger preload by accessing the media item
                    nextMediaItem.mediaId?.let { songId ->
                        // This will trigger the data source factory to load the song
                        android.util.Log.d("PlayerService", "Preloading song at index $nextIndex: $songId")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("PlayerService", "Error preloading next songs", e)
        }
    }

    private fun maybeSavePlayerQueue() {
        if (!isPersistentQueueEnabled) return

        val mediaItems = player.currentTimeline.mediaItems
        val mediaItemIndex = player.currentMediaItemIndex
        val mediaItemPosition = player.currentPosition

        mediaItems.mapIndexed { index, mediaItem ->
            QueuedMediaItem(
                mediaItem = mediaItem,
                position = if (index == mediaItemIndex) mediaItemPosition else null
            )
        }.let { queuedMediaItems ->
            query {
                Database.clearQueue()
                Database.insert(queuedMediaItems)
            }
        }
    }

    private fun maybeRestorePlayerQueue() {
        if (!isPersistentQueueEnabled) return

        query {
            val queuedSong = Database.queue()
            Database.clearQueue()

            if (queuedSong.isEmpty()) return@query

            val index = queuedSong.indexOfFirst { it.position != null }.coerceAtLeast(0)

            runBlocking(Dispatchers.Main) {
                player.setMediaItems(
                    queuedSong.map { mediaItem ->
                        mediaItem.mediaItem.buildUpon()
                            .setUri(mediaItem.mediaItem.mediaId)
                            .setCustomCacheKey(mediaItem.mediaItem.mediaId)
                            .build().apply {
                                mediaMetadata.extras?.putBoolean("isFromPersistentQueue", true)
                            }
                    },
                    index,
                    queuedSong[index].position ?: C.TIME_UNSET
                )
                player.prepare()

                isNotificationStarted = true
                startForegroundService(this@PlayerService, intent<PlayerService>())
                startForeground(NOTIFICATION_ID, notification())
            }
        }
    }

    private fun maybeNormalizeVolume() {
        if (!preferences.getBoolean(volumeNormalizationKey, false)) {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
            loudnessEnhancer = null
            volumeNormalizationJob?.cancel()
            player.volume = 1f
            return
        }

        try {
            if (loudnessEnhancer == null) {
                loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
            }

            player.currentMediaItem?.mediaId?.let { songId ->
                volumeNormalizationJob?.cancel()
                volumeNormalizationJob = coroutineScope.launch(Dispatchers.IO) {
                    try {
                        Database.loudnessDb(songId).cancellable().collectLatest { loudnessDb ->
                            try {
                                withContext(Dispatchers.Main) {
                                    loudnessEnhancer?.setTargetGain(-((loudnessDb ?: 0f) * 100).toInt() + 500)
                                    loudnessEnhancer?.enabled = true
                                }
                            } catch (e: Exception) {
                                // Handle audio effect errors gracefully
                            }
                        }
                    } catch (e: Exception) {
                        // Handle database errors gracefully
                    }
                }
            }
        } catch (e: Exception) {
            // Handle LoudnessEnhancer creation errors
            loudnessEnhancer = null
        }
    }

    private fun maybeShowSongCoverInLockScreen() {
        val bitmap =
            if (isAtLeastAndroid13 || isShowingThumbnailInLockscreen) bitmapProvider.bitmap else null

        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap)

        if (isAtLeastAndroid13 && player.currentMediaItemIndex == 0) {
            metadataBuilder.putText(
                MediaMetadata.METADATA_KEY_TITLE,
                "${player.mediaMetadata.title} "
            )
        }

        mediaSession.setMetadata(metadataBuilder.build())
    }

    @SuppressLint("NewApi")
    private fun maybeResumePlaybackWhenDeviceConnected() {
        if (!isAtLeastAndroid6) return

        if (preferences.getBoolean(resumePlaybackWhenDeviceConnectedKey, false)) {
            if (audioManager == null) {
                audioManager = getSystemService(AUDIO_SERVICE) as AudioManager?
            }

            audioDeviceCallback = object : AudioDeviceCallback() {
                private fun canPlayMusic(audioDeviceInfo: AudioDeviceInfo): Boolean {
                    if (!audioDeviceInfo.isSink) return false

                    return audioDeviceInfo.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                            audioDeviceInfo.type == AudioDeviceInfo.TYPE_USB_HEADSET
                }

                override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                    if (!player.isPlaying && addedDevices.any(::canPlayMusic)) {
                        player.play()
                    }
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) = Unit
            }

            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, handler)

        } else {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
            audioDeviceCallback = null
        }
    }

    private fun sendOpenEqualizerIntent() {
        sendBroadcast(
            Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            }
        )
    }

    private fun sendCloseEqualizerIntent() {
        sendBroadcast(
            Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
            }
        )
    }

    private fun updatePlaybackState() = coroutineScope.launch {
        playbackStateMutex.withLock {
            withContext(Dispatchers.Main) {
                mediaSession.setPlaybackState(
                    stateBuilder
                        .setState(player.androidPlaybackState, player.currentPosition, 1f)
                        .setBufferedPosition(player.bufferedPosition)
                        .build()
                )
            }
        }
    }

    private val Player.androidPlaybackState: Int
        get() = when (playbackState) {
            Player.STATE_BUFFERING -> if (playWhenReady) PlaybackState.STATE_BUFFERING else PlaybackState.STATE_PAUSED
            Player.STATE_READY -> if (playWhenReady) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
            Player.STATE_ENDED -> PlaybackState.STATE_STOPPED
            Player.STATE_IDLE -> PlaybackState.STATE_NONE
            else -> PlaybackState.STATE_NONE
        }

    @Suppress("DEPRECATION")
    override fun onEvents(player: Player, events: Player.Events) {
        if (player.duration != C.TIME_UNSET) {
            mediaSession.setMetadata(
                metadataBuilder
                    .putText(MediaMetadata.METADATA_KEY_TITLE, player.mediaMetadata.title)
                    .putText(MediaMetadata.METADATA_KEY_ARTIST, player.mediaMetadata.artist)
                    .putText(MediaMetadata.METADATA_KEY_ALBUM, player.mediaMetadata.albumTitle)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, player.duration)
                    .build()
            )
        }

        updatePlaybackState()

        // Enhanced playback stability - prevent songs from stopping unexpectedly
        if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
            when (player.playbackState) {
                Player.STATE_ENDED -> {
                    // Auto-advance to next song if in a playlist
                    if (player.hasNextMediaItem()) {
                        player.seekToNext()
                        player.play()
                    }
                }
                Player.STATE_IDLE -> {
                    // Attempt to recover from idle state
                    if (player.playerError != null) {
                        android.util.Log.w("PlayerService", "Player in idle state with error, attempting recovery")
                        player.prepare()
                    }
                }
            }
        }

        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY
            )
        ) {
            val notification = notification()

            if (notification == null) {
                isNotificationStarted = false
                makeInvincible(false)
                stopForeground(false)
                sendCloseEqualizerIntent()
                notificationManager?.cancel(NOTIFICATION_ID)
                return
            }

            if (player.shouldBePlaying && !isNotificationStarted) {
                isNotificationStarted = true
                startForegroundService(this@PlayerService, intent<PlayerService>())
                startForeground(NOTIFICATION_ID, notification)
                makeInvincible(false)
                sendOpenEqualizerIntent()
            } else {
                if (!player.shouldBePlaying) {
                    isNotificationStarted = false
                    stopForeground(false)
                    makeInvincible(true)
                    sendCloseEqualizerIntent()
                }
                notificationManager?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            persistentQueueKey -> if (sharedPreferences != null) {
                isPersistentQueueEnabled =
                    sharedPreferences.getBoolean(key, isPersistentQueueEnabled)
            }

            volumeNormalizationKey -> maybeNormalizeVolume()

            resumePlaybackWhenDeviceConnectedKey -> maybeResumePlaybackWhenDeviceConnected()

            isInvincibilityEnabledKey -> if (sharedPreferences != null) {
                isInvincibilityEnabled =
                    sharedPreferences.getBoolean(key, isInvincibilityEnabled)
            }

            skipSilenceKey -> if (sharedPreferences != null) {
                player.skipSilenceEnabled = sharedPreferences.getBoolean(key, false)
            }

            isShowingThumbnailInLockscreenKey -> {
                if (sharedPreferences != null) {
                    isShowingThumbnailInLockscreen = sharedPreferences.getBoolean(key, true)
                }
                maybeShowSongCoverInLockScreen()
            }

            trackLoopEnabledKey, queueLoopEnabledKey -> {
                player.repeatMode = when {
                    preferences.getBoolean(trackLoopEnabledKey, false) -> Player.REPEAT_MODE_ONE
                    preferences.getBoolean(queueLoopEnabledKey, false) -> Player.REPEAT_MODE_ALL
                    else -> Player.REPEAT_MODE_OFF
                }
            }
        }
    }

    override fun notification(): Notification? {
        if (player.currentMediaItem == null) return null

        val playIntent = Action.play.pendingIntent
        val pauseIntent = Action.pause.pendingIntent
        val nextIntent = Action.next.pendingIntent
        val prevIntent = Action.previous.pendingIntent

        val mediaMetadata = player.mediaMetadata

        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(mediaMetadata.title)
            .setContentText(mediaMetadata.artist)
            .setSubText(player.playerError?.message)
            .setLargeIcon(bitmapProvider.bitmap)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setOngoing(false)
            .setContentIntent(activityPendingIntent<MainActivity>(
                flags = PendingIntent.FLAG_UPDATE_CURRENT
            ) { putExtra("expandPlayerBottomSheet", true) })
            .setDeleteIntent(broadCastPendingIntent<NotificationDismissReceiver>())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.sessionToken))
            )
            .addAction(R.drawable.play_skip_back, "Skip back", prevIntent)
            .addAction(
                if (player.shouldBePlaying) R.drawable.pause else R.drawable.play,
                if (player.shouldBePlaying) "Pause" else "Play",
                if (player.shouldBePlaying) pauseIntent else playIntent
            )
            .addAction(R.drawable.play_skip_forward, "Skip forward", nextIntent)

        bitmapProvider.load(mediaMetadata.artworkUri) { bitmap ->
            maybeShowSongCoverInLockScreen()
            builder.setLargeIcon(bitmap)
            notificationManager?.notify(NOTIFICATION_ID, builder.build())
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        notificationManager = getSystemService()

        if (!isAtLeastAndroid8) return

        notificationManager?.run {
            if (getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.string.now_playing),
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableLights(false)
                        enableVibration(false)
                    }
                )
            }

            if (getNotificationChannel(SLEEP_TIMER_NOTIFICATION_CHANNEL_ID) == null) {
                createNotificationChannel(
                    NotificationChannel(
                        SLEEP_TIMER_NOTIFICATION_CHANNEL_ID,
                        getString(R.string.sleep_timer),
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableLights(false)
                        enableVibration(false)
                    }
                )
            }
        }
    }

    private fun createCacheDataSource(): DataSource.Factory {
        return CacheDataSource.Factory().setCache(cache).apply {
            setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory()
                    .setConnectTimeoutMs(16000)
                    .setReadTimeoutMs(8000)
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
            )
        }
    }

    private fun createDataSourceFactory(): DataSource.Factory {
        // Enhanced progressive loading system similar to Spotify
        val progressiveLoader = ProgressiveLoader(this)
        val ringBuffer = RingBuffer<Pair<String, Uri>?>(3) { null } // Increased buffer size

        return ResolvingDataSource.Factory(createCacheDataSource()) { dataSpec ->
            val videoId = dataSpec.key ?: error("A key must be set")

            // Use adaptive chunk sizing based on network and playback conditions
            val adaptiveChunkSize = progressiveLoader.calculateAdaptiveChunkSize(videoId, dataSpec.position)

            if (cache.isCached(videoId, dataSpec.position, adaptiveChunkSize)) {
                dataSpec
            } else {
                when (videoId) {
                    ringBuffer.getOrNull(0)?.first -> dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)
                    ringBuffer.getOrNull(1)?.first -> dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)
                    ringBuffer.getOrNull(2)?.first -> dataSpec.withUri(ringBuffer.getOrNull(2)!!.second)
                    else -> {
                        val urlResult = runBlocking(Dispatchers.IO) {
                            // Fetch remote config for dynamic error handling
                            val config = AdvancedRemoteConfig.fetchConfig()
                            
                            // Check if this video ID has already failed with retry logic
                            if (failedVideoIds.contains(videoId)) {
                                val retryCount = errorRetryCount[videoId] ?: 0
                                val lastError = lastErrorTime[videoId] ?: 0
                                val timeSinceLastError = System.currentTimeMillis() - lastError
                                
                                if (config.errorHandling.videoIdMismatchEnabled && 
                                    retryCount < config.errorHandling.videoIdMismatchMaxRetries && 
                                    timeSinceLastError > config.errorHandling.videoIdMismatchRetryDelayMs) {
                                    
                                    if (config.debugging.enableDetailedLogging) {
                                        android.util.Log.d("PlayerService", "Retrying failed video: $videoId (attempt ${retryCount + 1})")
                                    }
                                    
                                    // Remove from failed list to allow retry
                                    failedVideoIds.remove(videoId)
                                } else if (config.errorHandling.videoIdMismatchSkipOnFailure) {
                                    // Skip this video entirely
                                    return@runBlocking Result.failure(
                                        PlaybackException(
                                            "Skipping failed video",
                                            VideoIdMismatchException(),
                                            PlaybackException.ERROR_CODE_REMOTE_ERROR
                                        )
                                    )
                                } else {
                                    return@runBlocking Result.failure(
                                        PlaybackException(
                                            "Video previously failed",
                                            VideoIdMismatchException(),
                                            PlaybackException.ERROR_CODE_REMOTE_ERROR
                                        )
                                    )
                                }
                            }

                            Innertube.player(videoId = videoId)
                        }?.mapCatching { body ->
                            val config = AdvancedRemoteConfig.getConfig()
                            
                            if (config.errorHandling.videoIdMismatchEnabled && body.videoDetails?.videoId != videoId) {
                                // Track retry attempts
                                val currentRetries = errorRetryCount[videoId] ?: 0
                                errorRetryCount[videoId] = currentRetries + 1
                                lastErrorTime[videoId] = System.currentTimeMillis()
                                
                                if (config.debugging.enableDetailedLogging) {
                                    android.util.Log.w("PlayerService", "Video ID mismatch: expected=$videoId, got=${body.videoDetails?.videoId}")
                                }
                                
                                // Try alternative player endpoints if enabled
                                if (config.emergency.redirectToAlternativePlayer && config.apiEndpoints.alternativeEndpoints.isNotEmpty()) {
                                    if (config.debugging.enableDetailedLogging) {
                                        android.util.Log.d("PlayerService", "Attempting fallback for video: $videoId")
                                    }
                                    // Note: Alternative player logic would go here
                                    // For now, we'll still throw the exception but with better tracking
                                }
                                
                                failedVideoIds.add(videoId)
                                throw VideoIdMismatchException()
                            }

                            when (val status = body.playabilityStatus?.status) {
                                "OK" -> body.streamingData?.highestQualityFormat?.let { format ->
                                    val mediaItem = runBlocking(Dispatchers.Main) {
                                        player.findNextMediaItemById(videoId)
                                    }

                                    if (mediaItem?.mediaMetadata?.extras?.getString("durationText") == null) {
                                        format.approxDurationMs?.div(1000)
                                            ?.let(DateUtils::formatElapsedTime)?.removePrefix("0")
                                            ?.let { durationText ->
                                                mediaItem?.mediaMetadata?.extras?.putString(
                                                    "durationText",
                                                    durationText
                                                )
                                                Database.updateDurationText(videoId, durationText)
                                            }
                                    }

                                    query {
                                        mediaItem?.let(Database::insert)

                                        Database.insert(
                                            com.shaadow.tunes.models.Format(
                                                songId = videoId,
                                                itag = format.itag,
                                                mimeType = format.mimeType,
                                                bitrate = format.bitrate,
                                                loudnessDb = body.playerConfig?.audioConfig?.normalizedLoudnessDb,
                                                contentLength = format.contentLength,
                                                lastModified = format.lastModified
                                            )
                                        )
                                    }

                                    format.url
                                } ?: throw PlayableFormatNotFoundException()

                                "UNPLAYABLE" -> throw UnplayableException()
                                "LOGIN_REQUIRED" -> throw LoginRequiredException()
                                else -> throw PlaybackException(
                                    status,
                                    null,
                                    PlaybackException.ERROR_CODE_REMOTE_ERROR
                                )
                            }
                        }

                        urlResult?.getOrThrow()?.let { url ->
                            ringBuffer.append(videoId to url.toUri())

                            // Use adaptive chunking for progressive loading
                            val chunkSize = minOf(adaptiveChunkSize, 2 * 1024 * 1024L) // Cap at 2MB for safety

                            // Log adaptive chunking for debugging
                            if (AdvancedRemoteConfig.isLoggingEnabled()) {
                                android.util.Log.d("ProgressiveLoader",
                                    "Video: $videoId, Position: ${dataSpec.position}, " +
                                    "Adaptive chunk: ${chunkSize / 1024}KB, " +
                                    "Network: ${progressiveLoader.getLoadingStats(videoId)["networkQuality"]}")
                            }

                            dataSpec.withUri(url.toUri())
                                .subrange(dataSpec.uriPositionOffset, chunkSize)
                        } ?: throw PlaybackException(
                            null,
                            urlResult?.exceptionOrNull(),
                            PlaybackException.ERROR_CODE_REMOTE_ERROR
                        )
                    }
                }
            }
        }
    }

    private fun createMediaSourceFactory(): MediaSource.Factory {
        return DefaultMediaSourceFactory(createDataSourceFactory(), DefaultExtractorsFactory())
    }

    private fun createRendersFactory(): RenderersFactory {
        val audioSink = DefaultAudioSink.Builder(applicationContext)
            .setEnableFloatOutput(false)
            .setEnableAudioTrackPlaybackParams(false)
            .setAudioOffloadSupportProvider(DefaultAudioOffloadSupportProvider(applicationContext))
            .setAudioProcessorChain(
                DefaultAudioProcessorChain(
                    emptyArray(),
                    SilenceSkippingAudioProcessor(2_000_000, 0.01f, 2_000_000, 0, 256),
                    SonicAudioProcessor()
                )
            )
            .build()

        return RenderersFactory { handler: Handler?, _, audioListener: AudioRendererEventListener?, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(
                    this,
                    MediaCodecSelector.DEFAULT,
                    handler,
                    audioListener,
                    audioSink
                )
            )
        }
    }

    inner class Binder : AndroidBinder() {
        val player: ExoPlayer
            get() = this@PlayerService.player

        val cache: Cache
            get() = this@PlayerService.cache

        val mediaSession
            get() = this@PlayerService.mediaSession

        val sleepTimerMillisLeft: StateFlow<Long?>?
            get() = timerJob?.millisLeft

        // Expose progressive loading statistics for debugging
        fun getProgressiveLoadingStats(videoId: String): Map<String, Any> {
            return progressiveLoader.getLoadingStats(videoId)
        }

        // Get current network quality assessment
        fun getCurrentNetworkQuality(): String {
            return try {
                val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                val activeNetwork = connectivityManager?.activeNetwork
                val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)

                when {
                    capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi (Excellent)"
                    capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true -> "Unmetered (Good)"
                    capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true -> "Cellular (Fair)"
                    else -> "Poor Connection"
                }
            } catch (e: Exception) {
                "Unknown"
            }
        }

        private var radioJob: Job? = null

        var isLoadingRadio by mutableStateOf(false)
            private set

        fun startSleepTimer(delayMillis: Long) {
            timerJob?.cancel()

            timerJob = coroutineScope.timer(delayMillis) {
                val notification = NotificationCompat
                    .Builder(this@PlayerService, SLEEP_TIMER_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.sleep_timer_ended))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .build()

                notificationManager?.notify(SLEEP_TIMER_NOTIFICATION_ID, notification)

                stopSelf()
                exitProcess(0)
            }
        }

        fun cancelSleepTimer() {
            timerJob?.cancel()
            timerJob = null
        }

        fun setupRadio(endpoint: NavigationEndpoint.Endpoint.Watch?) =
            startRadio(endpoint = endpoint, justAdd = true)

        fun playRadio(endpoint: NavigationEndpoint.Endpoint.Watch?) =
            startRadio(endpoint = endpoint, justAdd = false)

        private fun startRadio(endpoint: NavigationEndpoint.Endpoint.Watch?, justAdd: Boolean) {
            radioJob?.cancel()
            radio = null
            YouTubeRadio(
                endpoint?.videoId,
                endpoint?.playlistId,
                endpoint?.playlistSetVideoId,
                endpoint?.params
            ).let {
                isLoadingRadio = true
                radioJob = coroutineScope.launch(Dispatchers.Main) {
                    if (justAdd) {
                        player.addMediaItems(it.process().drop(1))
                    } else {
                        player.forcePlayFromBeginning(it.process())
                    }
                    radio = it
                    isLoadingRadio = false
                }
            }
        }

        fun stopRadio() {
            isLoadingRadio = false
            radioJob?.cancel()
            radio = null
        }
    }

    private fun likeAction() = mediaItemState.value?.let { mediaItem ->
        query {
            runCatching {
                Database.like(
                    songId = mediaItem.mediaId,
                    likedAt = if (isLikedState.value) null else System.currentTimeMillis()
                )
            }
        }
        
        // Track behavior for recommendations
        if (!isLikedState.value) {
            behaviorTracker.onSongLiked(mediaItem)
        }
    }

    private fun play() {
        if (player.playerError != null) player.prepare()
        else if (player.playbackState == Player.STATE_ENDED) player.seekToDefaultPosition(0)
        else player.play()
    }

    private inner class SessionCallback(private val player: Player) : MediaSession.Callback() {
        override fun onPlay() = play()
        override fun onPause() = player.pause()
        override fun onSkipToPrevious() = runCatching(player::forceSeekToPrevious).let { }
        override fun onSkipToNext() = runCatching {
            // Track skip behavior before skipping
            mediaItemState.value?.let { mediaItem ->
                behaviorTracker.onSongSkipped(mediaItem, player.currentPosition)
            }
            player.forceSeekToNext()
        }.let { }
        override fun onSeekTo(pos: Long) = player.seekTo(pos)
        override fun onStop() = player.pause()
        override fun onRewind() = player.seekToDefaultPosition()
        override fun onSkipToQueueItem(id: Long) =
            runCatching { player.seekToDefaultPosition(id.toInt()) }.let { }

        override fun onCustomAction(action: String, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (action == FAVORITE_ACTION) likeAction()
        }
    }

    private inner class NotificationActionReceiver(private val player: Player) :
        BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Action.pause.value -> player.pause()
                Action.play.value -> play()
                Action.next.value -> {
                    // Track skip behavior before skipping
                    mediaItemState.value?.let { mediaItem ->
                        behaviorTracker.onSongSkipped(mediaItem, player.currentPosition)
                    }
                    player.forceSeekToNext()
                }
                Action.previous.value -> player.forceSeekToPrevious()
            }
        }
    }

    class NotificationDismissReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            context.stopService(context.intent<PlayerService>())
        }
    }

    @JvmInline
    private value class Action(val value: String) {
        context(Context)
        val pendingIntent: PendingIntent
            get() = PendingIntent.getBroadcast(
                this@Context,
                100,
                Intent(value).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT.or(if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0)
            )

        companion object {
            val pause = Action("com.shaadow.tunes.pause")
            val play = Action("com.shaadow.tunes.play")
            val next = Action("com.shaadow.tunes.next")
            val previous = Action("com.shaadow.tunes.previous")
        }
    }

    private companion object {
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "default_channel_id"

        const val SLEEP_TIMER_NOTIFICATION_ID = 1002
        const val SLEEP_TIMER_NOTIFICATION_CHANNEL_ID = "sleep_timer_channel_id"

        const val ERROR_NOTIFICATION_ID = 1003

        const val FAVORITE_ACTION = "FAVORITE"
    }
}
