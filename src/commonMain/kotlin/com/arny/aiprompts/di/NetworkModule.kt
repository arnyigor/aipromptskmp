package com.arny.aiprompts.di


import co.touchlab.kermit.Logger as AppLogger
import com.arny.aiprompts.api.GitHubService
import com.arny.aiprompts.api.OpenRouterService
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Создаем квалификаторы Koin для различения HttpClient, если это нужно
val GITHUB_HTTP_CLIENT = named("GitHubHttpClient")
val OPEN_ROUTER_HTTP_CLIENT = named("OpenRouterHttpClient")

val networkModule = module {

    // Предоставляем Json-парсер для всего приложения
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    // --- Создаем HttpClient для GitHub ---
    // Можно использовать один HttpClient для всех, если настройки одинаковые,
    // но для примера разделим их, как у вас было с Retrofit.
    single<HttpClient>(qualifier = GITHUB_HTTP_CLIENT) {
        HttpClient { // HttpClient(engine) - движок будет подставлен на каждой платформе
            install(Logging) {
                level = LogLevel.HEADERS
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        AppLogger.withTag("KtorHttpClient").i(message)
                    }
                }
            }
            // Для GitHub API не нужен ContentNegotiation, так как мы качаем сырые байты
        }
    }

    // --- Создаем HttpClient для OpenRouter ---
    single<HttpClient>(qualifier = OPEN_ROUTER_HTTP_CLIENT) {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // Очень важный параметр, спасает от падений, если GitHub добавит новые поля
                })
            }
            install(Logging) {
                level = LogLevel.HEADERS
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        AppLogger.withTag("KtorHttpClient").i(message)
                    }
                }
            }
            // Здесь можно добавить Bearer Token для авторизации
            // install(Auth) { bearer { ... } }
        }
    }

    // --- Предоставляем наши сервисы ---
    single<GitHubService> {
        // Явно указываем, какой HttpClient использовать
        GitHubService(httpClient = get(qualifier = GITHUB_HTTP_CLIENT))
    }

    single<OpenRouterService> {
        OpenRouterService(httpClient = get(qualifier = OPEN_ROUTER_HTTP_CLIENT))
    }


    // Этот HttpClient уже настроен с ContentNegotiation, что идеально подходит для OpenRouter
    single<HttpClient>(qualifier = OPEN_ROUTER_HTTP_CLIENT) {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // Очень важный параметр, спасает от падений, если GitHub добавит новые поля
                })
            }
            install(Logging) {
                level = LogLevel.HEADERS
                // ...
            }
        }
    }

    // Предоставляем OpenRouterService
    single<OpenRouterService> {
        OpenRouterService(httpClient = get(qualifier = OPEN_ROUTER_HTTP_CLIENT))
    }
}
