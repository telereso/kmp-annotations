package io.telereso.annotations.client

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlSchema
import io.telereso.annotations.client.cache.AnnotationsClientDatabase
import io.telereso.annotations.client.cache.Dao
import io.telereso.kmp.core.SqlDriverFactory


actual open class AnnotationsClientDatabaseDriverFactory(databaseName: String? = null) :
    SqlDriverFactory(databaseName ?: Dao.DATABASE_NAME) {
    actual companion object {
        actual fun default(databaseName: String?): SqlDriverFactory {
            return AnnotationsClientDatabaseDriverFactory(databaseName)
        }
    }

    actual override fun getAsyncSchema() = AnnotationsClientDatabase.Schema
    override fun getSchema(): SqlSchema<QueryResult.Value<Unit>>? = AnnotationsClientDatabase.Schema.synchronous()
}