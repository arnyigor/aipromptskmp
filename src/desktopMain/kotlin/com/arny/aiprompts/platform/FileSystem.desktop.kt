package com.arny.aiprompts.platform

import okio.FileSystem
import okio.Path

actual fun getCacheDir(): Path {
    // Используем временную директорию системы
    return FileSystem.SYSTEM_TEMPORARY_DIRECTORY
}