package com.arny.aiprompts.models

sealed class SyncResult {
    data class Success(val updatedPrompts: List<Prompt>) : SyncResult()
    data class Error(val message: String) : SyncResult()
    data class Conflicts(val conflicts: List<SyncConflict>) : SyncResult()
}