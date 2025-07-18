package com.arny.aiprompts

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arny.aiprompts.di.initKoin
import com.arny.aiprompts.navigation.DefaultRootComponent
import com.arny.aiprompts.ui.RootContent

fun main() = application {
    // 1. Инициализируем Koin
    initKoin {
        // ...
    }

    // 2. Создаем Lifecycle
    val lifecycle = LifecycleRegistry()

    // 3. РАСКОММЕНТИРУЙТЕ: Создаем полноценный корневой компонент
    val rootComponent = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
    )

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