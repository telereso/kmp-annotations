package io.telereso.annotations.client

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
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