package com.arny.aiprompts.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // 1. Определяем папку для данных нашего приложения в домашней директории пользователя
    val dbFolder = File(System.getProperty("user.home"), ".aiprompts_kmp_data")

    // 2. Убеждаемся, что эта папка существует. Если нет - создаем ее.
    // Это самый важный шаг, который, скорее всего, решит проблему.
    if (!dbFolder.exists()) {
        dbFolder.mkdirs() // mkdirs создаст все необходимые родительские папки
    }

    // 3. Определяем путь к файлу БД внутри нашей папки
    val dbFile = File(dbFolder, AppDatabase.DBNAME)

    // 4. Возвращаем builder с абсолютным путем к файлу
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}
