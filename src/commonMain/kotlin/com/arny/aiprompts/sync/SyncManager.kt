package com.arny.aiprompts.sync

import co.touchlab.kermit.Logger
import com.arny.aiprompts.api.GitHubService
import com.arny.aiprompts.models.SyncResult
import com.arny.aiprompts.models.SyncStatus
import com.arny.aiprompts.models.SyncStatus.*
import com.arny.aiprompts.repositories.IPromptSynchronizer
import com.arny.aiprompts.repositories.IPromptsRepository
import com.arny.aiprompts.repositories.ISyncSettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

interface ISyncManager {
    val syncState: StateFlow<SyncStatus>
    fun sync()
}

class SyncManagerImpl(
    private val settingsRepo: ISyncSettingsRepository,
    private val githubService: GitHubService,
    private val synchronizer: IPromptSynchronizer,
    private val promptsRepository: IPromptsRepository
) : ISyncManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val log = Logger.withTag("SyncManager")
    private val promptsFolderPath = "prompts"

    private val _syncState = MutableStateFlow<SyncStatus>(SyncStatus.None)
    override val syncState: StateFlow<SyncStatus> = _syncState.asStateFlow()

    override fun sync() {
        scope.launch {
            if (_syncState.value == SyncStatus.InProgress) {
                log.i { "Sync already in progress." }
                return@launch
            }
            _syncState.value = SyncStatus.InProgress

            val promptsCount = promptsRepository.getPromptsCount()
            val lastCheckTime = Instant.fromEpochMilliseconds(settingsRepo.getLastCheckTimestamp())
            val isTimeToCheck = lastCheckTime < Clock.System.now() - 1.days

            if (promptsCount == 0) {
                log.i { "No prompts found, forcing sync." }
            } else if (!isTimeToCheck) {
                log.i { "Sync check skipped: less than 24 hours ago." }
                _syncState.value = SyncStatus.None
                return@launch
            }

            log.i { "Time to check for updates..." }
            val remoteHash = githubService.getLatestCommitHashForPath(promptsFolderPath)
            if (remoteHash == null) {
                val errorMessage = "Could not fetch remote commit hash. Skipping sync."
                log.w { errorMessage }
                _syncState.value = SyncStatus.Error(errorMessage)
                return@launch
            }

            val localHash = settingsRepo.getLastCommitHash()
            if (remoteHash == localHash) {
                log.i { "Content is up-to-date. Hash: $remoteHash" }
                settingsRepo.saveLastCheckTimestamp()
                _syncState.value = SyncStatus.Success(0)
                return@launch
            }

            log.i { "New version detected (local: $localHash, remote: $remoteHash). Starting sync..." }
            when (val result = synchronizer.synchronize()) {
                is SyncResult.Success -> {
                    log.i { "Sync successful." }
                    settingsRepo.saveLastCommitHash(remoteHash)
                    settingsRepo.saveLastCheckTimestamp()
                    _syncState.value = Success(result.updatedPrompts.size)
                }



                is SyncResult.Error -> {
                    log.e { "Sync failed: ${result.message}" }
                    _syncState.value = Error(result.message)
                }

                is SyncResult.Conflicts -> {}
                is SyncResult.Skipped -> {}
            }
        }
    }
}
