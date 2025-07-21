package com.arny.aiprompts.di

import IOpenRouterRepository
import com.arny.aiprompts.interactors.ILLMInteractor
import com.arny.aiprompts.interactors.IPromptsInteractor
import com.arny.aiprompts.interactors.LLMInteractor
import com.arny.aiprompts.interactors.PromptsInteractorImpl
import com.arny.aiprompts.repositories.*
import com.arny.aiprompts.sync.PromptSynchronizerImpl
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val repositoryModule = module { // Рекомендую вынести в отдельный модуль
    single<IPromptsRepository> {
        PromptsRepositoryImpl(
            promptDao = get(),
            // Указываем, что сюда нужно внедрить диспатчер с квалификатором IoDispatcher
            dispatcher = get(qualifier = IoDispatcher)
        )
    }
}

val appModule = module {
    // 1. Регистрируем базовые зависимости
    single { createHttpClient() }
    single { createStringProvider() }

    // 2. Регистрируем репозитории явно. Это исправляет ошибку.
    single<IOpenRouterRepository> { OpenRouterRepositoryImpl(get()) }
    single<IChatHistoryRepository> { ChatHistoryRepositoryImpl() }
    single<ISettingsRepository> { SettingsRepositoryImpl() }
    // Репозитории
    single<IPromptSynchronizer> { PromptSynchronizerImpl(get(), get(), get(), get()) }

    // Интеракторы
    single<IPromptsInteractor> { PromptsInteractorImpl(get(), get()) }

    // 3. Регистрируем интерактор. Здесь все было правильно.
    single<ILLMInteractor> {
        LLMInteractor(
            stringProvider = get(),
            modelsRepository = get(),
            settingsRepository = get(),
            historyRepository = get()
        )
    }
}

fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(
            coroutinesModule,
            repositoryModule,
            appModule,
            networkModule,
            daoModule,
            databaseModule(),
            commonModule,
            platformModule
        )
    }
}
