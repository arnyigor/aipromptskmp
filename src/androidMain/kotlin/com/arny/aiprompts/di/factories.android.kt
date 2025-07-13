package com.arny.aiprompts.di


import android.annotation.SuppressLint
import android.content.Context
import com.arny.aiprompts.utils.StringProvider

// Для хранения экземпляра, созданного при старте приложения
@SuppressLint("StaticFieldLeak")
private lateinit var stringProviderInstance: StringProvider

// Функция для инициализации из класса Application
fun initStringProvider(context: Context) {
    stringProviderInstance = StringProvider(context.applicationContext)
}

// "Актуальная" реализация для Android: возвращает ранее созданный экземпляр
actual fun createStringProvider(): StringProvider {
    if (!::stringProviderInstance.isInitialized) {
        throw IllegalStateException("StringProvider has not been initialized. Call initStringProvider in your Application class.")
    }
    return stringProviderInstance
}
