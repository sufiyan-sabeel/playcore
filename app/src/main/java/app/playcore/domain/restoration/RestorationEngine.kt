package app.playcore.domain.restoration

import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Applies a per-game loadout and puts the device back afterwards.
 *
 * Safety order, enforced here rather than by callers:
 *  1. is the target supported by the platform at all
 *  2. is the required permission granted
 *  3. read the original value — an unreadable value aborts the change
 *  4. persist the snapshot successfully — a failed write to storage aborts the change
 *  5. apply, then read back where the platform allows it
 *  6. record the applied value and the status
 *
 * Restoration refuses to overwrite a value that changed independently, refuses to
 * restore from an [TargetValue.Unknown] original, and is idempotent: asking twice
 * is a no-op, not a second write.
 *
 * Ownership: at most one open session owns a given target. If a newer session asks
 * for a setting an older open session already changed, the older record is marked
 * [RestoreStatus.SUPERSEDED] and the newer session inherits the **oldest** original
 * value, so restoring puts back what the user actually had rather than an
 * intermediate value from the middle of the chain. Every entry point runs under a
 * single mutex, so apply, restore and recovery can never interleave inside one
 * process.
 */
class RestorationEngine(
    private val control: SystemControlPort,
    private val store: RestorationStore,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val newSessionId: () -> String = { UUID.randomUUID().toString() },
) {

    private val mutex = Mutex()

    suspend fun startSession(
        packageName: String,
        desired: Map<SystemTarget, TargetValue>,
    ): SessionOutcome = mutex.withLock { startSessionLocked(packageName, desired) }

    private suspend fun startSessionLocked(
        packageName: String,
        desired: Map<SystemTarget, TargetValue>,
    ): SessionOutcome {
        val session = ProfileSession(
            id = newSessionId(),
            packageName = packageName,
            startedAt = clock(),
            state = SessionState.ACTIVE,
        )

        runCatching { store.openSession(session) }.onFailure {
            return SessionOutcome(
                session = null,
                records = emptyList(),
                appliedCount = 0,
                skippedCount = 0,
                failedCount = 1,
            )
        }

        val records = mutableListOf<RestorationRecord>()
        var applied = 0
        var skipped = 0
        var failed = 0

        for ((target, value) in desired.entries.sortedBy { it.key.ordinal }) {
            val record = applyOne(session, target, value)
            records += record
            when (record.applyStatus) {
                ApplyStatus.APPLIED, ApplyStatus.APPLIED_UNVERIFIED -> applied++
                ApplyStatus.FAILED_APPLY, ApplyStatus.FAILED_PERSIST -> failed++
                else -> skipped++
            }
        }

        return SessionOutcome(session, records, applied, skipped, failed)
    }

    private suspend fun applyOne(
        session: ProfileSession,
        target: SystemTarget,
        value: TargetValue,
    ): RestorationRecord {
        val now = clock()

        if (!control.isSupported(target)) {
            return persist(
                RestorationRecord(
                    sessionId = session.id,
                    target = target,
                    original = TargetValue.Unknown,
                    applied = null,
                    applyStatus = ApplyStatus.SKIPPED_UNSUPPORTED,
                    restoreStatus = RestoreStatus.NOT_REQUIRED,
                    failure = FailureInfo("UNSUPPORTED", "The platform exposes no public way to write this value."),
                    updatedAt = now,
                )
            )
        }

        if (!control.isPermitted(target)) {
            return persist(
                RestorationRecord(
                    sessionId = session.id,
                    target = target,
                    original = TargetValue.Unknown,
                    applied = null,
                    applyStatus = ApplyStatus.SKIPPED_NO_PERMISSION,
                    restoreStatus = RestoreStatus.NOT_REQUIRED,
                    failure = FailureInfo("NO_PERMISSION", "The access this setting needs has not been granted."),
                    updatedAt = now,
                )
            )
        }

        val original = control.read(target)
        if (original == null || original is TargetValue.Unknown) {
            // Never apply on top of a value we could not read: it could not be restored.
            return persist(
                RestorationRecord(
                    sessionId = session.id,
                    target = target,
                    original = TargetValue.Unknown,
                    applied = null,
                    applyStatus = ApplyStatus.SKIPPED_UNREADABLE_ORIGINAL,
                    restoreStatus = RestoreStatus.NOT_REQUIRED,
                    failure = FailureInfo("ORIGINAL_UNREADABLE", "The current value could not be read, so nothing was changed."),
                    updatedAt = now,
                )
            )
        }

        // Ownership: collapse a chain of open sessions on the same target so the
        // value restored later is the user's original, not an intermediate one.
        val effectiveOriginal = when (val ownership = takeOwnership(target, session.id)) {
            Ownership.FirstOwner -> original

            is Ownership.Inherited -> ownership.original

            Ownership.InheritedUnknown -> return persist(
                RestorationRecord(
                    sessionId = session.id,
                    target = target,
                    original = TargetValue.Unknown,
                    applied = null,
                    applyStatus = ApplyStatus.SKIPPED_UNREADABLE_ORIGINAL,
                    restoreStatus = RestoreStatus.NOT_REQUIRED,
                    failure = FailureInfo(
                        "INHERITED_ORIGINAL_UNKNOWN",
                        "An earlier session left no confirmed original for this setting, so nothing was changed.",
                    ),
                    updatedAt = now,
                )
            )

            // The supersede write failed: two sessions would own this setting, so
            // nothing may be applied until the ownership is recorded.
            Ownership.NotRecorded -> return persist(
                RestorationRecord(
                    sessionId = session.id,
                    target = target,
                    original = original,
                    applied = null,
                    applyStatus = ApplyStatus.FAILED_PERSIST,
                    restoreStatus = RestoreStatus.NOT_REQUIRED,
                    failure = FailureInfo(
                        "OWNERSHIP_NOT_RECORDED",
                        "An earlier session could not be marked superseded, so nothing was changed.",
                    ),
                    updatedAt = now,
                )
            )
        }

        // Persist the snapshot before touching the device.
        val pending = RestorationRecord(
            sessionId = session.id,
            target = target,
            original = effectiveOriginal,
            applied = null,
            applyStatus = ApplyStatus.PENDING_APPLY,
            restoreStatus = RestoreStatus.PENDING,
            updatedAt = now,
        )
        val persisted = runCatching { store.insertRecord(pending) }
        if (persisted.isFailure) {
            return pending.copy(
                applyStatus = ApplyStatus.FAILED_PERSIST,
                restoreStatus = RestoreStatus.NOT_REQUIRED,
                failure = FailureInfo(
                    "SNAPSHOT_NOT_SAVED",
                    persisted.exceptionOrNull()?.message ?: "The original value could not be stored, so nothing was changed.",
                ),
            )
        }

        val accepted = runCatching { control.write(target, value) }.getOrDefault(false)
        if (!accepted) {
            return update(
                pending.copy(
                    applyStatus = ApplyStatus.FAILED_APPLY,
                    restoreStatus = RestoreStatus.NOT_REQUIRED,
                    failure = FailureInfo("APPLY_REJECTED", "The platform refused the change."),
                    updatedAt = clock(),
                )
            )
        }

        val readBack = runCatching { control.read(target) }.getOrNull()
        val verified = readBack == value
        return update(
            pending.copy(
                applied = value,
                applyStatus = if (verified) ApplyStatus.APPLIED else ApplyStatus.APPLIED_UNVERIFIED,
                restoreStatus = RestoreStatus.PENDING,
                failure = if (verified) {
                    null
                } else {
                    FailureInfo(
                        "READBACK_MISMATCH",
                        "Applied, but the value read back does not match what was requested.",
                    )
                },
                updatedAt = clock(),
            )
        )
    }

    private sealed interface Ownership {
        data object FirstOwner : Ownership
        data class Inherited(val original: TargetValue) : Ownership
        data object InheritedUnknown : Ownership
        data object NotRecorded : Ownership
    }

    /**
     * Marks every other open record for [target] as superseded and reports the
     * oldest original value. A failed supersede is reported, not swallowed: two
     * owners would otherwise be able to restore the same setting.
     */
    private suspend fun takeOwnership(target: SystemTarget, sessionId: String): Ownership {
        val open = runCatching { store.openRecordsForTarget(target) }.getOrNull()
            ?.filter { it.sessionId != sessionId && it.needsRestore }
            ?.sortedBy { it.updatedAt }
            ?: return Ownership.NotRecorded
        if (open.isEmpty()) return Ownership.FirstOwner

        val oldest = open.first().original
        val now = clock()
        var recorded = true
        open.forEach { record ->
            val result = runCatching {
                store.updateRecord(
                    record.copy(
                        restoreStatus = RestoreStatus.SUPERSEDED,
                        failure = FailureInfo(
                            "SUPERSEDED_BY_SESSION",
                            "Session $sessionId took ownership of this setting; this session no longer restores it.",
                        ),
                        updatedAt = now,
                    )
                )
            }
            if (result.isFailure) recorded = false
        }
        if (!recorded) return Ownership.NotRecorded

        return if (oldest is TargetValue.Unknown) Ownership.InheritedUnknown else Ownership.Inherited(oldest)
    }

    suspend fun restoreSession(sessionId: String): RestoreOutcome =
        mutex.withLock { restoreSessionLocked(sessionId) }

    private suspend fun restoreSessionLocked(sessionId: String): RestoreOutcome {
        val records = store.recordsForSession(sessionId)
        val pending = records.filter { it.needsRestore }.sortedBy { it.target.ordinal }

        var restored = 0
        var skipped = 0
        var unresolved = 0
        var failed = 0
        val updated = mutableListOf<RestorationRecord>()

        for (record in pending) {
            val result = restoreOne(record)
            updated += result
            when (result.restoreStatus) {
                RestoreStatus.RESTORED, RestoreStatus.RESTORED_UNVERIFIED -> restored++
                RestoreStatus.SKIPPED_CHANGED_EXTERNALLY -> skipped++
                RestoreStatus.FAILED_RESTORE -> failed++
                RestoreStatus.UNRESOLVED -> unresolved++
                else -> Unit
            }
        }

        val outcome = RestoreOutcome(
            sessionId = sessionId,
            restoredCount = restored,
            skippedChangedCount = skipped,
            unresolvedCount = unresolved,
            failedCount = failed,
            records = updated,
        )

        // Nothing left to put back: the session is finished, even if it never
        // changed anything (a skipped or failed apply leaves no work behind).
        if (outcome.fullyResolved) {
            runCatching { store.closeSession(sessionId, clock(), SessionState.COMPLETED) }
        }

        return outcome
    }

    private suspend fun restoreOne(record: RestorationRecord): RestorationRecord {
        // A value we never read cannot be restored safely.
        if (record.original is TargetValue.Unknown) {
            return update(
                record.copy(
                    restoreStatus = RestoreStatus.UNRESOLVED,
                    failure = FailureInfo("ORIGINAL_UNKNOWN", "No confirmed original value exists, so nothing was restored."),
                    updatedAt = clock(),
                )
            )
        }

        val current = runCatching { control.read(record.target) }.getOrNull()
        if (current == null) {
            return update(
                record.copy(
                    restoreStatus = RestoreStatus.UNRESOLVED,
                    failure = FailureInfo("CURRENT_UNREADABLE", "The current value could not be read; left for inspection."),
                    updatedAt = clock(),
                )
            )
        }

        // Already back at the original: a previous attempt wrote it and died before the
        // record could be settled. Settle it instead of reporting an external change.
        if (current == record.original) {
            return update(
                record.copy(
                    restoreStatus = RestoreStatus.RESTORED,
                    failure = null,
                    updatedAt = clock(),
                )
            )
        }

        if (record.applied != null && current != record.applied) {
            // Someone else changed it since. Do not clobber their change.
            return update(
                record.copy(
                    restoreStatus = RestoreStatus.SKIPPED_CHANGED_EXTERNALLY,
                    failure = FailureInfo(
                        "CHANGED_EXTERNALLY",
                        "The value changed outside PLAYCORE, so it was left as it is.",
                    ),
                    updatedAt = clock(),
                )
            )
        }

        if (!control.isPermitted(record.target)) {
            return update(
                record.copy(
                    restoreStatus = RestoreStatus.FAILED_RESTORE,
                    failure = FailureInfo("PERMISSION_LOST", "Access was revoked before the value could be restored."),
                    updatedAt = clock(),
                )
            )
        }

        val accepted = runCatching { control.write(record.target, record.original) }.getOrDefault(false)
        if (!accepted) {
            return update(
                record.copy(
                    restoreStatus = RestoreStatus.FAILED_RESTORE,
                    failure = FailureInfo("RESTORE_REJECTED", "The platform refused to restore the original value."),
                    updatedAt = clock(),
                )
            )
        }

        val readBack = runCatching { control.read(record.target) }.getOrNull()
        val verified = readBack == record.original
        return update(
            record.copy(
                restoreStatus = if (verified) RestoreStatus.RESTORED else RestoreStatus.RESTORED_UNVERIFIED,
                failure = if (verified) {
                    null
                } else {
                    FailureInfo("RESTORE_READBACK_MISMATCH", "Restored, but the value read back does not match the original.")
                },
                updatedAt = clock(),
            )
        )
    }

    /**
     * Restores sessions left open by a crash, a kill or a reboot. A session is only
     * closed when every record reached a settled state; anything unresolved stays
     * visible instead of being discarded.
     */
    suspend fun recoverStaleSessions(staleAfterMs: Long = 0L): RecoveryOutcome {
        val now = clock()
        val recovered = mutableListOf<String>()
        val unresolved = mutableListOf<String>()

        for (session in store.activeSessions()) {
            if (now - session.startedAt < staleAfterMs) continue
            val outcome = runCatching { restoreSession(session.id) }.getOrNull()
            val after = runCatching { store.findSession(session.id) }.getOrNull()
            if (outcome != null && after?.state == SessionState.COMPLETED) {
                recovered += session.id
            } else {
                unresolved += session.id
            }
        }

        return RecoveryOutcome(recovered, unresolved)
    }

    private suspend fun persist(record: RestorationRecord): RestorationRecord {
        runCatching { store.insertRecord(record) }
        return record
    }

    private suspend fun update(record: RestorationRecord): RestorationRecord {
        runCatching { store.updateRecord(record) }
        return record
    }
}
