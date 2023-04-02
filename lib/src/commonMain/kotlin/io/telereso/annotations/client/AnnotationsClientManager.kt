package io.telereso.annotations.client

import io.telereso.annotations.client.cache.Dao
import io.telereso.annotations.client.cache.SharedDatabase
import io.telereso.annotations.client.*
import io.telereso.annotations.models.RocketLaunch
import io.telereso.kmp.core.*
import io.telereso.annotations.client.cache.SettingsManager
import kotlinx.coroutines.*
import io.telereso.annotations.client.remote.AnnotationsApiServiceImpl
import io.telereso.annotations.client.repositories.AnnotationsClientRepository
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.telereso.annotations.models.RocketLaunchList
import io.telereso.kmp.annotations.ReactNativeExport
import io.telereso.kmp.annotations.SkipReactNativeExport
import io.telereso.kmp.core.models.ClientException
import io.telereso.kmp.core.models.ErrorBody
import io.telereso.kmp.core.models.fromJson
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.native.concurrent.ThreadLocal

/**
 * This class acts as the point of entry into the client sdk.
 *
 * @param databaseDriverFactory provides a SqlDriver needed to configure the database.
 * @param interceptors list of intercetors to be added on the Android platform HttpClient engine.
 * @constructor a private constructor due to the build pattern.
 */
@ExperimentalJsExport
@JsExport
@ReactNativeExport
class AnnotationsClientManager private constructor(
    databaseDriverFactory: DatabaseDriverFactory? = null,
    private val builder: Builder,
    config: Config? = null
) {

    @ThreadLocal
    companion object {
        /**
         * a Kotlin DSL fun that uses scope to build the AnnotationsClientManager.
         * @param databaseDriverFactory a mandatory value needed to be passed
         */
        inline fun client(
            databaseDriverFactory: DatabaseDriverFactory,
            block: Builder.() -> Unit
        ) =
            Builder(databaseDriverFactory)
                .apply(block)
                .build()

        private var instance: AnnotationsClientManager? = null

        @Throws(NullPointerException::class)
        fun getInstance(): AnnotationsClientManager {
            if (instance == null) throw NullPointerException("AnnotationsClientManager instance cannot be null!")
            return instance!!
        }

    }

    private val settingsManager by lazy {
        SettingsManager(settings)
    }

    private val repo: AnnotationsClientRepository by lazy {
        val httpClient = AnnotationsApiServiceImpl.getHttpClient(
            config?.builder?.logHttpRequests ?: false,
            config?.builder?.interceptors,
            config?.builder?.requestTimeoutMillis,
            config?.builder?.connectTimeoutMillis,
            config?.builder?.environment,
            config?.builder?.protocol,
            config?.builder?.host,
            config?.builder?.appName,
            config?.builder?.appVersion,
        ).config {
            HttpResponseValidator {
                handleResponseExceptionWithRequest { exception, _ ->
                    when (exception) {
                        is ClientRequestException -> {
                            throw ClientException(
                                exception.message,
                                exception,
                                exception.response.status.value,
                                ErrorBody.fromJson(exception.response.bodyAsText())
                            )
                        }
                        is ServerResponseException -> {
                            throw ClientException(
                                exception.message,
                                exception,
                                exception.response.status.value,
                                ErrorBody.fromJson(exception.response.bodyAsText())
                            )
                        }
                        else -> {
                            throw ClientException(exception)
                        }
                    }
                }
            }
        }
        AnnotationsClientRepository(
            settingsManager,
            AnnotationsApiServiceImpl(
                settingsManager,
                httpClient,
                config?.builder?.environment,
                config?.builder?.protocol,
                config?.builder?.host
            ), Dao(SharedDatabase(::provideDbDriver, databaseDriverFactory))
        )
    }

    init {
        instance = this
        // init database for js as soon as the manger is created , due to sql.wasm loading requirements
        when (getPlatform().type) {
            Platform.TYPE.BROWSER -> {
                ContextScope.get(Dispatchers.Default).launch {
                    repo.initDatabase()
                }
            }
            else -> {}
        }
    }

    /**
     * TODO Remove sample
     * Fetches RocketLaunch from network and cache based on the configuration sent.
     * @param forceReload if true the api will be hit, if false the api will only be hit if
     * no cache available.
     * @return a Task array of RocketLaunch items.
     */
    fun fetchLaunchRockets(forceReload: Boolean): Task<Array<RocketLaunch>> {
        return Task.execute {
            repo.getLaunchRockets(forceReload).toTypedArray()
        }
    }

    fun testDefaultParam(param: String = "") {

    }

    fun getFlow(param: String = ""): CommonFlow<String> {
        return param.split(",").toList().asFlow().asCommonFlow()
    }

    fun getFirstRocketLaunchFlow(): Task<CommonFlow<RocketLaunch?>> {
        return Task.execute {
            repo.getFirstRocketLaunchFlow()
        }
    }

    fun getRocketLaunchesFlow(param: String = ""): Task<CommonFlow<List<RocketLaunch>>> {
        return Task.execute {
            repo.getRocketLaunchesFlow()
        }
    }

    fun getRocketLaunchListFlow(rocketLaunch: RocketLaunch): Task<CommonFlow<RocketLaunchList>> {
        return Task.execute {
            repo.getRocketLaunchesFlow().map { RocketLaunchList(it) }.asCommonFlow()
        }
    }

    fun getRocketLaunchFlow(param: String = ""): Task<CommonFlow<Array<RocketLaunch>>> {
        return Task.execute {
            repo.getRocketLaunchesFlow().map { it.toTypedArray() }.asCommonFlow()
        }
    }

    @SkipReactNativeExport
    fun testSkip(param: String = "") {

    }

    /**
     * We may need to pass some compulsory values to the builder.
     * Mandatory param values are part of the constructor.
     */
    class Builder(
        val databaseDriverFactory: DatabaseDriverFactory
    ) {

        internal var config: Config? = null

        /**
         * set a listener for overall client events
         */
        fun withConfig(config: Config): Builder {
            this.config = config
            return this
        }

        fun build(): AnnotationsClientManager {
            return instance ?: AnnotationsClientManager(databaseDriverFactory, this, config)
        }
    }
}
