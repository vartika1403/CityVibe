package com.cityvibe.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger

/**
 * Provides the app-wide Coil ImageLoader. The DebugLogger prints the reason for
 * any failed image load to Logcat under the "coil" tag (filter: `adb logcat -s coil`),
 * which is the quickest way to see why an image isn't showing.
 */
class CityVibeApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .logger(DebugLogger())
            .build()
}
