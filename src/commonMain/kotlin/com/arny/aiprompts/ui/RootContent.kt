package com.arny.aiprompts.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arny.aiprompts.navigation.RootComponent
import com.arny.aiprompts.ui.detail.PromptDetailScreen
import com.arny.aiprompts.ui.prompts.PromptsScreen

@Composable
fun RootContent(component: RootComponent) {
    MaterialTheme {
        Children(
            stack = component.stack,
            animation = stackAnimation(slide()) // Добавляем анимацию перехода
        ) { child ->
            // В зависимости от активного дочернего компонента, рисуем нужный экран
            when (val instance = child.instance) {
                is RootComponent.Child.List -> PromptsScreen(component = instance.component)
                is RootComponent.Child.Details -> PromptDetailScreen(component = instance.component)
            }
        }
    }
}
