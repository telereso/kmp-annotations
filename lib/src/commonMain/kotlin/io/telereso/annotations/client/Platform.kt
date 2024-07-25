package io.telereso.annotations.client

import io.telereso.kmp.core.Settings
import com.squareup.sqldelight.db.SqlDriver


expect suspend fun provideDbDriver(
    schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?
): SqlDriver

/**
 * an abstract factory for database drivers.
 * SQLDelight provides multiple platform-specific implementations of the SQLite driver, so you need to create them for each platform separately.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

val settings: Settings = Settings.get()