package com.arny.aiprompts.di

import com.arny.aiprompts.models.GitHubConfig

actual object ConfigProvider {
    actual val gitHubConfig: GitHubConfig = GitHubConfig(
        owner = System.getProperty("github.owner", "default-owner"),
        repo = System.getProperty("github.repo", "default-repo"),
        branch = System.getProperty("github.branch", "main"),
        promptsPath = System.getProperty("github.promptsPath", "prompts")
    )
}