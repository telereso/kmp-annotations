package io.telereso.annotations.client

import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.SqlPreparedStatement
import com.squareup.sqldelight.drivers.sqljs.initSqlDriver
import kotlinx.coroutines.await


actual interface Parcelable

actual suspend fun provideDbDriver(
    schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?
): SqlDriver {

    return initSqlDriver(schema).await()
}

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // Dumpy object, for js will be using provideDbDriver(SqlDriver.Schema)
        return object : SqlDriver {
            override fun close() {

            }

            override fun currentTransaction(): Transacter.Transaction? {
                TODO("Not yet implemented")
            }

            override fun execute(
                identifier: Int?,
                sql: String,
                parameters: Int,
                binders: (SqlPreparedStatement.() -> Unit)?
            ) {

            }

            override fun executeQuery(
                identifier: Int?,
                sql: String,
                parameters: Int,
                binders: (SqlPreparedStatement.() -> Unit)?
            ): SqlCursor {
                TODO("Not yet implemented")
            }

            override fun newTransaction(): Transacter.Transaction {
                TODO("Not yet implemented")
            }
        }
    }
}