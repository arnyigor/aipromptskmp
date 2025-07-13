package com.arny.aiprompts.repositories

import com.arny.aiprompts.models.Message
import kotlinx.coroutines.flow.Flow

interface IChatHistoryRepository {
    /**
     * Предоставляет реактивный поток с полной историей сообщений.
     */
    fun getHistoryFlow(): Flow<List<Message>>

    /**
     * Добавляет одно или несколько сообщений в историю.
     */
    suspend fun addMessages(messages: List<Message>)

    /**
     * Очищает историю чата.
     */
    suspend fun clearHistory()
}
