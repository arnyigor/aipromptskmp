package com.arny.aiprompts.mappers

import com.arny.aiprompts.db.entities.PromptEntity
import com.arny.aiprompts.models.*
import com.arny.aiprompts.utils.toInstant
import com.arny.aiprompts.utils.toIsoString
import com.arny.aiprompts.utils.toJavaDate
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import java.util.*

// Domain -> Entity
fun Prompt.toEntity(): PromptEntity = PromptEntity(
    id = id,
    title = title,
    contentRu = content.ru,
    contentEn = content.en,
    description = description,
    category = category,
    status = status,
    tags = tags.joinToString(","),
    isLocal = isLocal,
    isFavorite = isFavorite,
    rating = rating,
    ratingVotes = ratingVotes,
    compatibleModels = compatibleModels.joinToString(),
    author = metadata.author.name,
    authorId = metadata.author.id,
    source = metadata.source,
    notes = metadata.notes,
    version = version,
    createdAt = this.createdAt?.toInstant()?.toString().orEmpty(),
    modifiedAt = this.modifiedAt?.toInstant()?.toString().orEmpty()
)

// Entity -> Domain
fun PromptEntity.toDomain(): Prompt = Prompt(
    id = id,
    title = title,
    content = PromptContent(
        ru = contentRu,
        en = contentEn
    ),
    description = description,
    category = category,
    status = status,
    tags = tags.split(",").filter { it.isNotBlank() },
    isLocal = isLocal,
    isFavorite = isFavorite,
    rating = rating,
    ratingVotes = ratingVotes,
    compatibleModels = compatibleModels.split(","),
    metadata = PromptMetadata(
        author = Author(
            id = authorId,
            name = author
        ),
        source = source,
        notes = notes
    ),
    version = version,
    createdAt = Instant.parse(createdAt).toJavaDate(),
    modifiedAt = Instant.parse(modifiedAt).toJavaDate(),
)

// API -> Domain
fun PromptJson.toDomain(): Prompt = Prompt(
    id = id ?: UUID.randomUUID().toString(),
    title = title.orEmpty(),
    description = description,
    content = PromptContent(
        ru = content["ru"].orEmpty(),
        en = content["en"].orEmpty()
    ),
    variables = variables.associate { it.name to it.type },
    compatibleModels = compatibleModels,
    category = category.orEmpty().lowercase(),
    tags = tags,
    isLocal = isLocal,
    isFavorite = isFavorite,
    rating = rating?.score ?: 0.0f,
    ratingVotes = rating?.votes ?: 0,
    status = status.orEmpty().lowercase(),
    metadata = PromptMetadata(
        author = Author(
            id = metadata?.author?.name.orEmpty(),
            name = metadata?.author?.name.orEmpty()
        ),
        source = metadata?.source.orEmpty(),
        notes = metadata?.notes.orEmpty()
    ),
    version = version.orEmpty(),
    createdAt = createdAt?.let { LocalDateTime.parse(it).toInstant() }?.toJavaDate(),
    modifiedAt = updatedAt?.let { LocalDateTime.parse(it).toInstant() }?.toJavaDate()
)
