package com.arny.aiprompts.di

import io.ktor.client.HttpClient

/**
 * "Ожидаем" функцию, которая умеет создавать HttpClient.
 * Теперь компилятор будет знать, что 'HttpClient' - это класс из библиотеки Ktor.
 */
expect fun createHttpClient(): HttpClient