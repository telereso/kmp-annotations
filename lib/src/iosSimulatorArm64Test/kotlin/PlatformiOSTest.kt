package io.telereso.annotations.client

import io.telereso.annotations.client.cache.Dao
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.cinterop.*
import platform.posix.*
import co.touchlab.sqliter.DatabaseConfiguration

import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection
import platform.Foundation.NSBundle

private var dbIndex = 0

@OptIn(ExperimentalForeignApi::class)
actual class Resource actual constructor(actual val name: String) {
    val pathParts = name.split("[.|/]".toRegex())
    val path = NSBundle.mainBundle.pathForResource("resources/${pathParts[0]}", pathParts[1])
    private val file: CPointer<FILE>? = fopen(path, "r")
    actual fun exists(): Boolean = file != null

    actual fun readText(): String {
        fseek(file, 0, SEEK_END)
        val size = ftell(file)
        rewind(file)

        return memScoped {
            val tmp = allocArray<ByteVar>(size)
            fread(tmp, sizeOf<ByteVar>().convert(), size.convert(), file)
            tmp.toKString()
        }
    }
}

actual suspend fun provideDbDriverTest(schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?): SqlDriver {
    return NativeSqliteDriver(
        DatabaseConfiguration(
            name = "${++dbIndex}-${Dao.DATABASE_NAME}",
            version = schema.version,
            create = { connection ->
                wrapConnection(connection) { schema.create(it) }
            },
            upgrade = { connection, oldVersion, newVersion ->
                wrapConnection(connection) {
                    schema.migrate(it, oldVersion, newVersion)
                }
            },
            inMemory = true
        )
    )
}