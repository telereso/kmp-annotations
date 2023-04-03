package io.telereso.annotations.client.cache

import io.telereso.kmp.core.Settings
import io.telereso.kmp.core.get
import io.telereso.kmp.core.set
import io.telereso.annotations.client.cache.ISettingsManager.Companion.KEY_FETCH_COUNT

internal class SettingsManager(private val settings: Settings) : ISettingsManager {

    override var fetchCount: Int
        get() = settings[KEY_FETCH_COUNT] ?: 0
        set(value) {
            settings[KEY_FETCH_COUNT] = value
        }

    override fun clearAllSettings() {
        settings.clear()
    }
}
