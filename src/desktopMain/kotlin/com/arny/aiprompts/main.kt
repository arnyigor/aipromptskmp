package com.arny.aiprompts

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arny.aiprompts.di.initKoin
import com.arny.aiprompts.screens.AppScreen

fun main() = application {
    initKoin()
    Window(onCloseRequest = ::exitApplication, title = "AI Prompts Desktop") {
        AppScreen()
    }
}