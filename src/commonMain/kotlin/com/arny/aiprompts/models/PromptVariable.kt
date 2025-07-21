package com.arny.aiprompts.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromptVariable(
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("description") val description: String
)