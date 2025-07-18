package com.arny.aiprompts.di

import com.arny.aiprompts.models.GitHubConfig
import com.arny.aipromptskmp.BuildConfig

actual object ConfigProvider {
    actual val gitHubConfig: GitHubConfig = GitHubConfig(
        owner = BuildConfig.GITHUB_OWNER,
        repo = BuildConfig.GITHUB_REPO,
        branch = BuildConfig.GITHUB_BRANCH,
        promptsPath = BuildConfig.GITHUB_PROMPTS_PATH
    )
}