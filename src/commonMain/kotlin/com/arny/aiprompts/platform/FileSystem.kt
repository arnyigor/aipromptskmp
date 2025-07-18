package com.arny.aiprompts.platform

import okio.Path

/**
 * Ожидаемая функция, которая должна вернуть путь к директории кэша на текущей платформе.
 */
expect fun getCacheDir(): Path