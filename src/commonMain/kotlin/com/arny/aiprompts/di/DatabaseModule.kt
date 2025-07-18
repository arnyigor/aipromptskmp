package com.arny.aiprompts.di

import com.arny.aiprompts.db.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

// Ожидаем, что платформа предоставит готовый экземпляр AppDatabase
expect fun databaseModule(): Module

// Этот модуль остается, так как DAO не зависит от платформы
val daoModule = module {
    single { get<AppDatabase>().promptDao() }
}