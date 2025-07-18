package com.arny.aiprompts.sync

import co.touchlab.kermit.Logger
import com.arny.aiprompts.api.GitHubService
import com.arny.aiprompts.models.GitHubConfig
import com.arny.aiprompts.models.Prompt
import com.arny.aiprompts.models.SyncResult
import com.arny.aiprompts.repositories.IPromptSynchronizer
import com.arny.aiprompts.repositories.IPromptsRepository
import com.arny.aiprompts.platform.getCacheDir
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer

// Вместо @Inject, зависимости будут предоставляться через Koin-модуль
class PromptSynchronizerImpl(
    private val githubService: GitHubService,
    private val promptsRepository: IPromptsRepository,
    private val settings: Settings,
    private val json: Json, // Используем kotlinx.serialization.json
    private val config: GitHubConfig,
) : IPromptSynchronizer {

    companion object {
        private const val LAST_SYNC_KEY = "last_sync_timestamp"
        private val log = Logger.withTag("PromptSynchronizer")
    }

    override suspend fun synchronize(): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Создаем временную директорию с помощью Okio
            val tempDir = getCacheDir() / "aipromptmaster_${Clock.System.now().toEpochMilliseconds()}"
            FileSystem.SYSTEM.createDirectories(tempDir)

            log.i { "Created temp directory: $tempDir" }

            try {
                // 1. Загружаем архив
                val responseBody = githubService.downloadArchive(
                    owner = config.owner,
                    repo = config.repo,
                    ref = config.branch
                ) // Предполагаем, что сервис возвращает ByteArray или InputStream

                // 2. Сохраняем и распаковываем архив с помощью Okio
                val zipFile = tempDir / "repo.zip"
                FileSystem.SYSTEM.write(zipFile) { write(responseBody) }

                val unzippedDir = tempDir / "unzipped"
                FileSystem.SYSTEM.createDirectories(unzippedDir)
                zipFile.unzip(unzippedDir)

                log.i { "Archive unzipped to: $unzippedDir" }

                // 3. Ищем директорию prompts
                val promptsDir = findPromptsDirectory(unzippedDir)
                    ?: return@withContext SyncResult.Error("Директория prompts не найдена в архиве")

                log.i { "Found prompts directory: $promptsDir" }

                // 4. Обрабатываем JSON файлы
                val remotePrompts = mutableListOf<Prompt>()
                val errors = mutableListOf<String>()
                processDirectory(promptsDir, remotePrompts, errors)

                if (remotePrompts.isEmpty() && errors.isEmpty()) {
                    log.w { "No prompts found in archive" }
                    return@withContext SyncResult.Error("Не найдены промпты в репозитории")
                }

                if (errors.isNotEmpty()) {
                    log.w { "Sync completed with errors: ${errors.joinToString()}" }
                    return@withContext SyncResult.Error("Синхронизация завершена с ошибками:\n${errors.joinToString("\n")}")
                }

                // 5. Обрабатываем удаленные и сохраняем обновленные
                handleDeletedPrompts(remotePrompts)
                promptsRepository.savePrompts(remotePrompts)
                settings.putLong(LAST_SYNC_KEY, Clock.System.now().toEpochMilliseconds())

                log.i { "Sync completed successfully, processed ${remotePrompts.size} prompts." }
                SyncResult.Success(remotePrompts)

            } finally {
                // 6. Очищаем временные файлы
                FileSystem.SYSTEM.deleteRecursively(tempDir)
                log.i { "Deleted temp directory: $tempDir" }
            }
        } catch (e: Exception) {
            log.e(e) { "Unexpected error during sync" }
            SyncResult.Error("Непредвиденная ошибка: ${e.message}")
        }
    }

    private fun findPromptsDirectory(dir: Path): Path? {
        if (dir.name == "prompts") return dir
        return FileSystem.SYSTEM.list(dir).firstNotNullOfOrNull { path ->
            if (FileSystem.SYSTEM.metadata(path).isDirectory) {
                findPromptsDirectory(path)
            } else {
                null
            }
        }
    }

    private fun processDirectory(
        dir: Path,
        remotePrompts: MutableList<Prompt>,
        errors: MutableList<String>
    ) {
        FileSystem.SYSTEM.list(dir).forEach { file ->
            val metadata = FileSystem.SYSTEM.metadata(file)
            when {
                metadata.isDirectory -> processDirectory(file, remotePrompts, errors)
                file.name.endsWith(".json") -> {
                    try {
                        val jsonContent = FileSystem.SYSTEM.source(file).buffer().readUtf8()
                        val promptJson = json.decodeFromString<PromptJson>(jsonContent)
                        remotePrompts.add(promptJson.toDomain())
                    } catch (e: Exception) {
                        log.e(e) { "Error processing file ${file.name}" }
                        errors.add("Ошибка обработки файла ${file.name}: ${e.message}")
                    }
                }
            }
        }
    }

    private suspend fun handleDeletedPrompts(remotePrompts: List<Prompt>) {
        // Эта логика остается почти без изменений, так как она работает с репозиторием
        val localPrompts = promptsRepository.getPrompts(limit = Int.MAX_VALUE)
        val remoteIds = remotePrompts.map { it.id }.toSet()
        val idsToDelete = localPrompts
            .filter { !it.isLocal && it.id !in remoteIds }
            .map { it.id }

        if (idsToDelete.isNotEmpty()) {
            log.i { "Deleting ${idsToDelete.size} prompts that are no longer on remote." }
            promptsRepository.deletePromptsByIds(idsToDelete)
        }
    }

    override suspend fun getLastSyncTime(): Long? {
        return settings.getLongOrNull(LAST_SYNC_KEY)
    }
}
