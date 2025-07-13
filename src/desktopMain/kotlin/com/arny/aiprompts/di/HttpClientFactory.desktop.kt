package com.arny.aiprompts.di

import com.arny.aiprompts.utils.BigDecimalSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * "Ожидаем" функцию, которая умеет создавать HttpClient.
 * Теперь компилятор будет знать, что 'HttpClient' - это класс из библиотеки Ktor.
 */

actual fun createHttpClient(): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            // ИСПРАВЛЕНИЕ: Создаем кастомный Json-модуль
            json(
                Json {
                    ignoreUnknownKeys = true // Полезная опция, чтобы игнорировать неизвестные поля из API
                    // Регистрируем наш сериализатор для типа BigDecimal
                    serializersModule = SerializersModule {
                        contextual(BigDecimalSerializer)
                    }
                }
            )
        }

        install(Logging) {
            // Выбираем логгер. Logger.DEFAULT выводит в стандартную консоль.
            logger = Logger.DEFAULT

            // Устанавливаем уровень детализации логов.
            // LogLevel.ALL показывает все: URL, заголовки, тело запроса и ответа.
            level = LogLevel.ALL
        }
    }
}
