package com.arny.aiprompts.di

import com.arny.aiprompts.utils.StringProvider

// "Ожидаем" функцию, которая вернет нам готовый экземпляр StringProvider
expect fun createStringProvider(): StringProvider
