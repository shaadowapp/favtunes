package com.shaadow.tunes

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade
import com.google.firebase.FirebaseApp
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.requests.relatedPage
import com.shaadow.innertube.requests.visitorData
import com.shaadow.tunes.Database
import com.shaadow.tunes.enums.CoilDiskCacheMaxSize
import com.shaadow.tunes.utils.ActivityDiagnostics
import com.shaadow.tunes.utils.coilDiskCacheMaxSizeKey
import com.shaadow.tunes.utils.getEnum
import com.shaadow.tunes.utils.preferences
import com.shaadow.tunes.notification.FavTunesNotificationManager
import com.shaadow.tunes.notification.NotificationService
import com.shaadow.tunes.notification.NotificationThrottleMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.app.NotificationChannel
import android.app.NotificationManager as SystemNotificationManager
import android.content.Context
import android.os.Build
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class MainApplication : Application(), SingletonImageLoader.Factory {
    // Application-scoped coroutine scope that will be cancelled when the application is destroyed
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    


    override fun onCreate() {
        super.onCreate()

        // Enable verbose logging for debugging (remove in production)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, "3190b63e-f333-446b-bbba-7712efa18bb9")
        // Prompt for push notifications (recommended to use In-App Message in production)
        applicationScope.launch {
            OneSignal.Notifications.requestPermission(true)
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
        
        // Initialize notification system with throttling
        val notificationManager = FavTunesNotificationManager(this)
        val throttleMonitor = NotificationThrottleMonitor(this)
        
        applicationScope.launch {
            // Log current throttle status for debugging
            throttleMonitor.logThrottleStatus()
            
            // ONLY SCHEDULE workers, don't deliver notifications immediately on app start
            notificationManager.scheduleEngagementNotifications()
            notificationManager.scheduleDailySuggestions()
            // Don't call scheduleMarketingNotification() here as it can deliver immediately
            
            // Log summary after scheduling
            android.util.Log.d("MainApplication", throttleMonitor.getThrottleSummary())
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
                    SystemNotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "General app notifications"
                },
                NotificationChannel(
                    "music_suggestions",
                    "Music Suggestions",
                    SystemNotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Personalized music recommendations"
                },
                NotificationChannel(
                    "engagement_reminders",
                    "Engagement Reminders",
                    SystemNotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Gentle reminders to discover new music"
                },
                NotificationChannel(
                    "marketing_fun",
                    "Fun Updates",
                    SystemNotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Witty updates and music humor"
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