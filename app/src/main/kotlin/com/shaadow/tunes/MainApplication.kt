package com.shaadow.tunes

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade
import com.google.firebase.FirebaseApp
import com.shaadow.tunes.enums.CoilDiskCacheMaxSize
import com.shaadow.tunes.utils.coilDiskCacheMaxSizeKey
import com.shaadow.tunes.utils.getEnum
import com.shaadow.tunes.utils.preferences

class MainApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer()

        FirebaseApp.initializeApp(this)

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