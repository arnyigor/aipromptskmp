package com.arny.aiprompts

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "AI Prompts Desktop") {
        AppScreen()
    }
}