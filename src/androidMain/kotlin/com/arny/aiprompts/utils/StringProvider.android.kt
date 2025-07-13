package com.arny.aiprompts.utils

import android.content.Context
import com.arny.aipromptskmp.R

// "Ожидаем" интерфейс, который умеет предоставлять строки
actual class StringProvider(private val context: Context) {
    actual fun getString(res: StringRes): String {
        val androidRes = when (res) {
            StringRes.SelectedModelNotFound -> R.string.selected_model_not_found
            StringRes.ApiKeyIsRequired -> R.string.api_key_is_required
            StringRes.EmptyResponseFromApi -> R.string.empty_response_from_api
        }
        return context.getString(androidRes)
    }
}