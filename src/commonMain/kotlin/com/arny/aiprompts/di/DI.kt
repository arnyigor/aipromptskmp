package com.arny.aiprompts.di

import IOpenRouterRepository
import com.arny.aiprompts.utils.StringProvider
import com.arny.aiprompts.interactors.ILLMInteractor
import com.arny.aiprompts.interactors.LLMInteractor
import com.arny.aiprompts.repositories.*

// Наш простой DI-контейнер в виде объекта
object DI {
    // Создаем HttpClient с помощью нашей фабрики
    private val httpClient = createHttpClient()
    private val stringProvider = createStringProvider()

    // --- РЕПОЗИТОРИИ ---
    // Используем by lazy, чтобы они создавались только при первом обращении
    private val openRouterRepository: IOpenRouterRepository by lazy {
        OpenRouterRepository(httpClient)
    }
    private val chatHistoryRepository: IChatHistoryRepository by lazy {
        ChatHistoryRepositoryImpl()
    }
    private val settingsRepository: ISettingsRepository by lazy {
        SettingsRepositoryImpl()
    }

    // --- ИНТЕРАКТОР ---
    // Публичное свойство, которое будет доступно из UI
    val llmInteractor: ILLMInteractor by lazy {
        LLMInteractor(
            stringProvider = stringProvider,
            modelsRepository = openRouterRepository,
            historyRepository = chatHistoryRepository,
            settingsRepository = settingsRepository
        )
    }
}
