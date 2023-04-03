package io.telereso.annotations.client.cache

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import io.telereso.annotations.models.RocketLaunch
import io.telereso.annotations.models.fromJson
import io.telereso.annotations.models.toJson
import io.telereso.kmp.core.CommonFlow
import io.telereso.kmp.core.asCommonFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map


/**
 * This class's visibility is set to internal, which means it is only accessible from within the multiplatform module.
 */
internal class Dao(val database: SharedDatabase) {

    companion object {
        const val DATABASE_NAME = "annotations-client.db"

        // TODO Remove sample
        val rocketLaunchAdapter = object : ColumnAdapter<RocketLaunch, String> {
            override fun decode(databaseValue: String): RocketLaunch {
                return if (databaseValue.isEmpty()) {
                    RocketLaunch()
                } else {
                    RocketLaunch.fromJson(databaseValue)
                }
            }

            override fun encode(value: RocketLaunch): String {
                return value.toJson()
            }
        }

        fun getDatabase(databaseDriver: SqlDriver): AnnotationsClientDatabase {

            return AnnotationsClientDatabase(
                databaseDriver,
                RocketLaunchT.Adapter(rocketLaunchAdapter)
            )
        }
    }

    /**
     * Add a function to clear all the tables in the database in a single SQL transaction
     */
    internal suspend fun clearDatabase() {
        database.queries {
            it.transaction {
                it.removeAllRocketLaunch()
            }
        }
    }

    // TODO Remove sample
    internal suspend fun getAllRocketLaunches(): List<RocketLaunch> {
        return database.queries {
            it.selectAllRocketLaunch().executeAsList().map { r ->
                r.rocketLaunch!!
            }
        }
    }

    private fun mapper(id: Long, rocketLaunch: RocketLaunch?): RocketLaunch {
        return rocketLaunch!!
    }

    internal suspend fun getRocketLaunchesFlow(): CommonFlow<List<RocketLaunch>> {
        return database.queries {
            it.selectAllRocketLaunch(::mapper).asFlow().mapToList().asCommonFlow()
        }
    }

    internal suspend fun getFirstRocketLaunches(): CommonFlow<RocketLaunch?> {
        return database.queries {
            it.selectFirstRocketLaunch(::mapper).asFlow().mapToOne().asCommonFlow()
        }
    }

    // TODO Remove sample
    internal suspend fun insertRocketLaunches(launches: List<RocketLaunch>) {
        database.queries {
            it.transaction {
                launches.forEachIndexed { index, rocketLaunch ->
                    it.insertRocketLaunch(index.toLong(), rocketLaunch)
                }
            }
        }
    }
}