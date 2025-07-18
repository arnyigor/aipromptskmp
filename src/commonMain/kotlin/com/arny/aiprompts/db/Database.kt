package com.arny.aiprompts.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/**
 * Общая функция для создания экземпляра базы данных.
 * Она принимает платформенный builder и применяет общие настройки.
 */
fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        // Используем драйвер, который поставляется вместе с Room, для консистентности
        .setDriver(BundledSQLiteDriver())
        // Указываем, что запросы должны выполняться в фоновом потоке
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
