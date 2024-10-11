package io.telereso.annotations.client.cache

import io.telereso.kmp.core.SqlDriverFactory

internal class SharedDatabase constructor(
    private val databaseDriverFactory: SqlDriverFactory
) {
    private var database: AnnotationsClientDatabase? = null

    private suspend fun initDatabase() {
        if (database == null) {
            database = Dao.getDatabase(databaseDriverFactory.createDriver())
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
