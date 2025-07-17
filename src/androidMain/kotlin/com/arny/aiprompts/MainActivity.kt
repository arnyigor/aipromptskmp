package com.arny.aiprompts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import com.arny.aiprompts.navigation.DefaultRootComponent
import com.arny.aiprompts.ui.RootContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Создаем корневой компонент, передавая ему defaultComponentContext
        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
        )

        setContent {
            // Передаем компонент в наш корневой UI
            RootContent(component = root)
        }
    }
}
