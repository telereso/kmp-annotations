package io.telereso.annotations.client.cache

import io.telereso.annotations.client.DatabaseDriverFactory
import io.telereso.annotations.client.cache.AnnotationsClientDatabase
import com.squareup.sqldelight.db.SqlDriver

internal class SharedDatabase(
    private val driverProvider: suspend (schema: SqlDriver.Schema, databaseDriverFactory: DatabaseDriverFactory?) -> SqlDriver,
    private val databaseDriverFactory: DatabaseDriverFactory?
) {
    private var database: AnnotationsClientDatabase? = null

    private suspend fun initDatabase() {
        if (database == null) {
            database = Dao.getDatabase(driverProvider(AnnotationsClientDatabase.Schema, databaseDriverFactory))
        }
    }

    suspend operator fun <R> invoke(block: suspend (AnnotationsClientDatabase) -> R): R {
        initDatabase()
        return block(database!!)
    }

    suspend fun <R> queries(block: suspend (AnnotationsClientDatabaseQueries) -> R): R {
        initDatabase()
        return block(database!!.annotationsClientDatabaseQueries)
    }
}
