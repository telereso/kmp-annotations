package io.telereso.annotations.client.remote

import io.telereso.annotations.models.RocketLaunch
import io.telereso.kmp.core.Environment
import io.telereso.kmp.core.Http
import io.telereso.kmp.core.getPlatform
import io.telereso.kmp.core.httpClient
import io.telereso.annotations.client.cache.SettingsManager
import io.telereso.annotations.client.BuildKonfig
import io.ktor.client.plugins.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal interface AnnotationsApiService {

    companion object {
        // TODO Remove sample
        const val API_PATH = "https://api.spacexdata.com/v3/launches"
        const val TOKEN =
            "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1NGFlZGRiMmFkZjc0MGI3NWY2YTlkNzA4MGVlNWI3ZiIsInN1YiI6IjYzNjBlMTNhMGY1MjY1MDA4MWZkMzZmYiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.xGeFktn3m19LGXVjXfyzlxYYIo4-fKHeCc29H6gxgz4"

        fun getHost(environment: Environment?): String {
            return when (environment) {
                Environment.STAGING, Environment.PRODUCTION -> "api.spacexdata.com"
                else -> {
                    "api.spacexdata.com"
                }
            }
        }
    }

    // TODO Remove sample
    suspend fun fetchRockets(): List<RocketLaunch>
}

internal class AnnotationsApiServiceImpl(
    private val settingsManager: SettingsManager,
    private val httpClient: HttpClient,
    private val environment: Environment?,
    private val clientProtocol: String?,
    private val clientHost: String?
) : AnnotationsApiService {

    companion object {

        private fun host(host: String?, environment: Environment?): String {
            return if (!host.isNullOrEmpty()) host else AnnotationsApiService.getHost(environment)
        }

        fun getHttpClient(
            shouldLogHttpRequests: Boolean,
            interceptors: List<Any?>? = listOf(),
            requestTimeout: Long?,
            connectionTimeout: Long?,
            environment: Environment?,
            clientProtocol: String?,
            clientHost: String?,
            appName: String?,
            appVersion: String?
        ): HttpClient {
            return httpClient(
                shouldLogHttpRequests,
                interceptors,
                Http.getUserAgent(
                    getPlatform(),
                    BuildKonfig.SDK_NAME,
                    BuildKonfig.SDK_VERSION,
                    appName,
                    appVersion,
                )
            ) {

                install(HttpTimeout) {
                    requestTimeoutMillis =
                        if (requestTimeout != null && requestTimeout > 0L) requestTimeout else Http.REQUEST_TIME_OUT_MILLIS
                    connectTimeoutMillis =
                        if (connectionTimeout != null && connectionTimeout > 0L) connectionTimeout else Http.CONNECTION_TIME_OUT_MILLIS
                }

                expectSuccess = true

                defaultRequest {
                    url {
                        protocol = Http.protocol(clientProtocol)

                        host = host(clientHost, environment)
                    }
                }
            }
        }
    }

    /**
     * TODO Remove sample
     * we making a GET request for fetchRockets . Other HTTP methods are also available
    with Ktor: POST, PUT, DELETE, HEAD, OPTION and PATCH.
    If you look closely at these functions, you’ll see they’re declared using
    the keyword suspend . It’s used so the current thread won’t get blocked while
    waiting for a response
     */
    override suspend fun fetchRockets(): List<RocketLaunch> {
        settingsManager.fetchCount = settingsManager.fetchCount + 1
        return httpClient.get(AnnotationsApiService.API_PATH).body()
    }
}