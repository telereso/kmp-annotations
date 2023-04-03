package io.telereso.annotations.client.cache

/**
 * Sso Shared Preferences and settings manager Interface
 */
internal interface ISettingsManager {

    var fetchCount: Int

    /**
     * fun clears all values stored
     */
    fun clearAllSettings()

    companion object {
        const val KEY_FETCH_COUNT = "settings_api_key"
    }
}
