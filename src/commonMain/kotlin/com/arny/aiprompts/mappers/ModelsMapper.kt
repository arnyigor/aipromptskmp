package com.arny.aiprompts.mappers

import com.arny.aiprompts.models.LlmModel
import com.arny.aiprompts.models.ModelDTO

fun ModelDTO.toDomain(): LlmModel = LlmModel(
    id = id,
    name = name.orEmpty(),
    description = description.orEmpty(),
    isSelected = false,
    contextLength = contextLength,
    created = createdAt?:0L,
    inputModalities = architecture?.inputModalities.orEmpty(),
    outputModalities = architecture?.outputModalities.orEmpty(),
    pricingPrompt = pricing?.prompt,
    pricingCompletion = pricing?.completion,
    pricingImage = pricing?.image
)