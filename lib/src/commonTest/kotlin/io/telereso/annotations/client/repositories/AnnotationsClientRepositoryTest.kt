package io.telereso.annotations.client.repositories

import ApiMockEngine
import ApiMockEngineParams
import io.telereso.kmp.core.Http
import io.telereso.kmp.core.Settings
import io.telereso.annotations.client.Resource
import io.telereso.annotations.client.cache.Dao
import io.telereso.annotations.client.cache.SettingsManager
import io.telereso.annotations.client.cache.SharedDatabase
import io.telereso.annotations.client.provideDbDriverTest
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import io.telereso.annotations.client.remote.AnnotationsApiService
import io.telereso.annotations.client.remote.AnnotationsApiServiceImpl
import io.telereso.annotations.client.repositories.AnnotationsClientRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class AnnotationsClientRepositoryTest {
    private lateinit var repository: AnnotationsClientRepository
    private lateinit var apiService: AnnotationsApiService
    private lateinit var dao: Dao

    @BeforeTest
    fun before() {

        val httpMockClient = HttpClient(
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

        val settingsManager = SettingsManager(Settings.getInMemory())
        apiService = AnnotationsApiServiceImpl(settingsManager, httpMockClient, null, null, null)
        val sharedDatabase = SharedDatabase(::provideDbDriverTest, null)
        dao = Dao(sharedDatabase)
        repository = AnnotationsClientRepository(settingsManager, apiService, dao)
    }

    @AfterTest
    fun after() {
    }

    @Test
    @Ignore
    fun fetchRockets() = runTest {
        val result = repository.getLaunchRockets()

        // Verify result
        result.size.shouldBe(111)

        // Confirm saved in db
        dao.getAllRocketLaunches().run {
            shouldNotBeNull()
            shouldNotBeEmpty()
            size.shouldBe(111)
        }

    }
}