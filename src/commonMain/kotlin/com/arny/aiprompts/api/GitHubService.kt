package com.arny.aiprompts.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class GitHubService(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://api.github.com/"
    }

    /**
     * Скачивает zip-архив репозитория.
     * @return Массив байт скачанного архива.
     * @throws Exception если запрос не удался.
     */
    suspend fun downloadArchive(
        owner: String,
        repo: String,
        ref: String
    ): ByteArray {
        val url = URLBuilder(BASE_URL).apply {
            path("repos", owner, repo, "zipball", ref)
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
}
