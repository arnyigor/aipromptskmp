package com.arny.aiprompts.repositories

import com.arny.aiprompts.models.SyncResult

interface IPromptSynchronizer {
    suspend fun synchronize(): SyncResult
    suspend fun getLastSyncTime(): Long?
    suspend fun setLastSyncTime(timestamp: Long)
} 