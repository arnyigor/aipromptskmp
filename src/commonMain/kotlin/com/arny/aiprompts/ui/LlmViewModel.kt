package com.arny.aiprompts.ui

import androidx.lifecycle.ViewModel
import com.arny.aiprompts.interactors.ILLMInteractor
import com.arny.aiprompts.models.LlmModel
import com.arny.aiprompts.results.DataResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Единый класс состояния для всего экрана
data class LlmUiState(
    val modelsResult: DataResult<List<LlmModel>> = DataResult.Loading,
    val prompt: String = "Why is the sky blue? Write a short answer.",
    val responseText: String = "Response will appear here...",
    val isGenerating: Boolean = false
) {
    // Вспомогательное свойство для удобного доступа к выбранной модели
    val selectedModel: LlmModel?
        get() = when (modelsResult) {
            is DataResult.Success -> modelsResult.data.firstOrNull { it.isSelected }
            else -> null
        }
}

class LlmViewModel(
    private val llmInteractor: ILLMInteractor,
    private val viewModelScope: CoroutineScope
) : ViewModel() {
    private val _uiState = MutableStateFlow(LlmUiState())
    val uiState: StateFlow<LlmUiState> = _uiState.asStateFlow()

    init {
        // Подписываемся на поток моделей из интерактора ОДИН РАЗ
        llmInteractor.getModels()
            .onEach { modelsResult ->
                _uiState.update { it.copy(modelsResult = modelsResult) }
            }
            .launchIn(viewModelScope)

        // Запускаем первоначальное обновление
        refreshModels()
    }

    // --- ОБРАБОТЧИКИ СОБЫТИЙ ОТ UI ---

    fun onPromptChanged(newPrompt: String) {
        _uiState.update { it.copy(prompt = newPrompt) }
    }

    fun onModelSelected(modelId: String) {
        viewModelScope.launch {
            llmInteractor.toggleModelSelection(modelId)
            // Интерактор обновит источник данных, а наша подписка в init{} получит новый список
        }
    }

    fun onGenerateClicked() {
        val currentState = _uiState.value
        val selectedModel = currentState.selectedModel ?: return
        if (currentState.isGenerating) return

        viewModelScope.launch {
            llmInteractor.sendMessage(selectedModel.id, currentState.prompt)
                .onStart {
                    _uiState.update { it.copy(isGenerating = true, responseText = "") }
                }
                .onCompletion {
                    _uiState.update { it.copy(isGenerating = false) }
                }
                .collect { result ->
                    when (result) {
                        is DataResult.Success -> _uiState.update { it.copy(responseText = result.data) }
                        is DataResult.Error -> _uiState.update { it.copy(responseText = "Error: ${result.exception?.message}") }
                        is DataResult.Loading -> _uiState.update { it.copy(responseText = "Generating...") }
                    }
                }
        }
    }

    fun refreshModels() {
        viewModelScope.launch {
            llmInteractor.refreshModels()
        }
    }

    // Этот метод нужно будет вызывать при уничтожении ViewModel
    override fun onCleared() {
        viewModelScope.cancel()
    }
}
