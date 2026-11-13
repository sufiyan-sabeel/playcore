package app.playcore.restoration

import app.playcore.domain.restoration.ApplyStatus
import app.playcore.domain.restoration.ProfileSession
import app.playcore.domain.restoration.RestorationCoordinator
import app.playcore.domain.restoration.RestorationEngine
import app.playcore.domain.restoration.RestorationRecord
import app.playcore.domain.restoration.RestorationStore
import app.playcore.domain.restoration.RestoreStatus
import app.playcore.domain.restoration.SessionState
import app.playcore.domain.restoration.SystemControlPort
import app.playcore.domain.restoration.SystemTarget
import app.playcore.domain.restoration.TargetValue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Acceptance tests for SUPERSEDED ownership and startup ordering. Every scenario in the
 * acceptance brief is represented here, in JVM form.
 */
class RestorationAcceptanceTest {

    private class Control : SystemControlPort {
        val values = linkedMapOf<SystemTarget, TargetValue>()
        val permitted = mutableSetOf(SystemTarget.DO_NOT_DISTURB)
        val unreadable = mutableSetOf<SystemTarget>()
        val events = mutableListOf<String>()
        var writes = 0

        override fun isSupported(target: SystemTarget) = true
        override fun isPermitted(target: SystemTarget) = target in permitted
        override fun read(target: SystemTarget): TargetValue? =
            if (target in unreadable) null else values[target]

        override fun write(target: SystemTarget, value: TargetValue): Boolean {
            writes++
            events += "write:$target=$value"
            values[target] = value
            return true
        }
    }

    private class Store : RestorationStore {
        val sessions = linkedMapOf<String, ProfileSession>()
        val records = linkedMapOf<Pair<String, SystemTarget>, RestorationRecord>()
        var failSupersede = false
        var activeSessionsCalls = 0

        override suspend fun openSession(session: ProfileSession) { sessions[session.id] = session }

        override suspend fun closeSession(sessionId: String, endedAt: Long, state: SessionState) {
            sessions[sessionId]?.let { sessions[sessionId] = it.copy(endedAt = endedAt, state = state) }
        }

        override suspend fun insertRecord(record: RestorationRecord) {
            records[record.sessionId to record.target] = record
        }

        override suspend fun updateRecord(record: RestorationRecord) {
            if (failSupersede && record.restoreStatus == RestoreStatus.SUPERSEDED) error("write failed")
            records[record.sessionId to record.target] = record
        }

        override suspend fun recordsForSession(sessionId: String): List<RestorationRecord> =
            records.values.filter { it.sessionId == sessionId }

        override suspend fun activeSessions(): List<ProfileSession> {
            activeSessionsCalls++
            return sessions.values.filter { it.state == SessionState.ACTIVE }
        }

        override suspend fun findSession(sessionId: String): ProfileSession? = sessions[sessionId]

        override suspend fun openRecordsForTarget(target: SystemTarget): List<RestorationRecord> {
            val active = sessions.values.filter { it.state == SessionState.ACTIVE }.map { it.id }.toSet()
            return records.values
                .filter { it.target == target && it.sessionId in active && it.needsRestore }
                .sortedBy { it.updatedAt }
        }

        fun record(sessionId: String, target: SystemTarget) = records[sessionId to target]
        fun owners(target: SystemTarget) = records.values.filter { it.target == target && it.needsRestore }
    }

    private class Bench {
        val control = Control()
        val store = Store()
        var now = 1_000L
        private var n = 0
        fun engine() = RestorationEngine(
            control = control,
            store = store,
            clock = { now },
            newSessionId = { "s${++n}" },
        )
        fun coordinator(engine: RestorationEngine) = RestorationCoordinator(engine)
    }

    private val dnd = SystemTarget.DO_NOT_DISTURB
    private val original = TargetValue.IntValue(1)
    private val a2 = TargetValue.IntValue(2)
    private val b3 = TargetValue.IntValue(3)

    @Test
    fun `game A starts before game B and B inherits the oldest original`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        val a = engine.startSession("com.a", mapOf(dnd to a2))
        b.now = 2_000L
        val c = engine.startSession("com.b", mapOf(dnd to b3))

        assertEquals(original, c.records.single().original)
        assertEquals(RestoreStatus.SUPERSEDED, b.store.record(a.session!!.id, dnd)?.restoreStatus)
        engine.restoreSession(c.session!!.id)
        assertEquals(original, b.control.values[dnd])
    }

    @Test
    fun `game A ends before game B and A restores nothing`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        val a = engine.startSession("com.a", mapOf(dnd to a2))
        b.now = 2_000L
        val c = engine.startSession("com.b", mapOf(dnd to b3))

        val writesBefore = b.control.writes
        val aOutcome = engine.restoreSession(a.session!!.id)
        assertEquals(0, aOutcome.restoredCount)
        assertEquals(writesBefore, b.control.writes)
        assertEquals(b3, b.control.values[dnd])

        engine.restoreSession(c.session!!.id)
        assertEquals(original, b.control.values[dnd])
    }

    @Test
    fun `game B ends before game A and A still restores nothing`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        val a = engine.startSession("com.a", mapOf(dnd to a2))
        b.now = 2_000L
        val c = engine.startSession("com.b", mapOf(dnd to b3))

        engine.restoreSession(c.session!!.id)
        assertEquals(original, b.control.values[dnd])

        val writesBefore = b.control.writes
        val aOutcome = engine.restoreSession(a.session!!.id)
        assertEquals(0, aOutcome.restoredCount)
        assertEquals(writesBefore, b.control.writes)
        assertEquals(original, b.control.values[dnd])
    }

    @Test
    fun `both sessions end unexpectedly and recovery restores the original once`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        engine.startSession("com.a", mapOf(dnd to a2))
        b.now = 2_000L
        engine.startSession("com.b", mapOf(dnd to b3))

        b.now = 900_000L
        val recovery = engine.recoverStaleSessions(staleAfterMs = 60_000L)
        assertEquals(1, recovery.recoveredSessions.size)
        assertTrue(recovery.stillUnresolved.isEmpty())
        assertEquals(original, b.control.values[dnd])
        assertEquals(1, b.control.writes - 2) // two applies, exactly one restore
    }

    @Test
    fun `crash during restoration of a superseded chain still lands on the original`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        engine.startSession("com.a", mapOf(dnd to a2))
        b.now = 2_000L
        val c = engine.startSession("com.b", mapOf(dnd to b3))

        b.control.unreadable += dnd
        val stuck = engine.restoreSession(c.session!!.id)
        assertEquals(1, stuck.unresolvedCount)
        b.control.unreadable.clear()

        b.now = 900_000L
        engine.recoverStaleSessions(staleAfterMs = 60_000L)
        assertEquals(original, b.control.values[dnd])
    }

    @Test
    fun `new session starts while an older session is unresolved`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        val a = engine.startSession("com.a", mapOf(dnd to a2))
        b.control.unreadable += dnd
        engine.restoreSession(a.session!!.id)
        b.control.unreadable.clear()

        b.now = 3_000L
        val c = engine.startSession("com.b", mapOf(dnd to b3))
        assertEquals(original, c.records.single().original)
        assertEquals(RestoreStatus.SUPERSEDED, b.store.record(a.session!!.id, dnd)?.restoreStatus)
        engine.restoreSession(c.session!!.id)
        assertEquals(original, b.control.values[dnd])
    }

    @Test
    fun `four sessions on one target leave exactly one owner and the oldest original`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        val sessions = (1..4).map { index ->
            b.now = 1_000L * index
            engine.startSession("com.$index", mapOf(dnd to TargetValue.IntValue(index + 1)))
        }

        val owners = b.store.owners(dnd)
        assertEquals(1, owners.size)
        assertEquals(original, owners.single().original)
        assertEquals(sessions.last().session!!.id, owners.single().sessionId)

        sessions.forEach { engine.restoreSession(it.session!!.id) }
        assertEquals(original, b.control.values[dnd])
    }

    @Test
    fun `a failed supersede write prevents the apply`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        engine.startSession("com.a", mapOf(dnd to a2))

        b.store.failSupersede = true
        b.now = 2_000L
        val writesBefore = b.control.writes
        val outcome = engine.startSession("com.b", mapOf(dnd to b3))

        assertEquals(ApplyStatus.FAILED_PERSIST, outcome.records.single().applyStatus)
        assertEquals("OWNERSHIP_NOT_RECORDED", outcome.records.single().failure?.code)
        assertEquals(writesBefore, b.control.writes)
        assertEquals(a2, b.control.values[dnd])
    }

    @Test
    fun `coordinator runs startup recovery before applying a profile`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        engine.startSession("com.stale", mapOf(dnd to a2)) // left open by a "crash"

        b.now = 900_000L
        val coordinator = b.coordinator(engine)
        val outcome = coordinator.startSession("com.new", mapOf(dnd to b3))

        // If recovery had not run first, the new session would have inherited 2.
        assertEquals(original, outcome.records.single().original)
        coordinator.restoreSession(outcome.session!!.id)
        assertEquals(original, b.control.values[dnd])
    }

    @Test
    fun `coordinator performs one recovery pass for concurrent callers`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        val coordinator = b.coordinator(engine)

        coroutineScope {
            val first = async { coordinator.startSession("com.a", mapOf(dnd to a2)) }
            val second = async { coordinator.startSession("com.b", mapOf(dnd to b3)) }
            first.await(); second.await()
        }

        assertEquals(1, b.store.activeSessionsCalls)
        assertEquals(1, b.store.owners(dnd).size)
        assertEquals(original, b.store.owners(dnd).single().original)
    }

    @Test
    fun `recovery never restores a superseded record`() = runTest {
        val b = Bench(); b.control.values[dnd] = original
        val engine = b.engine()
        val a = engine.startSession("com.a", mapOf(dnd to a2))
        b.now = 2_000L
        val c = engine.startSession("com.b", mapOf(dnd to b3))

        b.now = 900_000L
        engine.recoverStaleSessions(staleAfterMs = 60_000L)

        val superseded = b.store.record(a.session!!.id, dnd)
        assertEquals(RestoreStatus.SUPERSEDED, superseded?.restoreStatus)
        assertEquals(false, superseded?.needsRestore)
        assertEquals(RestoreStatus.RESTORED, b.store.record(c.session!!.id, dnd)?.restoreStatus)
    }
}
