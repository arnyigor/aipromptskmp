package com.arny.aiprompts.features.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arny.aiprompts.interactors.IPromptsInteractor
import com.arny.aiprompts.models.Prompt
import com.arny.aiprompts.models.SyncResult
import com.arny.aiprompts.ui.prompts.PromptsListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface PromptListComponent {
    val state: StateFlow<PromptsListState>

    fun onPromptClicked(id: String)
    fun onFavoriteClicked(id: String)
    fun onFilterChanged(filter: PromptsListState.Filter)
    fun onRefresh()
}

class DefaultPromptListComponent(
    componentContext: ComponentContext,
    private val promptsInteractor: IPromptsInteractor,
    private val onNavigateToDetails: (promptId: String) -> Unit,
) : PromptListComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(PromptsListState(isLoading = true))
    override val state: StateFlow<PromptsListState> = _state.asStateFlow()

    private val scope = coroutineScope()

    init {
        // Запускаем синхронизацию и загрузку при создании компонента
        onRefresh()
    }

    // Загрузка данных из БД
    private fun loadPrompts() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Вызываем интерактор для получения данных
                val allPrompts = promptsInteractor.getPrompts(
                    query = "", // Начальный пустой запрос
                    limit = Int.MAX_VALUE, // Загружаем все для кеширования
                    offset = 0
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        allPrompts = allPrompts,
                        currentPrompts = filterPrompts(allPrompts, it.selectedFilter)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Не удалось загрузить промпты: ${e.message}") }
            }
        }
    }

    /**
     * Синхронизирует данные с сервером.
     * @param isInitialSync true, если это первая синхронизация, чтобы не показывать ошибку, если нет сети при запуске.
     */
    private fun synchronize(isInitialSync: Boolean = false) {
        scope.launch {
            // Показываем индикатор синхронизации (например, для SwipeRefresh)
            _state.update { it.copy(isSyncing = true, error = null) }

            // Используем when для обработки всех вариантов SyncResult
            when (val result = promptsInteractor.synchronize()) {
                is SyncResult.Success -> {
                    // Данные УЖЕ сохранены в БД синхронизатором.
                    // Теперь нам нужно просто перезагрузить их в UI.
                    // Вызываем loadPrompts, чтобы обновить allPrompts и currentPrompts.
                    loadPrompts()
                }

                is SyncResult.Error -> {
                    // Показываем ошибку, только если это не первая "тихая" синхронизация
                    if (!isInitialSync) {
                        _state.update { it.copy(error = result.message) }
                    }
                }

                is SyncResult.Conflicts -> {
                    if (!isInitialSync) {
                        _state.update { it.copy(error = "Обнаружены конфликты синхронизации") }
                    }
                }
            }

            // Убираем индикатор синхронизации ПОСЛЕ всех операций
            _state.update { it.copy(isSyncing = false) }
        }
    }

    override fun onFavoriteClicked(id: String) {
        scope.launch {
            // Вызываем интерактор для изменения статуса
            promptsInteractor.toggleFavorite(id)

            // Оптимистичное обновление UI: не ждем ответа от БД, а сразу меняем состояние
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
        // Фильтрация теперь происходит только на клиенте, это очень быстро
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
        // При "pull-to-refresh" мы запускаем синхронизацию
        synchronize()
    }

}

