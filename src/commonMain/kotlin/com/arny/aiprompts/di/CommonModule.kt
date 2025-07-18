package com.arny.aiprompts.di

import com.arny.aiprompts.models.GitHubConfig
import com.russhwolf.settings.Settings
import org.koin.dsl.module

const val APP_SETTINGS_NAME = "app_secure_settings"

val commonModule = module {
    single<Settings> {
        get<SettingsFactory>().create(APP_SETTINGS_NAME)
    }
    single<GitHubConfig> { ConfigProvider.gitHubConfig }
}
