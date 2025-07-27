package com.arny.aiprompts.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import co.touchlab.kermit.Logger
import com.arny.aiprompts.AppContext

/**
 * Актуальная реализация для Android.
 * Использует Intent для открытия URL.
 */
actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        // Флаг важен для запуска из контекста, не являющегося Activity
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        AppContext.INSTANCE.startActivity(intent)
    } catch (e: Exception) {
        Logger.withTag("PlatformUtils").e(e) { "Не удалось открыть URL: $url" }
    }
}

/**
 * Актуальная реализация для Android.
 * Использует ClipboardManager для копирования текста.
 */
actual fun copyToClipboard(text: String) {
    val clipboard = AppContext.INSTANCE.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("AiPromptMaster-Token", text)
    clipboard.setPrimaryClip(clip)
}