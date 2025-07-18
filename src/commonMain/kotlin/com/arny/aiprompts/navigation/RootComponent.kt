package com.arny.aiprompts.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arny.aiprompts.features.details.DefaultPromptDetailComponent
import com.arny.aiprompts.features.details.PromptDetailComponent
import com.arny.aiprompts.features.list.DefaultPromptListComponent
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arny.aiprompts.features.list.PromptListComponent
import com.arny.aiprompts.interactors.IPromptsInteractor
import org.koin.java.KoinJavaComponent.get

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    // Sealed-класс для дочерних компонентов, чтобы UI знал, какой экран рисовать
    sealed interface Child {
        data class List(val component: PromptListComponent) : Child
        data class Details(val component: PromptDetailComponent) : Child
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    // Внедряем интерактор с помощью Koin
    private val promptsInteractor: IPromptsInteractor = get(IPromptsInteractor::class.java)

    private val navigation = StackNavigation<ScreenConfig>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = ScreenConfig.serializer(), // Для сохранения стека
            initialConfiguration = ScreenConfig.PromptList, // Стартовый экран
            handleBackButton = true, // Автоматическая обработка кнопки "назад" на Android
            childFactory = ::createChild // Фабрика для создания дочерних компонентов
        )

    private fun createChild(
        config: ScreenConfig,
        context: ComponentContext
    ): RootComponent.Child {
        return when (config) {
            is ScreenConfig.PromptList -> RootComponent.Child.List(
                DefaultPromptListComponent(
                    componentContext = context,
                    promptsInteractor = promptsInteractor,
                    onNavigateToDetails = { promptId ->
                        // Переход на экран деталей
                        navigation.push(ScreenConfig.PromptDetails(promptId))
                    }
                )
            )
            is ScreenConfig.PromptDetails -> RootComponent.Child.Details(
                DefaultPromptDetailComponent(
                    componentContext = context,
                    promptId = config.promptId, // Передаем ID из конфига
                    onNavigateBack = {
                        // Возврат на предыдущий экран
                        navigation.pop()
                    }
                )
            )
        }
    }
}
