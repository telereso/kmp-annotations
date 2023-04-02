package io.telereso.annotations.client.cache

import io.telereso.annotations.models.RocketLaunch
import io.telereso.annotations.client.cache.Dao
import io.telereso.annotations.client.cache.SharedDatabase
import io.telereso.annotations.client.provideDbDriverTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class DaoTest {

    private val dao: Dao = Dao(SharedDatabase(::provideDbDriverTest, null))

    @BeforeTest
    fun before() {

    }

    @AfterTest
    fun after() {

    }

    @Test
    fun shouldInsertRockets() = runTest {
        dao.database.queries {
            it.insertRocketLaunch(2, RocketLaunch(mission_name = "Mars and Beyond"))
            it.selectAllRocketLaunch().executeAsList().shouldNotBeEmpty()
        }
    }

    @Test
    fun shouldSelectAllRockets() = runTest {
        dao.database.queries {
            it.insertRocketLaunch(2, RocketLaunch(mission_name = "Mars and Beyond"))
            it.insertRocketLaunch(5, RocketLaunch(mission_name = "Venus and Back"))
            with(it.selectAllRocketLaunch().executeAsList()) {
                shouldNotBeEmpty()
                size.shouldBe(2)
                first().rocketLaunch?.mission_name.shouldBe("Mars and Beyond")
            }
        }
    }

    @Test
    fun shouldClearDatabase() = runTest {
        dao.database.queries {
            with(it) {
                insertRocketLaunch(5, RocketLaunch(mission_name = "Mission Impossible"))
                removeAllRocketLaunch()
                selectAllRocketLaunch().executeAsList().shouldBeEmpty()
            }
        }
    }
}