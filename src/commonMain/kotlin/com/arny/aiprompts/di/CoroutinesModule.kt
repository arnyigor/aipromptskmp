package com.arny.aiprompts.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Определим квалификаторы, чтобы можно было предоставлять разные диспатчеры
val IoDispatcher = named("IoDispatcher")
val MainDispatcher = named("MainDispatcher")

val coroutinesModule = module {
    // Предоставляем Dispatchers.IO под квалификатором IoDispatcher
    single<CoroutineDispatcher>(qualifier = IoDispatcher) {
        Dispatchers.IO
    }

    // Можно также предоставить и Main, если он где-то понадобится
    single<CoroutineDispatcher>(qualifier = MainDispatcher) {
        Dispatchers.Main
    }
}
