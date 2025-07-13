package com.arny.aiprompts

import androidx.compose.runtime.Composable

/**
 * "Ожидаемый" Composable-экран.
 * Каждая платформа (Android, Desktop) обязана предоставить
 * свою собственную 'actual' реализацию этой функции.
 */
@Composable
expect fun AppScreen()
