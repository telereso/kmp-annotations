package io.telereso.annotations.client.remote

import ApiMockEngine
import ApiMockEngineParams
import io.telereso.kmp.core.Settings
import io.telereso.kmp.core.Http
import io.telereso.annotations.client.Resource
import io.telereso.annotations.client.cache.SettingsManager
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class AnnotationsApiServiceImplTest {

    private lateinit var apiService: AnnotationsApiService

    /**
     * https://kotest.io/docs/assertions/ktor-matchers.html
     */
    @BeforeTest
    fun before() {
        val httpClient = HttpClient(
            ApiMockEngine(
                ApiMockEngineParams(
                    Resource("launches.json").readText(),
                    encodedPath = "/v3/launches"
                )
            ).get()
        ) {
            install(ContentNegotiation) {
                json(Http.ktorConfigJson)
            }
        }
        apiService = AnnotationsApiServiceImpl(SettingsManager(Settings.getInMemory()),httpClient, null, null, null)
    }

    @AfterTest
    fun after() {

    }

    @Test
    @Ignore
    fun fetchRockets() = runTest {
        val result = apiService.fetchRockets()
        result.size.shouldBe(111)
    }
}