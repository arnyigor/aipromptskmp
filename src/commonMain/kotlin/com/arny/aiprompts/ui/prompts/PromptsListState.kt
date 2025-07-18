package com.arny.aiprompts.ui.prompts

import com.arny.aiprompts.models.Prompt

data class PromptsListState(
    val isLoading: Boolean = false, // Для первоначальной загрузки
    val isSyncing: Boolean = false, // Для pull-to-refresh / фоновой синхронизации
    val allPrompts: List<Prompt> = emptyList(),
    val currentPrompts: List<Prompt> = emptyList(),
    val selectedFilter: Filter = Filter.ALL,
    val error: String? = null
) {
    enum class Filter { ALL, FAVORITES }
}