package io.telereso.annotations.client

import android.content.Context
import io.telereso.annotations.client.cache.AnnotationsClientDatabase
import io.telereso.annotations.client.cache.Dao
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual suspend fun provideDbDriver(
    schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?
): SqlDriver {
    return databaseDriverFactory!!.createDriver()
}

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(AnnotationsClientDatabase.Schema, context, Dao.DATABASE_NAME)
    }
}