package com.arny.aiprompts.ui.detail

import com.arny.aiprompts.models.Prompt

data class PromptDetailState(
    val prompt: Prompt? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    // Хранит копию промпта, которую мы меняем в режиме редактирования
    val draftPrompt: Prompt? = null
)

enum class PromptLanguage {
    RU, EN
}