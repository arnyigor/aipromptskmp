package com.arny.aiprompts.sync

import co.touchlab.kermit.Logger
import com.arny.aiprompts.api.GitHubService
import com.arny.aiprompts.mappers.toDomain
import com.arny.aiprompts.models.Prompt
import com.arny.aiprompts.models.PromptJson
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

import okio.Path.Companion.toPath
import okio.openZip

class PromptSynchronizerImpl(
    private val githubService: GitHubService,
    private val promptsRepository: IPromptsRepository,
    private val settings: Settings,
    private val json: Json,
) : IPromptSynchronizer {

    companion object {
        private const val LAST_SYNC_KEY = "last_sync_timestamp"
        private val log = Logger.withTag("PromptSynchronizer")
    }

    private fun unzipWithOkio(zipFile: Path, destinationDir: Path) {
        // "Монтируем" zip-архив как файловую систему
        val zipFileSystem = FileSystem.SYSTEM.openZip(zipFile)

        // Рекурсивно обходим все файлы в архиве
        zipFileSystem.listRecursively("/".toPath()).forEach { pathInZip ->
            val metadata = zipFileSystem.metadata(pathInZip)
            val targetPath = destinationDir / pathInZip.toString().removePrefix("/")

            if (metadata.isDirectory) {
                FileSystem.SYSTEM.createDirectories(targetPath)
            } else {
                targetPath.parent?.let { FileSystem.SYSTEM.createDirectories(it) }
                // Копируем файл из виртуальной ФС в реальную
                zipFileSystem.source(pathInZip).use { source ->
                    FileSystem.SYSTEM.sink(targetPath).buffer().use { sink ->
                        sink.writeAll(source)
                    }
                }
            }
        }
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
                ) // Предполагаем, что сервис возвращает ByteArray или InputStream

                // 2. Сохраняем и распаковываем архив с помощью Okio
                val zipFile = tempDir / "repo.zip"
                FileSystem.SYSTEM.write(zipFile) { write(responseBody) }

                val unzippedDir = tempDir / "unzipped"
                FileSystem.SYSTEM.createDirectories(unzippedDir)
                unzipWithOkio(zipFile, unzippedDir)
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


    override suspend fun getLastSyncTime(): Long? = settings.getLongOrNull(LAST_SYNC_KEY)

    override suspend fun setLastSyncTime(timestamp: Long) {
        settings.putLong(LAST_SYNC_KEY, timestamp)
    }
}
