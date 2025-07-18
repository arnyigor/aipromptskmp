package com.arny.aiprompts.features.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arny.aiprompts.interactors.IPromptsInteractor
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
    private val promptsInteractor: IPromptsInteractor,
    private val onNavigateToDetails: (promptId: String) -> Unit,
) : PromptListComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(PromptsListState(isLoading = true))
    override val state: StateFlow<PromptsListState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
        }
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

    // Синхронизация с сервером
    private fun synchronize() {
        scope.launch {
            _state.update { it.copy(isSyncing = true) } // Добавим поле isSyncing в State
            val result = promptsInteractor.synchronize()
            _state.update { it.copy(isSyncing = false) }
            // После синхронизации перезагружаем данные из локальной БД
            if (result.isSuccess) {
                loadPrompts()
            } else {
                _state.update { it.copy(error = result.errorMessage) }
            }
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

