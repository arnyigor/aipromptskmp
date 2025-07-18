package com.arny.aipromptmaster.data.mappers

import com.arny.aiprompts.models.ChatCompletionResponse
import com.arny.aiprompts.models.ChatCompletionResponseDTO
import com.arny.aiprompts.models.Choice
import com.arny.aiprompts.models.ChoiceDTO
import com.arny.aiprompts.models.Message
import com.arny.aiprompts.models.MessageDTO
import com.arny.aiprompts.models.Usage
import com.arny.aiprompts.models.UsageDTO


object ChatMapper {
    fun toDomain(dto: ChatCompletionResponseDTO): ChatCompletionResponse {
        return ChatCompletionResponse(
            id = dto.id,
            choices = dto.choices.map { toDomainChoice(it) },
            usage = dto.usage?.let { toDomainUsage(it) }
        )
    }

    private fun toDomainChoice(dto: ChoiceDTO): Choice {
        return Choice(
            message = toDomainMessage(dto.message),
            finishReason = dto.finishReason
        )
    }

    private fun toDomainMessage(dto: MessageDTO): Message {
        return Message(
            role = dto.role,
            content = dto.content
        )
    }

    private fun toDomainUsage(dto: UsageDTO): Usage {
        return Usage(
            promptTokens = dto.promptTokens,
            completionTokens = dto.completionTokens,
            totalTokens = dto.totalTokens
        )
    }
}