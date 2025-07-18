package com.arny.aiprompts.di

import com.arny.aiprompts.platform.EncryptedJvmSettings
import com.arny.aiprompts.platform.SecureKeyStorage
import com.russhwolf.settings.Settings
import java.security.SecureRandom
import java.util.prefs.Preferences
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

actual class SettingsFactory {
    private val secureStorage = SecureKeyStorage()
    private val keyAlias = "app-settings-encryption-key"

    actual fun create(name: String): Settings {
        val delegate = Preferences.userRoot().node(name)

        var keyBytes = secureStorage.getKey(keyAlias)
        if (keyBytes == null) {
            keyBytes = ByteArray(32) // 256-bit AES key
            SecureRandom().nextBytes(keyBytes)
            secureStorage.saveKey(keyAlias, keyBytes)
        }

        val secretKey: SecretKey = SecretKeySpec(keyBytes, "AES")

        return EncryptedJvmSettings(delegate, secretKey)
    }
}
