package com.arny.aiprompts.ui.prompts

import com.arny.aiprompts.models.Prompt

// Добавим state-класс для полноты картины
data class PromptsListState(
    val isLoading: Boolean = false,
    val allPrompts: List<Prompt> = emptyList(),
    val currentPrompts: List<Prompt> = emptyList(),
    val selectedFilter: Filter = Filter.ALL,
    val error: String? = null
) {
    enum class Filter { ALL, FAVORITES }
}