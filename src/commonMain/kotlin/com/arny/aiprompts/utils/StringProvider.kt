package com.arny.aiprompts.utils

enum class StringRes {
    SelectedModelNotFound,
    ApiKeyIsRequired,
    EmptyResponseFromApi
}

// "Ожидаем" интерфейс, который умеет предоставлять строки
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class StringProvider {
    fun getString(res: StringRes): String
}
