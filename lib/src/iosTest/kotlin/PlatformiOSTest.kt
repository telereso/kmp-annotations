package io.telereso.annotations.client

import io.telereso.annotations.client.cache.Dao
import com.squareup.sqldelight.db.SqlDriver

import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import platform.posix.SEEK_END
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.rewind
import kotlinx.cinterop.*
import platform.posix.*



actual class Resource actual constructor(actual val name: String) {
    private val file: CPointer<FILE>? = fopen("${TestUtils.RESOURCE_PATH}/$name", "r")

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
    return NativeSqliteDriver(schema, Dao.DATABASE_NAME)
}