package com.arny.aiprompts.di

import IOpenRouterRepository
import com.arny.aiprompts.interactors.ILLMInteractor
import com.arny.aiprompts.interactors.LLMInteractor
import com.arny.aiprompts.repositories.ChatHistoryRepositoryImpl
import com.arny.aiprompts.repositories.IChatHistoryRepository
import com.arny.aiprompts.repositories.ISettingsRepository
import com.arny.aiprompts.repositories.OpenRouterRepositoryImpl
import com.arny.aiprompts.repositories.SettingsRepositoryImpl
import com.arny.aiprompts.ui.LlmViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import org.koin.core.context.GlobalContext.startKoin
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration

val appModule = module {
    // 1. Регистрируем базовые зависимости
    single { createHttpClient() }
    single { createStringProvider() }

    // 2. Регистрируем репозитории явно. Это исправляет ошибку.
    single<IOpenRouterRepository> { OpenRouterRepositoryImpl(get()) }
    single<IChatHistoryRepository> { ChatHistoryRepositoryImpl() }
    single<ISettingsRepository> { SettingsRepositoryImpl() }

    // 3. Регистрируем интерактор. Здесь все было правильно.
    single<ILLMInteractor> {
        LLMInteractor(
            stringProvider = get(),
            modelsRepository = get(),
            settingsRepository = get(),
            historyRepository = get()
        )
    }

    // 4. Регистрируем ViewModel. viewModelOf здесь должен работать без проблем.
    factory {
        LlmViewModel(
            llmInteractor = get(),
            // Создаем и передаем scope, который будет жить вместе с этим экземпляром ViewModel
            viewModelScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        )
    }
}

fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(appModule)
    }
}
