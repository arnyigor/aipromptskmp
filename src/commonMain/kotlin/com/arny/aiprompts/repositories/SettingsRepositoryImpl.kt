package com.arny.aiprompts.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl : ISettingsRepository {

    private val _selectedId = MutableStateFlow<String?>(null)

    override fun saveApiKey(apiKey: String) {

    }

    override fun getApiKey(): String? {
        return "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"
    }

    override fun setSelectedModelId(id: String?) {
        _selectedId.update { id }
    }

    override fun getSelectedModelId(): Flow<String?> = _selectedId
}
