package io.telereso.annotations.client

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File


actual class Resource actual constructor(actual val name: String) {
    private val file = File("${TestUtils.RESOURCE_PATH}/$name")

    actual fun exists(): Boolean = file.exists()

    actual fun readText(): String = file.readText()
}

actual suspend fun provideDbDriverTest(schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?): SqlDriver {
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        schema.create(this)
    }
}