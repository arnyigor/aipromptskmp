package com.arny.aiprompts.di

import org.koin.dsl.module

actual val platformModule = module {
    single<SettingsFactory> { SettingsFactory() }
}