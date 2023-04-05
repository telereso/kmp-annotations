package io.telereso.annotations.client.repositories

import io.telereso.annotations.models.RocketLaunch
import io.telereso.kmp.core.Log.logDebug
import io.telereso.kmp.core.Log.logError
import io.telereso.annotations.client.cache.Dao
import io.telereso.annotations.client.cache.SettingsManager
import io.telereso.annotations.client.remote.AnnotationsApiService
import io.telereso.kmp.core.CommonFlow

internal class AnnotationsClientRepository(
    private val settingsManager: SettingsManager,
    private val apiService: AnnotationsApiService,
    private val dao: Dao
) {

    suspend fun initDatabase() {
        dao.getAllRocketLaunches()
    }

    @Throws(Exception::class)
    suspend fun getLaunchRockets(
        forceReload: Boolean = false
    ): List<RocketLaunch> {
        return getLaunchRockets(forceReload, null)
    }

    @Throws(Exception::class)
    suspend fun getLaunchRockets(
        forceReload: Boolean = false,
        type: RocketLaunch.Type? = null
    ): List<RocketLaunch> {
        logDebug("getLaunchRockets , count ${settingsManager.fetchCount}")
        val cachedLaunches = dao.getAllRocketLaunches()

        return if (cachedLaunches.isNotEmpty() && !forceReload) {
            cachedLaunches
        } else {
            try {
                val allLaunches: List<RocketLaunch> = apiService.fetchRockets()
                if (allLaunches.isNotEmpty()) {
                    allLaunches.also {
                        dao.clearDatabase()
                        dao.insertRocketLaunches(it)
                    }
                } else {
                    // Check if any. So here if the server error or any exception we return an error result and some cached items.
                    // We can also return and empty list instead of throwing an Error.
                    cachedLaunches.ifEmpty { listOf() }
                    //cachedLaunches.ifEmpty { throw Exception("Fetched Rockets") }
                }
            } catch (e: Exception) {
                logError(e)
                cachedLaunches.ifEmpty { throw e }
            }
        }
    }

    suspend fun getFirstRocketLaunchFlow(): CommonFlow<RocketLaunch?> {
        return dao.getFirstRocketLaunches()
    }

    suspend fun getRocketLaunchesFlow(param: String = ""): CommonFlow<List<RocketLaunch>> {
        return dao.getRocketLaunchesFlow()
    }
}
