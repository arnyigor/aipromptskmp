package com.arny.aiprompts.api

import com.arny.aiprompts.models.ChatCompletionRequestDTO
import com.arny.aiprompts.models.ChatCompletionResponseDTO
import com.arny.aiprompts.models.ModelsResponseDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

// KMP-совместимый сервис, использующий Ktor
class OpenRouterService(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://openrouter.ai/api/v1/"
    }

    /**
     * Выполняет запрос на завершение чата.
     * @return Распарсенный DTO ответа.
     * @throws Exception в случае ошибки сети или неуспешного статуса ответа.
     */
    suspend fun getChatCompletion(
        authorization: String,
        request: ChatCompletionRequestDTO,
        referer: String? = null,
        title: String? = null
    ): ChatCompletionResponseDTO {
        // Ktor автоматически обработает Content-Type: application/json
        // благодаря плагину ContentNegotiation
        return httpClient.post(BASE_URL + "chat/completions") {
            // Устанавливаем заголовки
            header(HttpHeaders.Authorization, authorization)
            referer?.let { header("HTTP-Referer", it) }
            title?.let { header("X-Title", it) }

            // Устанавливаем тело запроса
            setBody(request)
        }.body() // .body() автоматически распарсит JSON в DTO
    }

    /**
     * Получает список доступных моделей.
     * @return Распарсенный DTO ответа.
     * @throws Exception в случае ошибки сети или неуспешного статуса ответа.
     */
    suspend fun getModels(): ModelsResponseDTO {
        return httpClient.get(BASE_URL + "models").body()
    }
}
