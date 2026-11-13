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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Domain tests for the restoration engine. No Android, no root, no Shizuku, no device:
 * every platform call goes through a fake port and every write goes through an
 * in-memory store.
 */
class RestorationEngineTest {

    private class FakeSystemControl : SystemControlPort {
        val values = linkedMapOf<SystemTarget, TargetValue>()
        val supported = mutableSetOf(SystemTarget.DO_NOT_DISTURB, SystemTarget.BRIGHTNESS)
        val permitted = mutableSetOf(SystemTarget.DO_NOT_DISTURB, SystemTarget.BRIGHTNESS)
        val rejectedWrites = mutableSetOf<SystemTarget>()
        val unreadable = mutableSetOf<SystemTarget>()
        val coerced = mutableMapOf<SystemTarget, TargetValue>()
        var writeAttempts = 0

        override fun isSupported(target: SystemTarget) = target in supported
        override fun isPermitted(target: SystemTarget) = target in permitted
        override fun read(target: SystemTarget): TargetValue? =
            if (target in unreadable) null else values[target]

        override fun write(target: SystemTarget, value: TargetValue): Boolean {
            writeAttempts++
            if (target in rejectedWrites) return false
            values[target] = coerced[target] ?: value
            return true
        }
    }

    private class InMemoryRestorationStore : RestorationStore {
        val sessions = linkedMapOf<String, ProfileSession>()
        val records = linkedMapOf<Pair<String, SystemTarget>, RestorationRecord>()
        var failOpen = false
        var failInsert = false

        override suspend fun openSession(session: ProfileSession) {
            if (failOpen) error("database unavailable")
            sessions[session.id] = session
        }

        override suspend fun closeSession(sessionId: String, endedAt: Long, state: SessionState) {
            sessions[sessionId]?.let { sessions[sessionId] = it.copy(endedAt = endedAt, state = state) }
        }

        override suspend fun insertRecord(record: RestorationRecord) {
            if (failInsert) error("disk full")
            records[record.sessionId to record.target] = record
        }

        override suspend fun updateRecord(record: RestorationRecord) {
            records[record.sessionId to record.target] = record
        }

        override suspend fun recordsForSession(sessionId: String): List<RestorationRecord> =
            records.values.filter { it.sessionId == sessionId }

        override suspend fun activeSessions(): List<ProfileSession> =
            sessions.values.filter { it.state == SessionState.ACTIVE }

        override suspend fun findSession(sessionId: String): ProfileSession? = sessions[sessionId]
    }

    private class Harness {
        val control = FakeSystemControl()
        val store = InMemoryRestorationStore()
        var now = 1_000L
        private var counter = 0
        fun engine() = RestorationEngine(
            control = control,
            store = store,
            clock = { now },
            newSessionId = { "session-${++counter}" },
        )
    }

    private val dndOn = mapOf(SystemTarget.DO_NOT_DISTURB to TargetValue.IntValue(2))
    private val dndOriginal = TargetValue.IntValue(1)

    @Test
    fun `applies a change and restores the original`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal

        val outcome = h.engine().startSession("com.game", dndOn)
        assertEquals(1, outcome.appliedCount)
        assertEquals(0, outcome.failedCount)
        assertEquals(TargetValue.IntValue(2), h.control.values[SystemTarget.DO_NOT_DISTURB])

        val record = outcome.records.single()
        assertEquals(dndOriginal, record.original)
        assertEquals(ApplyStatus.APPLIED, record.applyStatus)
        assertEquals(RestoreStatus.PENDING, record.restoreStatus)

        val restore = h.engine().restoreSession(outcome.session!!.id)
        assertEquals(1, restore.restoredCount)
        assertTrue(restore.fullyResolved)
        assertEquals(dndOriginal, h.control.values[SystemTarget.DO_NOT_DISTURB])
        assertEquals(SessionState.COMPLETED, h.store.sessions.getValue(outcome.session.id).state)
    }

    @Test
    fun `apply failure leaves nothing to restore`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        h.control.rejectedWrites += SystemTarget.DO_NOT_DISTURB

        val outcome = h.engine().startSession("com.game", dndOn)
        assertEquals(0, outcome.appliedCount)
        assertEquals(1, outcome.failedCount)
        val record = outcome.records.single()
        assertEquals(ApplyStatus.FAILED_APPLY, record.applyStatus)
        assertEquals(RestoreStatus.NOT_REQUIRED, record.restoreStatus)
        assertEquals("APPLY_REJECTED", record.failure?.code)
        assertEquals(dndOriginal, h.control.values[SystemTarget.DO_NOT_DISTURB])

        val restore = h.engine().restoreSession(outcome.session!!.id)
        assertEquals(0, restore.restoredCount)
        assertTrue(restore.fullyResolved)
    }

    @Test
    fun `restoration failure keeps the session open for a retry`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        val outcome = h.engine().startSession("com.game", dndOn)

        h.control.rejectedWrites += SystemTarget.DO_NOT_DISTURB
        val restore = h.engine().restoreSession(outcome.session!!.id)
        assertEquals(1, restore.failedCount)
        assertEquals("RESTORE_REJECTED", restore.records.single().failure?.code)
        assertEquals(SessionState.ACTIVE, h.store.sessions.getValue(outcome.session.id).state)

        h.control.rejectedWrites.clear()
        val retry = h.engine().restoreSession(outcome.session.id)
        assertEquals(1, retry.restoredCount)
        assertEquals(dndOriginal, h.control.values[SystemTarget.DO_NOT_DISTURB])
    }

    @Test
    fun `missing permission skips without touching the device`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        h.control.permitted.clear()

        val outcome = h.engine().startSession("com.game", dndOn)
        assertEquals(0, outcome.appliedCount)
        assertEquals(1, outcome.skippedCount)
        assertEquals(ApplyStatus.SKIPPED_NO_PERMISSION, outcome.records.single().applyStatus)
        assertEquals(0, h.control.writeAttempts)
        assertEquals(dndOriginal, h.control.values[SystemTarget.DO_NOT_DISTURB])
    }

    @Test
    fun `unsupported target is recorded and never written`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.REFRESH_RATE] = TargetValue.TextValue("60.0")
        val outcome = h.engine().startSession(
            "com.game",
            mapOf(SystemTarget.REFRESH_RATE to TargetValue.TextValue("120")),
        )
        assertEquals(ApplyStatus.SKIPPED_UNSUPPORTED, outcome.records.single().applyStatus)
        assertEquals(0, h.control.writeAttempts)
        assertEquals(TargetValue.TextValue("60.0"), h.control.values[SystemTarget.REFRESH_RATE])
    }

    @Test
    fun `storage failure aborts before the device is touched`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        h.store.failInsert = true

        val outcome = h.engine().startSession("com.game", dndOn)
        val record = outcome.records.single()
        assertEquals(ApplyStatus.FAILED_PERSIST, record.applyStatus)
        assertEquals("SNAPSHOT_NOT_SAVED", record.failure?.code)
        assertEquals(0, h.control.writeAttempts)
        assertEquals(dndOriginal, h.control.values[SystemTarget.DO_NOT_DISTURB])
    }

    @Test
    fun `unreadable original is never treated as confirmed`() = runTest {
        val h = Harness()
        h.control.unreadable += SystemTarget.DO_NOT_DISTURB

        val outcome = h.engine().startSession("com.game", dndOn)
        val record = outcome.records.single()
        assertEquals(ApplyStatus.SKIPPED_UNREADABLE_ORIGINAL, record.applyStatus)
        assertEquals(TargetValue.Unknown, record.original)
        assertEquals(0, h.control.writeAttempts)

        val restore = h.engine().restoreSession(outcome.session!!.id)
        assertEquals(0, restore.restoredCount)
        assertTrue(restore.fullyResolved)
    }

    @Test
    fun `repeated restoration is a no-op`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        val outcome = h.engine().startSession("com.game", dndOn)
        val first = h.engine().restoreSession(outcome.session!!.id)
        assertEquals(1, first.restoredCount)
        val writesAfterFirst = h.control.writeAttempts

        val second = h.engine().restoreSession(outcome.session.id)
        assertEquals(0, second.restoredCount)
        assertEquals(0, second.unresolvedCount)
        assertEquals(writesAfterFirst, h.control.writeAttempts)
        assertEquals(dndOriginal, h.control.values[SystemTarget.DO_NOT_DISTURB])
    }

    @Test
    fun `a value changed outside PLAYCORE is not overwritten`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        val outcome = h.engine().startSession("com.game", dndOn)

        h.control.values[SystemTarget.DO_NOT_DISTURB] = TargetValue.IntValue(5)
        val restore = h.engine().restoreSession(outcome.session!!.id)

        assertEquals(1, restore.skippedChangedCount)
        assertEquals(RestoreStatus.SKIPPED_CHANGED_EXTERNALLY, restore.records.single().restoreStatus)
        assertEquals(TargetValue.IntValue(5), h.control.values[SystemTarget.DO_NOT_DISTURB])
    }

    @Test
    fun `applied but unverified is recorded honestly`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        h.control.coerced[SystemTarget.DO_NOT_DISTURB] = TargetValue.IntValue(3)

        val outcome = h.engine().startSession("com.game", dndOn)
        val record = outcome.records.single()
        assertEquals(ApplyStatus.APPLIED_UNVERIFIED, record.applyStatus)
        assertEquals("READBACK_MISMATCH", record.failure?.code)
        assertEquals(TargetValue.IntValue(2), record.applied)
    }

    @Test
    fun `sessions are isolated from each other`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        h.control.values[SystemTarget.BRIGHTNESS] = TargetValue.IntValue(120)

        val first = h.engine().startSession("com.game.one", dndOn)
        val second = h.engine().startSession(
            "com.game.two",
            mapOf(SystemTarget.BRIGHTNESS to TargetValue.IntValue(40)),
        )

        h.engine().restoreSession(first.session!!.id)
        assertEquals(dndOriginal, h.control.values[SystemTarget.DO_NOT_DISTURB])
        assertEquals(TargetValue.IntValue(40), h.control.values[SystemTarget.BRIGHTNESS])
        assertEquals(SessionState.ACTIVE, h.store.sessions.getValue(second.session!!.id).state)

        h.engine().restoreSession(second.session.id)
        assertEquals(TargetValue.IntValue(120), h.control.values[SystemTarget.BRIGHTNESS])
    }

    @Test
    fun `stale session recovery after a process restart`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        val started = h.engine().startSession("com.game", dndOn)
        assertNotNull(started.session)

        // Process death: a brand-new engine over the same store, later in time.
        h.now = 900_000L
        val recovery = h.engine().recoverStaleSessions(staleAfterMs = 60_000L)

        assertEquals(listOf(started.session!!.id), recovery.recoveredSessions)
        assertTrue(recovery.stillUnresolved.isEmpty())
        assertEquals(dndOriginal, h.control.values[SystemTarget.DO_NOT_DISTURB])
        assertEquals(SessionState.COMPLETED, h.store.sessions.getValue(started.session.id).state)
    }

    @Test
    fun `recovery keeps unresolved sessions visible instead of discarding them`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        val started = h.engine().startSession("com.game", dndOn)

        h.control.unreadable += SystemTarget.DO_NOT_DISTURB
        val recovery = h.engine().recoverStaleSessions(staleAfterMs = 0L)

        assertEquals(listOf(started.session!!.id), recovery.stillUnresolved)
        assertEquals(SessionState.ACTIVE, h.store.sessions.getValue(started.session.id).state)
        val record = h.store.recordsForSession(started.session.id).single()
        assertEquals(RestoreStatus.UNRESOLVED, record.restoreStatus)
        assertEquals("CURRENT_UNREADABLE", record.failure?.code)
    }

    @Test
    fun `session open failure is reported and writes nothing`() = runTest {
        val h = Harness()
        h.control.values[SystemTarget.DO_NOT_DISTURB] = dndOriginal
        h.store.failOpen = true

        val outcome = h.engine().startSession("com.game", dndOn)
        assertNull(outcome.session)
        assertEquals(1, outcome.failedCount)
        assertEquals(0, h.control.writeAttempts)
    }
}
