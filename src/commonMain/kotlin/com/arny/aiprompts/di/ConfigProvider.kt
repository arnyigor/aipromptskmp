package com.arny.aiprompts.di

import com.arny.aiprompts.models.GitHubConfig

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object ConfigProvider {
    val gitHubConfig: GitHubConfig
}