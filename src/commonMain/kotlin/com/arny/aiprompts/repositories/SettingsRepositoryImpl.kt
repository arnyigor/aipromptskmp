package com.arny.aiprompts.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl : ISettingsRepository {

    private val _selectedId = MutableStateFlow<String?>(null)

    override fun saveApiKey(apiKey: String) {

    }

    override fun getApiKey(): String? {
        return ""
    }

    override fun setSelectedModelId(id: String?) {
        _selectedId.update { id }
    }

    override fun getSelectedModelId(): Flow<String?> = _selectedId
}
