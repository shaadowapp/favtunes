package com.shaadow.tunes

import android.app.Application
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.requests.relatedPage
import com.shaadow.innertube.requests.visitorData
import com.shaadow.tunes.enums.CoilDiskCacheMaxSize
import com.shaadow.tunes.notification.FavTunesNotificationManager
import com.shaadow.tunes.utils.ActivityDiagnostics
import com.shaadow.tunes.utils.AdvancedRemoteConfig
import com.shaadow.tunes.utils.coilDiskCacheMaxSizeKey
import com.shaadow.tunes.utils.getEnum
import com.shaadow.tunes.utils.preferences
import com.shaadow.tunes.utils.SecureCredentialsManager
import com.shaadow.tunes.utils.SecureInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.app.NotificationManager as SystemNotificationManager

class MainApplication : Application(), SingletonImageLoader.Factory {
    // Application-scoped coroutine scope that will be cancelled when the application is destroyed
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    


    override fun onCreate() {
        super.onCreate()

        // Initialize secure credentials manager
        val credentialsManager = com.shaadow.tunes.utils.SecureCredentialsManager.getInstance(this)
        
        // Initialize credentials securely on first launch
        SecureInitializer.initializeCredentials(this)
        
        // Set production logging level (no verbose logging in production)
        OneSignal.Debug.logLevel = LogLevel.WARN
        
        // Initialize OneSignal with securely stored App ID
        OneSignal.initWithContext(this, credentialsManager.getOneSignalAppId())
        
        // Initialize Innertube with secure API key
        com.shaadow.innertube.Innertube.setApiKey(credentialsManager.getYouTubeApiKey())
        
        // Initialize advanced remote config for complete control
        AdvancedRemoteConfig.initialize(this)
        
        // Initialize emergency config trigger system
        com.shaadow.tunes.utils.EmergencyConfigTrigger.initialize(this)
        
        // Request notification permission only if OneSignal is properly configured
        applicationScope.launch {
            val credentialsManager = com.shaadow.tunes.utils.SecureCredentialsManager.getInstance(this@MainApplication)
            if (credentialsManager.areCredentialsConfigured()) {
                // Request permission with user consent (not forced)
                OneSignal.Notifications.requestPermission(false)
            }
        }
        
        // Log system information for debugging
        ActivityDiagnostics.logSystemInfo(this)
        
        // Verify activities are properly registered and accessible
        if (!ActivityDiagnostics.verifyActivities(this)) {
            // If activities can't be found, try clearing cache
            ActivityDiagnostics.clearAppCache(this)
        }
        
        // Initialize the global Database object
        try {
            Database.initialize(this)
        } catch (e: Exception) {
            // Log database initialization error but don't crash
            android.util.Log.e("MainApplication", "Database initialization failed", e)
        }

        FirebaseApp.initializeApp(this)
        
        // Create notification channels first
        createNotificationChannels()
        
        // Initialize simple notification system
        val notificationManager = FavTunesNotificationManager(this)
        
        applicationScope.launch {
            // Only initialize notifications if user has granted permission and credentials are configured
            val credentialsManager = com.shaadow.tunes.utils.SecureCredentialsManager.getInstance(this@MainApplication)
            if (credentialsManager.areCredentialsConfigured()) {
                // Schedule minimal, user-friendly notifications
                // Reduced frequency to comply with Play Store policies
                notificationManager.scheduleReducedEngagementNotifications()
                notificationManager.scheduleWeeklySuggestions() // Changed from daily to weekly
                
                android.util.Log.d("MainApplication", "Notification system initialized with reduced frequency")
            } else {
                android.util.Log.d("MainApplication", "Notifications disabled - credentials not configured")
            }
        }

        // Preload critical data immediately
        applicationScope.launch {
            // Initialize visitor data
            try {
                if (Innertube.visitorData.isNullOrBlank()) {
                    Innertube.visitorData().onSuccess { visitorData ->
                        Innertube.visitorData = visitorData
                    }
                }
            } catch (e: Exception) {
                // Silently handle visitor data initialization errors
            }
            
            // Preload trending song for QuickPicks
            try {
                val song = Database.trending().first()
                if (song != null) {
                    // Preload related page for the trending song
                    Innertube.relatedPage(videoId = song.id)
                }
            } catch (e: Exception) {
                // Silently handle errors to not block app startup
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Cancel all coroutines when application terminates
        applicationScope.cancel()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Cancel non-critical coroutines on low memory
        applicationScope.cancel()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            // Cancel coroutines when memory pressure is high
            applicationScope.cancel()
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotificationManager
            
            val channels = listOf(
                NotificationChannel(
                    "general_notifications",
                    "General Notifications",
                    SystemNotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Essential app notifications"
                    enableVibration(false)
                    setShowBadge(false)
                },
                NotificationChannel(
                    "music_suggestions",
                    "Music Suggestions",
                    SystemNotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Optional music recommendations"
                    enableVibration(false)
                    setShowBadge(false)
                },
                NotificationChannel(
                    "engagement_reminders",
                    "Engagement Reminders",
                    SystemNotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = "Optional reminders (can be disabled)"
                    enableVibration(false)
                    setShowBadge(false)
                },
                NotificationChannel(
                    "marketing_fun",
                    "Fun Updates",
                    SystemNotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = "Optional updates (can be disabled)"
                    enableVibration(false)
                    setShowBadge(false)
                }
            )
            
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .diskCache(
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil"))
                    .maxSizeBytes(
                        preferences.getEnum(
                            coilDiskCacheMaxSizeKey,
                            CoilDiskCacheMaxSize.`128MB`
                        ).bytes
                    )
                    .build()
            )
            .build()
    }
}