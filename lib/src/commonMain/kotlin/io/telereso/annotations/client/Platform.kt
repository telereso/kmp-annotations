package io.telereso.annotations.client

import app.cash.sqldelight.db.QueryResult
import io.telereso.kmp.core.Settings
import app.cash.sqldelight.db.SqlSchema
import io.telereso.kmp.core.SqlDriverFactory


expect open class AnnotationsClientDatabaseDriverFactory : SqlDriverFactory {
    companion object {
        fun default(databaseName: String? = null): SqlDriverFactory
    }

    override fun getAsyncSchema(): SqlSchema<QueryResult.AsyncValue<Unit>>
}

val settings: Settings = Settings.get()