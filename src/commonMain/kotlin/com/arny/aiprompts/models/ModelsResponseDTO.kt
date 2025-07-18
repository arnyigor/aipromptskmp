package com.arny.aiprompts.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ModelsResponseDTO(
    @SerialName("data") val models: List<ModelDTO>
)
