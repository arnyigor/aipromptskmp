package com.arny.aiprompts.screens

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
    initKoin()

    // 2. Вручную создаем Lifecycle для Decompose
    val lifecycle = LifecycleRegistry()

    // 3. Создаем корневой компонент, передавая ему DefaultComponentContext
    val rootComponent = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
    )

    // 4. Создаем окно приложения
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Prompts KMP"
    ) {
        // 5. Внутри окна размещаем наш общий RootContent
        MaterialTheme { // Оборачиваем в тему
            RootContent(component = rootComponent)
        }
    }
}