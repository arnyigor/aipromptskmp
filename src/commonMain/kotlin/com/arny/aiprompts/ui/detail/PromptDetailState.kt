package com.arny.aiprompts.ui.detail

import com.arny.aiprompts.models.Prompt

data class PromptDetailState(
    val prompt: Prompt? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)