package com.arny.aiprompts.repositories

import com.arny.aiprompts.models.Prompt
import kotlinx.coroutines.flow.Flow

interface IPromptsRepository {
    suspend fun getPromptById(promptId: String): Prompt?
    suspend fun insertPrompt(prompt: Prompt): Long
    suspend fun updatePrompt(prompt: Prompt)
    suspend fun deletePrompt(promptId: String)
    suspend fun savePrompts(prompts: List<Prompt>)
    suspend fun getAllPrompts(): Flow<List<Prompt>>
    suspend fun getPrompts(
        search: String = "",
        category: String? = null,
        status: String? = null,
        tags: List<String> = emptyList(),
        offset: Int = 0,
        limit: Int = 20
    ): List<Prompt>

    suspend fun deletePromptsByIds(promptIds: List<String>)
}