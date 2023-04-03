package io.telereso.annotations.client

import com.squareup.sqldelight.db.SqlDriver
import io.telereso.annotations.client.DatabaseDriverFactory

import kotlin.test.BeforeTest

/**
 * To run the test on KOver run in gradle this command
 *./gradlew :sdk:koverHtmlReport
 * RTunning this will oinlyt run tests for the sdk variant rather
 * than runing for all debug release and sdk
it will gneerate an html repoirt with test coverage
 */
class TestUtils {
    @BeforeTest
    fun before() {
    }

    companion object{
        const val RESOURCE_PATH = "./src/commonTest/resources"
    }
}

expect suspend fun provideDbDriverTest(
    schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?
): SqlDriver

expect class Resource(name: String) {
    val name: String

    fun exists(): Boolean
    fun readText(): String
}
