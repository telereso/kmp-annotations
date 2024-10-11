package io.telereso.annotations.client

import android.content.Context
import io.telereso.annotations.client.cache.Dao
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlSchema
import io.telereso.annotations.client.cache.AnnotationsClientDatabase
import io.telereso.kmp.core.SqlDriverFactory


actual open class AnnotationsClientDatabaseDriverFactory(
    context: Context?,
    databaseName: String? = null
) :
    SqlDriverFactory(databaseName ?: Dao.DATABASE_NAME, context) {
    actual companion object {
        actual fun default(databaseName: String?): SqlDriverFactory {
            return AnnotationsClientDatabaseDriverFactory(null, databaseName)
        }
    }

    actual override fun getAsyncSchema() = AnnotationsClientDatabase.Schema
    override fun getSchema(): SqlSchema<QueryResult.Value<Unit>>? = AnnotationsClientDatabase.Schema.synchronous()
}