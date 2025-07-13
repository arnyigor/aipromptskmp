package com.arny.aiprompts.di

import com.arny.aiprompts.utils.StringProvider

// "Актуальная" реализация для Desktop: просто создает экземпляр
actual fun createStringProvider(): StringProvider = StringProvider()
