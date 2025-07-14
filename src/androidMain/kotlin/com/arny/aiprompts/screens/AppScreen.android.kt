package com.arny.aiprompts.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


/**
 * "Актуальная" реализация экрана для Android.
 * Это совершенно другой UI, без логики Ollama.
 */
@Composable
actual fun AppScreen() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Hello from Android!")
        }
    }
}
