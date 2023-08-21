package io.telereso.annotations.client

import io.telereso.annotations.client.cache.AnnotationsClientDatabase
import io.telereso.annotations.client.cache.Dao
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