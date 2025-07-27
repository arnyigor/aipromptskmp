package com.arny.aiprompts

// /desktopApp/src/main/kotlin/com/arny/aiprompts/Main.kt

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arny.aiprompts.di.initKoin
import com.arny.aiprompts.interactors.IPromptsInteractor
import com.arny.aiprompts.navigation.DefaultRootComponent
import com.arny.aiprompts.sync.ISyncManager
import com.arny.aiprompts.ui.RootContent
import org.koin.java.KoinJavaComponent.get

fun main() = application {
    // 1. Инициализируем Koin
    initKoin { /* ... */ }

    // 2. Создаем Lifecycle
    val lifecycle = LifecycleRegistry()

    val promptsInteractor: IPromptsInteractor = get(IPromptsInteractor::class.java)
    val syncManager: ISyncManager = get(ISyncManager::class.java)

    // 3. Создаем rootComponent, ПЕРЕДАВАЯ зависимость
    val rootComponent = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
        promptsInteractor = promptsInteractor // <-- Передаем здесь
    )

    // Запускаем проверку при старте
    syncManager.sync()

    // 4. Создаем окно
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Prompts KMP"
    ) {
        MaterialTheme {
            RootContent(component = rootComponent)
        }
    }
}
