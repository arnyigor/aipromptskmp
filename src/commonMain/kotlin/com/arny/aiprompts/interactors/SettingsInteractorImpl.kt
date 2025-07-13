package com.arny.aiprompts.interactors

import com.arny.aipromptmaster.domain.interactors.ISettingsInteractor
import com.arny.aiprompts.repositories.ISettingsRepository

class SettingsInteractorImpl(
    private val settingsRepository: ISettingsRepository
) : ISettingsInteractor {
    override fun saveApiKey(apiKey: String) {
        settingsRepository.saveApiKey(apiKey)
    }

    override fun getApiKey(): String? = settingsRepository.getApiKey()
}