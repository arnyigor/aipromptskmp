package com.arny.aiprompts.utils

// "Ожидаем" интерфейс, который умеет предоставлять строки
actual class StringProvider {
    actual fun getString(res: StringRes): String {
        return when (res) {
            StringRes.SelectedModelNotFound -> "Selected model not found"
            StringRes.ApiKeyIsRequired -> "API key is required"
            StringRes.EmptyResponseFromApi -> "Empty response from API"
        }
    }
}