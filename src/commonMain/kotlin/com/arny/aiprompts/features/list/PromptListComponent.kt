package com.arny.aiprompts.features.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arny.aiprompts.models.Author
import com.arny.aiprompts.models.Prompt
import com.arny.aiprompts.models.PromptContent
import com.arny.aiprompts.models.PromptMetadata
import com.arny.aiprompts.ui.prompts.PromptsListState
import com.arny.aiprompts.utils.toJavaDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

interface PromptListComponent {
    val state: StateFlow<PromptsListState>

    fun onPromptClicked(id: String)
    fun onFavoriteClicked(id: String)
    fun onFilterChanged(filter: PromptsListState.Filter)
    fun onRefresh()
}

class DefaultPromptListComponent(
    componentContext: ComponentContext,
    private val onNavigateToDetails: (promptId: String) -> Unit,
) : PromptListComponent, ComponentContext by componentContext {

    // Используем state-класс, как я предлагал в первом ответе
    private val _state = MutableStateFlow(PromptsListState(isLoading = true))
    override val state: StateFlow<PromptsListState> = _state.asStateFlow()

    // Создаем CoroutineScope, который будет жить, пока жив компонент
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    init {
        // Убедимся, что scope отменится при уничтожении компонента, чтобы избежать утечек
        lifecycle.doOnDestroy {
            scope.cancel()
        }

        // Теперь можно запускать корутины
        loadPrompts()
    }

    private fun loadPrompts() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val allPrompts = fakePrompts()
                _state.update {
                    it.copy(
                        isLoading = false,
                        allPrompts = allPrompts, // Храним все, чтобы не грузить заново
                        currentPrompts = filterPrompts(allPrompts, it.selectedFilter)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Не удалось загрузить промпты") }
            }
        }
    }

    // Временная функция для заглушки данных
    private fun fakePrompts(): List<Prompt> {
        return listOf(
            Prompt(
                id = "0a2e7773-36db-4867-9e55-dd15d273ce2d",
                title = "Для начинающих диспечеров",
                version = "1.0.0",
                status = "active",
                isLocal = false,
                isFavorite = false,
                description = "",
                content = PromptContent(ru = "Текст промпта на русском", en = "Prompt text in English"),
                compatibleModels = listOf("Model A", "Model B"),
                category = "business",
                tags = listOf("general"),
                variables = emptyMap(),
                metadata = PromptMetadata(author = Author(id = "", name = ""), source = "", notes = ""),
                rating = 0.0f,
                createdAt = LocalDateTime.parse("2025-07-14T18:09:47.755110").toJavaDate(),
                modifiedAt = LocalDateTime.parse("2025-07-14T18:09:47.755110").toJavaDate(),
            ),
            Prompt(
                id = "1b3f99a1-7c5e-4b8d-9a2c-5e9a8b7c3d4f",
                title = "Продвинутый промпт",
                version = "2.0.0",
                status = "active",
                isLocal = true,
                isFavorite = true,
                description = "Описание продвинутого промпта",
                content = PromptContent(ru = "Продвинутый текст на русском", en = "Advanced text in English"),
                compatibleModels = listOf("Model C", "Model D"),
                category = "technology",
                tags = listOf("advanced", "ai"),
                variables = emptyMap(),
                metadata = PromptMetadata(
                    author = Author(id = "", name = "Автор 2"),
                    source = "stackoverflow",
                    notes = "Примечание"
                ),
                rating = 0.0f,
                ratingVotes = 0,
                createdAt = LocalDateTime.parse("2025-07-15T10:00:00.000000").toJavaDate(),
                modifiedAt = LocalDateTime.parse("2025-07-15T10:00:00.000000").toJavaDate(),
            )
        )
    }

    override fun onFavoriteClicked(id: String) {
        scope.launch {
            // TODO: Логика обновления в репозитории
            val updatedPrompts = _state.value.allPrompts.map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
            _state.update {
                it.copy(
                    allPrompts = updatedPrompts,
                    currentPrompts = filterPrompts(updatedPrompts, it.selectedFilter)
                )
            }
        }
    }

    override fun onFilterChanged(filter: PromptsListState.Filter) {
        _state.update {
            it.copy(
                selectedFilter = filter,
                currentPrompts = filterPrompts(it.allPrompts, filter)
            )
        }
    }

    private fun filterPrompts(prompts: List<Prompt>, filter: PromptsListState.Filter): List<Prompt> {
        return when (filter) {
            PromptsListState.Filter.ALL -> prompts
            PromptsListState.Filter.FAVORITES -> prompts.filter { it.isFavorite }
        }
    }

    override fun onPromptClicked(id: String) {
        onNavigateToDetails(id)
    }

    override fun onRefresh() {
        loadPrompts()
    }
}

