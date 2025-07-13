package com.arny.aiprompts

import android.app.Application

// Переименовали для ясности, чтобы не путать с Composable App()
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Здесь можно инициализировать библиотеки, нужные для всего приложения.
        // Пока можно оставить пустым.
    }
}
