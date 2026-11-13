package app.playcore.domain.restoration

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Orders startup recovery before any profile application.
 *
 * The engine serialises calls inside itself, but that does not stop a user tapping
 * "Apply loadout" while the startup recovery is still running. This coordinator
 * makes recovery a one-shot barrier: it runs once, and every `startSession` waits
 * for it, so a new application can never overtake a pending restore.
 */
class RestorationCoordinator(private val engine: RestorationEngine) {

    private val gate = Mutex()
    private val recoveryResult = CompletableDeferred<RecoveryOutcome>()
    private var recoveryStarted = false

    /** Runs recovery exactly once. Safe to call from the Application and the UI. */
    suspend fun recoverAtStartup(staleAfterMs: Long = 0L): RecoveryOutcome {
        gate.withLock {
            if (!recoveryStarted) {
                recoveryStarted = true
                val outcome = runCatching { engine.recoverStaleSessions(staleAfterMs) }
                    .getOrElse { RecoveryOutcome(emptyList(), emptyList()) }
                recoveryResult.complete(outcome)
            }
        }
        return recoveryResult.await()
    }

    /** Applies a loadout only after startup recovery has finished. */
    suspend fun startSession(
        packageName: String,
        desired: Map<SystemTarget, TargetValue>,
    ): SessionOutcome {
        if (recoveryStarted) recoveryResult.await() else recoverAtStartup()
        return engine.startSession(packageName, desired)
    }

    suspend fun restoreSession(sessionId: String): RestoreOutcome = engine.restoreSession(sessionId)

    suspend fun recoverStaleSessions(staleAfterMs: Long): RecoveryOutcome =
        engine.recoverStaleSessions(staleAfterMs)
}
