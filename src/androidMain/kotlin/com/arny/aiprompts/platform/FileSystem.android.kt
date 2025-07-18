package com.arny.aiprompts.platform

import android.content.Context
import okio.Path
import okio.Path.Companion.toPath
import org.koin.mp.KoinPlatform.getKoin

// Нужен Context, его можно получить через Koin
actual fun getCacheDir(): Path {
    val context: Context = getKoin().get()
    return context.cacheDir.absolutePath.toPath()
}