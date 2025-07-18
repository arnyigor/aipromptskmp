package com.arny.aiprompts.di

import com.arny.aiprompts.db.AppDatabase
import com.arny.aiprompts.db.getDatabaseBuilder
import com.arny.aiprompts.db.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun databaseModule() = module {
    single<AppDatabase> {
        val builder = getDatabaseBuilder(context = androidContext())
        getRoomDatabase(builder)
    }
}