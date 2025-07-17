package com.arny.aiprompts

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arny.aiprompts.di.initKoin
import com.arny.aiprompts.navigation.DefaultRootComponent
import com.arny.aiprompts.screens.AppScreen
import com.arny.aiprompts.ui.RootContent

fun main() = application {
    initKoin()
    // На Desktop нужно вручную создать LifecycleRegistry
    val lifecycle = LifecycleRegistry()

    // Создаем корневой компонент
    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
    )

    Window(onCloseRequest = ::exitApplication, title = "Prompts App") {
        RootContent(component = root)
    }
}