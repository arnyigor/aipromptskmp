package com.arny.aiprompts

import android.app.Application
import com.arny.aiprompts.di.initKoin
import com.arny.aiprompts.sync.ISyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AiPromptsAndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализируем Koin здесь
        initKoin {
            androidLogger() // Включаем логгирование для Android
            androidContext(this@AiPromptsAndroidApplication) // Предоставляем Context для Koin
        }
        // Запускаем проверку при старте
        val syncManager: ISyncManager = org.koin.java.KoinJavaComponent.get(ISyncManager::class.java)
        syncManager.syncIfNeeded()
    }
}
