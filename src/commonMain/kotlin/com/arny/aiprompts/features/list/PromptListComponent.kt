package com.arny.aiprompts.features.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arny.aiprompts.interactors.IPromptsInteractor
import com.arny.aiprompts.models.Prompt
import com.arny.aiprompts.models.SyncStatus
import com.arny.aiprompts.sync.ISyncManager
import com.arny.aiprompts.ui.prompts.PromptsListState
import com.arny.aiprompts.ui.prompts.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface PromptListComponent {
    val state: StateFlow<PromptsListState>

    fun onRefresh()

    // --- Обновленные и новые методы для UI ---
    fun onPromptClicked(id: String)
    fun onFavoriteClicked(id: String)
    fun onSearchQueryChanged(query: String)
    fun onCategoryChanged(category: String)
    fun onSortOrderChanged(sortOrder: SortOrder)
    fun onFavoritesToggleChanged(isFavoritesOnly: Boolean)
    fun onSortDirectionToggle()

    // Методы для правой панели действий
    fun onAddPromptClicked()
    fun onEditPromptClicked()
    fun onDeletePromptClicked()

    fun onMoreMenuToggle(isVisible: Boolean)
    fun onSettingsClicked()
    fun onTokenDialogDismiss()
    fun onTokenSave(token: String)
}

class DefaultPromptListComponent(
    componentContext: ComponentContext,
    private val promptsInteractor: IPromptsInteractor,
    private val syncManager: ISyncManager,
    private val onNavigateToDetails: (promptId: String) -> Unit,
) : PromptListComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(PromptsListState(isLoading = true))
    override val state: StateFlow<PromptsListState> = _state.asStateFlow()

    private val scope = coroutineScope()

    init {
        scope.launch {
            loadPrompts()
        }
        scope.launch {
            syncManager.syncState.collectLatest { syncStatus ->
                _state.update { it.copy(syncStatus = syncStatus) }
                if (syncStatus is SyncStatus.Success) {
                    loadPrompts() // Reload prompts after successful sync
                } else if (syncStatus is SyncStatus.Error) {
                    _state.update { it.copy(error = syncStatus.message) }
                }
            }
        }
        scope.launch {
            syncManager.sync() // Initial sync attempt
        }
    }

    override fun onRefresh() {
        scope.launch {
            syncManager.sync()
        }
    }

    override fun onPromptClicked(id: String) {
        // Обновляем ID выбранного элемента для правой панели
        _state.update { it.copy(selectedPromptId = id) }
        // Можно и сразу навигацию делать, если на desktop не нужна правая панель
        onNavigateToDetails(id) // Просто вызываем коллбэк, переданный из RootComponent
    }

    override fun onFavoriteClicked(id: String) {
        scope.launch {
            promptsInteractor.toggleFavorite(id)
            val updatedPrompts = _state.value.allPrompts.map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
            _state.update { it.copy(allPrompts = updatedPrompts) }
            applyFiltersAndSorting()
        }
    }

    override fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFiltersAndSorting()
    }

    override fun onCategoryChanged(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        applyFiltersAndSorting()
    }

    override fun onSortDirectionToggle() {
        _state.update { it.copy(isSortAscending = !it.isSortAscending) }
        applyFiltersAndSorting() // <-- Важно переприменить сортировку
    }

    override fun onSortOrderChanged(sortOrder: SortOrder) {
        _state.update { it.copy(selectedSortOrder = sortOrder) }
        applyFiltersAndSorting()
    }

    override fun onFavoritesToggleChanged(isFavoritesOnly: Boolean) {
        _state.update { it.copy(isFavoritesOnly = isFavoritesOnly) }
        applyFiltersAndSorting()
    }

    override fun onAddPromptClicked() { /* TODO: Навигация на экран создания */
    }

    override fun onEditPromptClicked() {
        state.value.selectedPromptId?.let {

        }
    }

    override fun onDeletePromptClicked() {
        state.value.selectedPromptId?.let { /* TODO: Показать диалог и удалить по ID */ }
    }

    override fun onMoreMenuToggle(isVisible: Boolean) {
        _state.update { it.copy(isMoreMenuVisible = isVisible) }
    }

    override fun onSettingsClicked() {
        val currentToken = promptsInteractor.getGitHubToken().orEmpty()
        _state.update {
            it.copy(
                isTokenDialogVisible = true,
                currentGitHubToken = currentToken // и сохраняем его в state
            )
        }
    }

    override fun onTokenDialogDismiss() {
        _state.update { it.copy(isTokenDialogVisible = false, currentGitHubToken = "") }
    }

    override fun onTokenSave(token: String) {
        scope.launch {
            promptsInteractor.saveGitHubToken(token)
            _state.update { it.copy(isTokenDialogVisible = false, currentGitHubToken = "") }
            syncManager.sync()
        }
    }

    private fun loadPrompts() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val allPrompts = promptsInteractor.getPrompts(query = "", limit = Int.MAX_VALUE, offset = 0)
                _state.update { it.copy(isLoading = false, allPrompts = allPrompts) }
                applyFiltersAndSorting()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Не удалось загрузить промпты: ${e.message}") }
            }
        }
    }

    private fun applyFiltersAndSorting() {
        val currentState = _state.value
        val filteredList = currentState.allPrompts.filter { prompt ->
            val favoriteMatch = if (currentState.isFavoritesOnly) prompt.isFavorite else true
            val categoryMatch =
                currentState.selectedCategory == "Все категории" || prompt.category == currentState.selectedCategory
            val queryMatch = if (currentState.searchQuery.isBlank()) {
                true
            } else {
                val query = currentState.searchQuery.trim().lowercase()
                prompt.title.lowercase().contains(query) || prompt.description?.lowercase()?.contains(query) == true
            }
            favoriteMatch && categoryMatch && queryMatch
        }

        val sortedList = when (currentState.selectedSortOrder) {
            SortOrder.BY_FAVORITE_DESC -> filteredList.sortedWith(compareByDescending<Prompt> { it.isFavorite }.thenByDescending { it.modifiedAt })
            SortOrder.BY_NAME_ASC -> filteredList.sortedBy { it.title }
            SortOrder.BY_DATE_DESC -> filteredList.sortedByDescending { it.modifiedAt }
            SortOrder.BY_CATEGORY -> filteredList.sortedBy { it.category }
        }
        _state.update {
            it.copy(
                currentPrompts = if (it.isSortAscending) sortedList.reversed() else sortedList
            )
        }
    }
}
