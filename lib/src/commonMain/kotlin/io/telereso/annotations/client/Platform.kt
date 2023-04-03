package io.telereso.annotations.client

import io.telereso.kmp.core.Settings
import com.squareup.sqldelight.db.SqlDriver


// For Android Parcelable
/**
 * By declaring an empty expected interface in our common sourceset, we will be able to write classes that implement Parcelable.
 */
expect interface Parcelable

/**
 * // For Android Parcelize
 * @OptionalExpectation is not supported for interfaces take note
 * @OptionalExpectation helps us make actual implementations optional on target platform.
 * Here is an example where we expect and actual Parcelize for Android Platforms in this case
 * a Parcelize annotation but the rest like iOS it will be optional.
 * this way the IDE wont warn we missed an actual.
 */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class CommonParcelize()

expect suspend fun provideDbDriver(
    schema: SqlDriver.Schema,
    databaseDriverFactory: DatabaseDriverFactory?
): SqlDriver

/**
 * an abstract factory for database drivers.
 * SQLDelight provides multiple platform-specific implementations of the SQLite driver, so you need to create them for each platform separately.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

val settings: Settings = Settings.get()