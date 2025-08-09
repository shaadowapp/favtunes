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
import com.shaadow.tunes.DatabaseInitializer
import com.shaadow.tunes.enums.CoilDiskCacheMaxSize
import com.shaadow.tunes.utils.coilDiskCacheMaxSizeKey
import com.shaadow.tunes.utils.getEnum
import com.shaadow.tunes.utils.preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainApplication : Application(), SingletonImageLoader.Factory {
    // Application-scoped coroutine scope that will be cancelled when the application is destroyed
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        with(this) { DatabaseInitializer() }

        FirebaseApp.initializeApp(this)

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