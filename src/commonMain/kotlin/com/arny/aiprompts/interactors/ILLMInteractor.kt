package com.arny.aiprompts.interactors

import com.arny.aiprompts.models.LlmModel
import com.arny.aiprompts.models.Message
import com.arny.aiprompts.results.DataResult
import kotlinx.coroutines.flow.Flow

interface ILLMInteractor {
    fun sendMessage(model: String, userMessage: String): Flow<DataResult<String>>
    fun getModels(): Flow<DataResult<List<LlmModel>>>
    fun getSelectedModel(): Flow<DataResult<LlmModel>>
    suspend fun selectModel(id: String)
    suspend fun refreshModels(): Result<Unit>
    suspend fun toggleModelSelection(clickedModelId: String)
    fun getChatHistoryFlow(): Flow<List<Message>>
    suspend fun clearChat()
}
