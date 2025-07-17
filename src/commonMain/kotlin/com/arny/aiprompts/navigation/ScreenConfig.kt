package com.arny.aiprompts.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface ScreenConfig {
    @Serializable
    data object PromptList : ScreenConfig

    @Serializable
    data class PromptDetails(val promptId: String) : ScreenConfig
}
