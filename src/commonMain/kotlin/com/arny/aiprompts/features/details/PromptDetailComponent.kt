package com.arny.aiprompts.features.details

import com.arkivanov.decompose.ComponentContext
import com.arny.aiprompts.ui.detail.PromptDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PromptDetailComponent {
    val state: StateFlow<PromptDetailState>
    fun onFavoriteClicked()
    fun onBackClicked()
    fun onRefresh()
}

class DefaultPromptDetailComponent(
    componentContext: ComponentContext,
    private val promptId: String, // Получаем ID из навигации
    private val onNavigateBack: () -> Unit, // Коллбэк для возврата назад
) : PromptDetailComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(PromptDetailState(isLoading = true))
    override val state: StateFlow<PromptDetailState> = _state.asStateFlow()

    override fun onFavoriteClicked() {
        TODO("Not yet implemented")
    }

    init {
        // Загружаем данные для конкретного promptId
        loadPromptDetails()
    }

    private fun loadPromptDetails() {
        // ... логика загрузки данных промпта по promptId ...
    }

    // ... остальная логика (onFavoriteClicked, onRefresh) ...

    override fun onBackClicked() {
        onNavigateBack()
    }

    override fun onRefresh() {
        TODO("Not yet implemented")
    }
}
