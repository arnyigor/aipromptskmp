package com.arny.aiprompts

import android.app.Application
import com.arny.aiprompts.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

// Переименовали для ясности, чтобы не путать с Composable App()
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализируем Koin здесь
        initKoin {
            androidLogger() // Включаем логгирование для Android
            androidContext(this@MyApplication) // Предоставляем Context для Koin
        }
    }
}
