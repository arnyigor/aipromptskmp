package com.arny.aiprompts.repositories

import com.arny.aiprompts.models.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatHistoryRepositoryImpl constructor() : IChatHistoryRepository {

    private val _history = MutableStateFlow<List<Message>>(emptyList())

    override fun getHistoryFlow(): Flow<List<Message>> = _history.asStateFlow()

    override suspend fun addMessages(messages: List<Message>) {
        _history.update { currentHistory ->
            currentHistory + messages
        }
    }

    override suspend fun clearHistory() {
        _history.value = emptyList()
    }
}
