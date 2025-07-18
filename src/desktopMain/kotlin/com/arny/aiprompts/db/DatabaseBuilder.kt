package com.arny.aiprompts.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home"), AppDatabase.DBNAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}