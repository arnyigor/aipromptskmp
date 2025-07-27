package com.arny.aiprompts.platform

import co.touchlab.kermit.Logger
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

/**
 * Актуальная реализация для Desktop.
 * Использует java.awt.Desktop для открытия URL в системном браузере.
 */
actual fun openUrl(url: String) {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(URI.create(url))
        } catch (e: Exception) {
            Logger.withTag("PlatformUtils").e(e) { "Не удалось открыть URL: $url" }
        }
    } else {
        Logger.withTag("PlatformUtils").w { "Desktop browse action не поддерживается" }
    }
}


/**
 * Актуальная реализация для Desktop.
 * Использует AWT Toolkit для копирования текста.
 */
actual fun copyToClipboard(text: String) {
    val selection = StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(selection, selection)
}