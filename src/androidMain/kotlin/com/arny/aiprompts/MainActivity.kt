package com.arny.aiprompts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Здесь мы вызываем общую Composable-функцию, определенную в commonMain
        setContent {
            com.arny.aiprompts.screens.AppScreen()
        }
    }
}
