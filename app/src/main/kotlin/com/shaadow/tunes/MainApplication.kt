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
import com.shaadow.innertube.requests.visitorData
import com.shaadow.tunes.enums.CoilDiskCacheMaxSize
import com.shaadow.tunes.utils.coilDiskCacheMaxSizeKey
import com.shaadow.tunes.utils.getEnum
import com.shaadow.tunes.utils.preferences
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainApplication : Application(), SingletonImageLoader.Factory {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer()

        FirebaseApp.initializeApp(this)

        GlobalScope.launch {
            if (Innertube.visitorData.isNullOrBlank()) Innertube.visitorData =
                Innertube.visitorData().getOrNull()
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