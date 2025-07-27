package com.arny.aiprompts.api

import co.touchlab.kermit.Logger
import com.arny.aiprompts.models.GitHubCommitResponse
import com.arny.aiprompts.repositories.ISyncSettingsRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

// --- РЕШЕНИЕ: Создаем псевдоним (typealias) для нашего списка ---
typealias GitHubCommitListResponse = List<GitHubCommitResponse>

class GitHubService(
    private val httpClient: HttpClient,
    private val settingsRepo: ISyncSettingsRepository
) {

    private companion object {
        const val BASE_URL = "https://api.github.com/"
        const val OWNER = "arnyigor"
        const val REPO = "aiprompts"
        const val BRANCH = "master"
        val log = Logger.withTag("GitHubService")
    }

    /**
     * Скачивает zip-архив репозитория.
     * @return Массив байт скачанного архива.
     * @throws Exception если запрос не удался.
     */
    suspend fun downloadArchive(): ByteArray {
        val url = URLBuilder(BASE_URL).apply {
            path("repos", OWNER, REPO, "zipball", BRANCH)
        }.build()

        val response = httpClient.get(url) {
            // Ktor автоматически следует редиректам, что важно для скачивания архива с GitHub
            header("Accept", "application/vnd.github.v3+json")
        }

        if (response.status.isSuccess()) {
            return response.body<ByteArray>()
        } else {
            throw Exception("Failed to download archive: ${response.status} ${response.body<String>()}")
        }
    }

    suspend fun getLatestCommitHashForPath(path: String): String? {
        val url = URLBuilder(BASE_URL).apply {
            path("repos", OWNER, REPO, "commits")
            parameters.append("path", path)
            parameters.append("per_page", "1")
        }.build()

        return try {
            val response = httpClient.get(url) {
                // Улучшение: явно указываем, что ждем JSON
                header(HttpHeaders.Accept, "application/json")
                // --- ГЛАВНОЕ ИЗМЕНЕНИЕ ---
                // Добавляем заголовок авторизации, если токен есть
                val token = settingsRepo.getGitHubToken()
                if (!token.isNullOrBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            if (response.status.isSuccess()) {
                val commits = response.body<GitHubCommitListResponse>()
                commits.firstOrNull()?.sha
            } else {
                log.e { "Error fetching commit hash: ${response.status} ${response.body<String>()}" }
                // Полезно сообщить пользователю, если его токен невалиден
                if (response.status == HttpStatusCode.Unauthorized) {
                    log.e { "Authorization failed. The provided GitHub token might be invalid or expired." }
                }
                null
            }
        } catch (e: Exception) {
            log.e(e) { "Exception while fetching commit hash" }
            null
        }
    }
}
