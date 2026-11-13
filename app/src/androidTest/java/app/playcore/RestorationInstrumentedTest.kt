package app.playcore

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.playcore.data.local.DatabaseMigrations
import app.playcore.data.local.MigrationSql
import app.playcore.data.local.PlaycoreDatabase
import app.playcore.data.local.ProfileSessionEntity
import app.playcore.data.local.RoomRestorationStore
import app.playcore.domain.restoration.ApplyStatus
import app.playcore.domain.restoration.RestorationEngine
import app.playcore.domain.restoration.SessionState
import app.playcore.domain.restoration.SystemControlPort
import app.playcore.domain.restoration.SystemTarget
import app.playcore.domain.restoration.TargetValue
import app.playcore.system.AndroidSystemControlAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device tests. These prove what the JVM tests cannot: Room's own migration path,
 * the real notification-policy adapter, and persistence across a fresh engine instance.
 */
@RunWith(AndroidJUnit4::class)
class RestorationInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "restoration-instrumented.db"

    @Before
    fun setUp() {
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    /** Builds a real v2 database file, then opens it with the v3 Room definition. */
    @Test
    fun roomMigrationFromV2PreservesLibraryData() = runBlocking {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        MigrationSql.V2_FIXTURE.forEach { db.execSQL(it) }
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        helper.writableDatabase.use { db ->
            db.execSQL(
                "INSERT INTO library_games (packageName, label, isFavorite, categoryId, lastLaunchedAt, addedAt, totalPlayMs) " +
                    "VALUES ('com.test.game', 'Test Game', 1, NULL, 10, 5, 60000)"
            )
            db.execSQL(
                "INSERT INTO game_profiles (packageName, overlayStyle, cubeMode, enabledCards, dndEnabled, " +
                    "brightnessLock, refreshRateHz, cpuMode, notes, updatedAt) " +
                    "VALUES ('com.test.game', 'ENERGY_CUBE', 'BALANCE', 'CROSSHAIR', 1, 0, NULL, NULL, 'kept', 7)"
            )
        }
        helper.close()

        val database = Room.databaseBuilder(context, PlaycoreDatabase::class.java, dbName)
            .addMigrations(*DatabaseMigrations.ALL)
            .build()

        assertEquals(1, database.gameDao().count())
        val game = database.gameDao().find("com.test.game")
        assertNotNull(game)
        assertEquals("Test Game", game?.label)
        assertEquals(60000L, game?.totalPlayMs ?: -1L)
        assertEquals("kept", database.gameProfileDao().observeAll().first().single().notes)

        // The new tables are usable after the migration.
        database.restorationDao().insertSession(
            ProfileSessionEntity(id = "s1", packageName = "com.test.game", startedAt = 1L, endedAt = null, state = "ACTIVE")
        )
        assertNotNull(database.restorationDao().findSession("s1"))
        database.close()
    }

    @Test
    fun dndAdapterReportsSupportAndCapturesTheFilter() {
        val adapter = AndroidSystemControlAdapter(context)
        assertTrue(adapter.isSupported(SystemTarget.DO_NOT_DISTURB))
        val filter = adapter.read(SystemTarget.DO_NOT_DISTURB)
        assertTrue("the current interruption filter must be readable", filter is TargetValue.IntValue)
    }

    @Test
    fun dndWriteIsRefusedWithoutPolicyAccess() {
        val adapter = AndroidSystemControlAdapter(context)
        if (adapter.isPermitted(SystemTarget.DO_NOT_DISTURB)) {
            // Access already granted on this device: nothing to assert about refusal.
            return
        }
        val before = adapter.read(SystemTarget.DO_NOT_DISTURB)
        val accepted = adapter.write(SystemTarget.DO_NOT_DISTURB, TargetValue.IntValue(2))
        assertFalse("a write without policy access must not report success", accepted)
        assertEquals(before, adapter.read(SystemTarget.DO_NOT_DISTURB))
    }

    @Test
    fun refreshRateIsReportedUnsupported() {
        val adapter = AndroidSystemControlAdapter(context)
        assertFalse(adapter.isSupported(SystemTarget.REFRESH_RATE))
        assertFalse(adapter.write(SystemTarget.REFRESH_RATE, TargetValue.TextValue("120")))
    }

    @Test
    fun engineOverRealRoomStoreSurvivesAFreshInstance() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, PlaycoreDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val store = RoomRestorationStore(database.restorationDao())
        val control = FakeControl().apply { values[SystemTarget.DO_NOT_DISTURB] = TargetValue.IntValue(1) }

        val first = RestorationEngine(control = control, store = store)
        val session = first.startSession("com.test.game", mapOf(SystemTarget.DO_NOT_DISTURB to TargetValue.IntValue(2)))
        assertEquals(TargetValue.IntValue(2), control.values[SystemTarget.DO_NOT_DISTURB])

        // A brand-new engine over the same persisted store: the "restart" path.
        val second = RestorationEngine(control = control, store = store)
        val recovery = second.recoverStaleSessions(staleAfterMs = 0L)

        assertEquals(1, recovery.recoveredSessions.size)
        assertEquals(TargetValue.IntValue(1), control.values[SystemTarget.DO_NOT_DISTURB])
        assertEquals(SessionState.COMPLETED, store.findSession(session.session!!.id)?.state)
        database.close()
    }

    @Test
    fun engineRecordsPermissionSkipsOnTheRealAdapter() = runBlocking {
        val adapter = AndroidSystemControlAdapter(context)
        val database = Room.inMemoryDatabaseBuilder(context, PlaycoreDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val store = RoomRestorationStore(database.restorationDao())
        val engine = RestorationEngine(control = adapter, store = store)

        val outcome = engine.startSession(
            "com.test.game",
            mapOf(SystemTarget.REFRESH_RATE to TargetValue.TextValue("120")),
        )
        assertEquals(ApplyStatus.SKIPPED_UNSUPPORTED, outcome.records.single().applyStatus)
        assertEquals(0, outcome.appliedCount)
        database.close()
    }

    private class FakeControl : SystemControlPort {
        val values = linkedMapOf<SystemTarget, TargetValue>()
        override fun isSupported(target: SystemTarget) = true
        override fun isPermitted(target: SystemTarget) = true
        override fun read(target: SystemTarget): TargetValue? = values[target]
        override fun write(target: SystemTarget, value: TargetValue): Boolean {
            values[target] = value
            return true
        }
    }
}
