package com.arny.aiprompts

import android.annotation.SuppressLint
import android.content.Context

// Простой объект для хранения ApplicationContext
@SuppressLint("StaticFieldLeak")
object AppContext {
    lateinit var INSTANCE: Context
    private set

    fun initialize(context: Context) {
        INSTANCE = context.applicationContext
    }
}