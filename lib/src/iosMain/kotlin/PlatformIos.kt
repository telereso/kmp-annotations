package io.telereso.annotations.client

import io.telereso.annotations.client.cache.AnnotationsClientDatabase
import io.telereso.annotations.client.cache.Dao
import platform.UIKit.UIDevice
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import kotlinx.serialization.json.Json
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver


actual interface Parcelable

actual suspend fun provideDbDriver(
    schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?
): SqlDriver {
    return NativeSqliteDriver(schema, Dao.DATABASE_NAME)
}

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(AnnotationsClientDatabase.Schema, Dao.DATABASE_NAME)
    }
}