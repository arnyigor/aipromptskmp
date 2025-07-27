package com.arny.aiprompts.models

sealed class SyncStatus {
    data object None : SyncStatus()
    data object InProgress : SyncStatus()
    data class Error(val message: String? = null) : SyncStatus()
    data class Success(val updatedCount: Int) : SyncStatus()
    data class Conflicts(val conflicts: List<SyncConflict>) : SyncStatus()
}