package app.playcore.restoration

import app.playcore.domain.restoration.ApplyStatus
import app.playcore.domain.restoration.ProfileSession
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reliability tests for the restoration engine: ownership between sessions, crash
 * windows, recovery races and DND filter fidelity. No device, no root, no Shizuku.
 */
class RestorationHardeningTest {

    private class Control : SystemControlPort {
        val values = linkedMapOf<SystemTarget, TargetValue>()
        val supported = mutableSetOf(SystemTarget.DO_NOT_DISTURB, SystemTarget.BRIGHTNESS)
        val permitted = mutableSetOf(SystemTarget.DO_NOT_DISTURB, SystemTarget.BRIGHTNESS)
        val unreadable = mutableSetOf<SystemTarget>()
        val rejectWrites = mutableSetOf<SystemTarget>()
        var writes = 0

        override fun isSupported(target: SystemTarget) = target in supported
        override fun isPermitted(target: SystemTarget) = target in permitted
        override fun read(target: SystemTarget): TargetValue? =
            if (target in unreadable) null else values[target]

        override fun write(target: SystemTarget, value: TargetValue): Boolean {
            writes++
            if (target in rejectWrites) return false
            values[target] = value
            return true
        }
    }

    private class Store : RestorationStore {
        val sessions = linkedMapOf<String, ProfileSession>()
        val records = linkedMapOf<Pair<String, SystemTarget>, RestorationRecord>()
        var failInserts = false
        /** Simulates a process dying right after the platform write, before the record is saved. */
        var failUpdatesAfterFirstApply = false
        /** Simulates a process dying in the middle of a restore. */
        var failUpdatesDuringRestore = false
        private var updates = 0

        override suspend fun openSession(session: ProfileSession) { sessions[session.id] = session }

        override suspend fun closeSession(sessionId: String, endedAt: Long, state: SessionState) {
            sessions[sessionId]?.let { sessions[sessionId] = it.copy(endedAt = endedAt, state = state) }
        }

        override suspend fun insertRecord(record: RestorationRecord) {
            if (failInserts) error("disk full")
            records[record.sessionId to record.target] = record
        }

        override suspend fun updateRecord(record: RestorationRecord) {
            updates++
            if (failUpdatesAfterFirstApply && record.applied != null) error("process died")
            if (failUpdatesDuringRestore &&
                record.restoreStatus == RestoreStatus.RESTORED
            ) {
                error("process died during restore")
            }
            records[record.sessionId to record.target] = record
        }

        override suspend fun recordsForSession(sessionId: String): List<RestorationRecord> =
            records.values.filter { it.sessionId == sessionId }

        override suspend fun activeSessions(): List<ProfileSession> =
            sessions.values.filter { it.state == SessionState.ACTIVE }

        override suspend fun findSession(sessionId: String): ProfileSession? = sessions[sessionId]

        override suspend fun openRecordsForTarget(target: SystemTarget): List<RestorationRecord> {
            val active = sessions.values.filter { it.state == SessionState.ACTIVE }.map { it.id }.toSet()
            return records.values
                .filter { it.target == target && it.sessionId in active && it.needsRestore }
                .sortedBy { it.updatedAt }
        }

        fun recordsFor(target: SystemTarget): List<RestorationRecord> =
            records.values.filter { it.target == target }.sortedBy { it.updatedAt }
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
    }

    private val dnd = SystemTarget.DO_NOT_DISTURB
    private val priority = TargetValue.IntValue(2)

    // 1. Ownership: two sessions on the same target collapse to the true original.
    @Test
    fun `two sessions on one target restore the original, not an intermediate value`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1) // ALL
        val engine = b.engine()

        val first = engine.startSession("com.one", mapOf(dnd to priority))
        b.now = 2_000L
        val second = engine.startSession("com.two", mapOf(dnd to TargetValue.IntValue(3)))

        assertEquals(TargetValue.IntValue(1), second.records.single().original)
        assertEquals(RestoreStatus.SUPERSEDED, b.store.recordsForSession(first.session!!.id).single().restoreStatus)

        engine.restoreSession(first.session.id) // superseded: must not write
        assertEquals(TargetValue.IntValue(3), b.control.values[dnd])

        val restored = engine.restoreSession(second.session!!.id)
        assertEquals(1, restored.restoredCount)
        assertEquals(TargetValue.IntValue(1), b.control.values[dnd])
    }

    // 2. A new session may start while an older one is unresolved.
    @Test
    fun `a new session may start while an older session is unresolved`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        val engine = b.engine()

        val first = engine.startSession("com.one", mapOf(dnd to priority))
        b.control.unreadable += dnd
        val stuck = engine.restoreSession(first.session!!.id)
        assertEquals(1, stuck.unresolvedCount)

        b.control.unreadable.clear()
        b.now = 3_000L
        val second = engine.startSession("com.two", mapOf(dnd to TargetValue.IntValue(3)))
        assertEquals(TargetValue.IntValue(1), second.records.single().original)

        engine.restoreSession(second.session!!.id)
        assertEquals(TargetValue.IntValue(1), b.control.values[dnd])
    }

    // 3. Concurrent starts are serialised and never duplicate ownership.
    @Test
    fun `concurrent session starts leave exactly one owner per target`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        val engine = b.engine()

        val results = coroutineScope {
            val a = async { engine.startSession("com.a", mapOf(dnd to priority)) }
            val c = async { engine.startSession("com.b", mapOf(dnd to TargetValue.IntValue(3))) }
            listOf(a.await(), c.await())
        }

        val owning = b.store.recordsFor(dnd).filter { it.needsRestore }
        assertEquals(1, owning.size)
        assertEquals(TargetValue.IntValue(1), owning.single().original)

        // Whichever session owns it, restoring every session returns the original.
        results.forEach { engine.restoreSession(it.session!!.id) }
        assertEquals(TargetValue.IntValue(1), b.control.values[dnd])
    }

    // 4. Recovery is idempotent.
    @Test
    fun `duplicate recovery performs no second write`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        val engine = b.engine()
        engine.startSession("com.one", mapOf(dnd to priority))
        val writesAfterApply = b.control.writes

        b.now = 900_000L
        val firstRecovery = engine.recoverStaleSessions(staleAfterMs = 60_000L)
        assertEquals(1, firstRecovery.recoveredSessions.size)
        val writesAfterRecovery = b.control.writes

        val secondRecovery = engine.recoverStaleSessions(staleAfterMs = 60_000L)
        assertTrue(secondRecovery.recoveredSessions.isEmpty())
        assertEquals(writesAfterRecovery, b.control.writes)
        assertEquals(writesAfterApply + 1, writesAfterRecovery)
    }

    // 5. Crash between the platform write and persisting the applied value.
    @Test
    fun `crash between write and persistence is recovered to the original`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        b.store.failUpdatesAfterFirstApply = true

        val outcome = b.engine().startSession("com.one", mapOf(dnd to priority))
        assertEquals(TargetValue.IntValue(2), b.control.values[dnd])
        val stuck = b.store.recordsForSession(outcome.session!!.id).single()
        assertEquals(RestoreStatus.PENDING, stuck.restoreStatus)
        assertEquals(null, stuck.applied)

        b.store.failUpdatesAfterFirstApply = false
        val recovery = b.engine().recoverStaleSessions(staleAfterMs = 0L)
        assertEquals(1, recovery.recoveredSessions.size)
        assertEquals(TargetValue.IntValue(1), b.control.values[dnd])
    }

    // 6. Crash during restoration stays retryable.
    @Test
    fun `crash during restoration leaves the record retryable`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        val engine = b.engine()
        val outcome = engine.startSession("com.one", mapOf(dnd to priority))

        b.store.failUpdatesDuringRestore = true
        engine.restoreSession(outcome.session!!.id)
        // The write landed, but the record could not be settled.
        val pending = b.store.recordsForSession(outcome.session!!.id).single()
        assertTrue(pending.needsRestore)

        b.store.failUpdatesDuringRestore = false
        val retry = engine.restoreSession(outcome.session!!.id)
        assertEquals(1, retry.restoredCount)
        assertEquals(TargetValue.IntValue(1), b.control.values[dnd])
    }

    // 7. Permission revoked between apply and restore.
    @Test
    fun `permission revocation during restore is recorded and the session stays open`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        val engine = b.engine()
        val outcome = engine.startSession("com.one", mapOf(dnd to priority))

        b.control.permitted.clear()
        val restore = engine.restoreSession(outcome.session!!.id)

        assertEquals(1, restore.failedCount)
        assertEquals("PERMISSION_LOST", restore.records.single().failure?.code)
        assertEquals(TargetValue.IntValue(2), b.control.values[dnd])
        assertEquals(SessionState.ACTIVE, b.store.sessions.getValue(outcome.session.id).state)

        b.control.permitted += dnd
        val retry = engine.restoreSession(outcome.session.id)
        assertEquals(1, retry.restoredCount)
        assertEquals(TargetValue.IntValue(1), b.control.values[dnd])
    }

    // 8. Recovery racing with a fresh application never loses the original.
    @Test
    fun `recovery racing with a new application keeps the original value`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        val engine = b.engine()
        engine.startSession("com.one", mapOf(dnd to priority))

        b.now = 900_000L
        coroutineScope {
            val recovery = async { engine.recoverStaleSessions(staleAfterMs = 60_000L) }
            val fresh = async { engine.startSession("com.two", mapOf(dnd to TargetValue.IntValue(3))) }
            recovery.await()
            fresh.await()
        }

        val owning = b.store.recordsFor(dnd).filter { it.needsRestore }
        assertTrue(owning.size <= 1)
        owning.forEach { engine.restoreSession(it.sessionId) }
        assertEquals(TargetValue.IntValue(1), b.control.values[dnd])
    }

    // 9. DND filters are captured and restored exactly, not as a boolean.
    @Test
    fun `every interruption filter is preserved exactly`() = runTest {
        for (original in listOf(1, 2, 3, 4)) {
            val b = Bench()
            b.control.values[dnd] = TargetValue.IntValue(original)
            val engine = b.engine()

            val outcome = engine.startSession("com.one", mapOf(dnd to priority))
            assertEquals(TargetValue.IntValue(original), outcome.records.single().original)

            engine.restoreSession(outcome.session!!.id)
            assertEquals(
                "filter $original must come back unchanged",
                TargetValue.IntValue(original),
                b.control.values[dnd],
            )
        }
    }

    // 10. A user change made during the session is never overwritten.
    @Test
    fun `user change to the interruption filter is left alone`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        val engine = b.engine()
        val outcome = engine.startSession("com.one", mapOf(dnd to priority))

        b.control.values[dnd] = TargetValue.IntValue(4) // user switched to alarms only
        val restore = engine.restoreSession(outcome.session!!.id)

        assertEquals(1, restore.skippedChangedCount)
        assertEquals(TargetValue.IntValue(4), b.control.values[dnd])
        assertEquals(SessionState.COMPLETED, b.store.sessions.getValue(outcome.session.id).state)
    }

    // 11. Storage failure still aborts before the platform is touched.
    @Test
    fun `persistence failure never reaches the device`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        b.store.failInserts = true

        val outcome = b.engine().startSession("com.one", mapOf(dnd to priority))
        assertEquals(ApplyStatus.FAILED_PERSIST, outcome.records.single().applyStatus)
        assertEquals(0, b.control.writes)
        assertEquals(TargetValue.IntValue(1), b.control.values[dnd])
    }

    // 12. A superseded record is never restored by a later recovery.
    @Test
    fun `superseded records are ignored by recovery`() = runTest {
        val b = Bench()
        b.control.values[dnd] = TargetValue.IntValue(1)
        val engine = b.engine()
        val first = engine.startSession("com.one", mapOf(dnd to priority))
        b.now = 2_000L
        engine.startSession("com.two", mapOf(dnd to TargetValue.IntValue(3)))

        b.now = 900_000L
        engine.recoverStaleSessions(staleAfterMs = 60_000L)
        assertFalse(b.store.recordsForSession(first.session!!.id).single().needsRestore)
    }
}
