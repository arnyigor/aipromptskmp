package com.arny.aiprompts.sync

import co.touchlab.kermit.Logger
import com.arny.aiprompts.api.GitHubService
import com.arny.aiprompts.models.SyncResult
import com.arny.aiprompts.repositories.IPromptSynchronizer
import com.arny.aiprompts.repositories.ISyncSettingsRepository
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

interface ISyncManager {
    fun syncIfNeeded()
}

class SyncManagerImpl(
    private val settingsRepo: ISyncSettingsRepository,
    private val githubService: GitHubService,
    private val synchronizer: IPromptSynchronizer
) : ISyncManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val log = Logger.withTag("SyncManager")
    private val promptsFolderPath = "prompts"

    override fun syncIfNeeded() {
        scope.launch {
            val lastCheckTime = Instant.fromEpochMilliseconds(settingsRepo.getLastCheckTimestamp())
            if (lastCheckTime > Clock.System.now() - 1.days) {
                log.i { "Sync check skipped: less than 24 hours ago." }
                return@launch
            }

            log.i { "Time to check for updates..." }
            val remoteHash = githubService.getLatestCommitHashForPath(promptsFolderPath)
            if (remoteHash == null) {
                log.w { "Could not fetch remote commit hash. Skipping sync." }
                settingsRepo.saveLastCheckTimestamp() // Обновляем время, чтобы не спамить запросами
                return@launch
            }

            val localHash = settingsRepo.getLastCommitHash()
            if (remoteHash == localHash) {
                log.i { "Content is up-to-date. Hash: $remoteHash" }
                settingsRepo.saveLastCheckTimestamp()
                return@launch
            }

            log.i { "New version detected (local: $localHash, remote: $remoteHash). Starting sync..." }
            val result = synchronizer.synchronize() // Запускаем полную синхронизацию
            if (result is SyncResult.Success) {
                log.i { "Sync successful." }
                settingsRepo.saveLastCommitHash(remoteHash)
            }
            // В любом случае обновляем время последней ПРОВЕРКИ
            settingsRepo.saveLastCheckTimestamp()
        }
    }
}
