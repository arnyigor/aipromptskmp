package com.arny.aiprompts.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arny.aiprompts.db.daos.PromptDao
import com.arny.aiprompts.db.entities.PromptEntity

@Database(
    entities = [
        PromptEntity::class,
    ],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao

    companion object {
        const val DBNAME = "AiPromptMasterDB"
    }
}