package com.arny.aiprompts

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arny.aiprompts.di.initKoin

fun main() = application {
    initKoin()
    Window(onCloseRequest = ::exitApplication, title = "AI Prompts Desktop") {
        AppScreen()
    }
}