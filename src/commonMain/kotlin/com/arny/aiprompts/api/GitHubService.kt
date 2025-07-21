package com.arny.aiprompts.api

import com.arny.aiprompts.models.GitHubCommitResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class GitHubService(private val httpClient: HttpClient) {

    private companion object {
        const val BASE_URL = "https://api.github.com/"
        const val OWNER = "arnyigor"
        const val REPO = "aiprompts"
        const val BRANCH = "master"
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
            val response = httpClient.get(url)
            val commits = response.body<List<GitHubCommitResponse>>()
            commits.firstOrNull()?.sha
        } catch (e: Exception) {
            e.printStackTrace()
            // Здесь можно добавить логирование
            null
        }
    }
}
