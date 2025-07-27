package com.arny.aiprompts.platform

/**
 * Ожидаемая функция для открытия URL-адреса в браузере по умолчанию на платформе.
 */
expect fun openUrl(url: String)

/**
 * Ожидаемая функция для копирования текста в системный буфер обмена.
 */
expect fun copyToClipboard(text: String)