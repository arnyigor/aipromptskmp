package com.arny.aiprompts.repositories

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.datetime.Clock

interface ISyncSettingsRepository {
    fun getLastCheckTimestamp(): Long
    fun saveLastCheckTimestamp(timestamp: Long = Clock.System.now().toEpochMilliseconds())
    fun getLastCommitHash(): String?
    fun saveLastCommitHash(hash: String)
}

class SyncSettingsRepositoryImpl(
    private val settings: Settings
) : ISyncSettingsRepository {
    private companion object {
        const val KEY_LAST_CHECK_TIMESTAMP = "sync_last_check_timestamp"
        const val KEY_LAST_COMMIT_HASH = "sync_last_commit_hash"
    }

    override fun getLastCheckTimestamp(): Long = settings[KEY_LAST_CHECK_TIMESTAMP, 0L]
    override fun saveLastCheckTimestamp(timestamp: Long) {
        settings[KEY_LAST_CHECK_TIMESTAMP] = timestamp
    }

    override fun getLastCommitHash(): String? = settings[KEY_LAST_COMMIT_HASH]
    override fun saveLastCommitHash(hash: String) {
        settings[KEY_LAST_COMMIT_HASH] = hash
    }
}
