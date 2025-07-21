package com.arny.aiprompts

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arny.aiprompts.di.initKoin
import com.arny.aiprompts.navigation.DefaultRootComponent
import com.arny.aiprompts.sync.ISyncManager
import com.arny.aiprompts.ui.RootContent

fun main() = application {
    // 1. Инициализируем Koin
    initKoin {
        // ...
    }

    // 2. Создаем Lifecycle
    val lifecycle = LifecycleRegistry()

    val rootComponent = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
    )

    // Запускаем проверку при старте
    val syncManager: ISyncManager = org.koin.java.KoinJavaComponent.get(ISyncManager::class.java)
    syncManager.syncIfNeeded()

    // 4. Создаем окно
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Prompts KMP"
    ) {
        // 5. РАСКОММЕНТИРУЙТЕ: Показываем полноценный UI
        MaterialTheme {
            RootContent(component = rootComponent)
        }
    }
}