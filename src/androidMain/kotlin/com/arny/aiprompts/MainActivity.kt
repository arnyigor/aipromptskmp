package com.arny.aiprompts

// MainActivity.kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import com.arny.aiprompts.interactors.IPromptsInteractor
import com.arny.aiprompts.navigation.DefaultRootComponent
import com.arny.aiprompts.ui.RootContent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    // Внедряем зависимость с помощью делегата.
    // Koin сам найдет нужную реализацию.
    private val promptsInteractor: IPromptsInteractor by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем корневой компонент, передавая ему defaultComponentContext
        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            promptsInteractor = promptsInteractor
        )

        setContent {
            // Передаем компонент в наш корневой UI
            RootContent(component = root)
        }
    }
}
