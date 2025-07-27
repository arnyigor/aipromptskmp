package com.arny.aiprompts.interactors
import com.arny.aiprompts.models.Prompt
import com.arny.aiprompts.models.SyncResult

interface IPromptsInteractor {
    suspend fun getPrompts(
        query: String = "",
        category: String? = null,
        status: String? = null,
        tags: List<String> = emptyList(),
        offset: Int = 0,
        limit: Int = DEFAULT_PAGE_SIZE
    ): List<Prompt>

    suspend fun getPromptById(id: String): Prompt?
    suspend fun savePrompt(prompt: Prompt): Long
    suspend fun updatePrompt(prompt: Prompt)
    suspend fun deletePrompt(promptId: String)
    suspend fun synchronize(): SyncResult
    suspend fun getLastSyncTime(): Long?
    suspend fun toggleFavorite(promptId: String)
    suspend fun saveGitHubToken(string: String)
      fun getGitHubToken(): String?

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}