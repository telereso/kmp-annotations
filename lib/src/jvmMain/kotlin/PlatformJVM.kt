package io.telereso.annotations.client

import com.squareup.sqldelight.db.SqlDriver


actual class DatabaseDriverFactory() {
    actual fun createDriver(): SqlDriver {
        TODO("Not yet implemented")
    }
}

actual suspend fun provideDbDriver(
    schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?
): SqlDriver {
    TODO("Not yet implemented")
}