package app.playcore.domain.restoration

/**
 * The only way the restoration engine touches the platform. Keeping it an
 * interface is what makes the whole state machine testable without a device,
 * root or Shizuku.
 */
interface SystemControlPort {
    /** False when the platform has no public way to write this target at all. */
    fun isSupported(target: SystemTarget): Boolean

    /** False when the user has not granted the access this target needs. */
    fun isPermitted(target: SystemTarget): Boolean

    /** Current value, or null when it could not be read. Never invents a value. */
    fun read(target: SystemTarget): TargetValue?

    /** True only when the platform accepted the write. */
    fun write(target: SystemTarget, value: TargetValue): Boolean
}

/** Persistence port, so the engine can be tested against an in-memory store. */
interface RestorationStore {
    suspend fun openSession(session: ProfileSession)

    suspend fun closeSession(sessionId: String, endedAt: Long, state: SessionState)

    suspend fun insertRecord(record: RestorationRecord)

    suspend fun updateRecord(record: RestorationRecord)

    suspend fun recordsForSession(sessionId: String): List<RestorationRecord>

    suspend fun activeSessions(): List<ProfileSession>

    suspend fun findSession(sessionId: String): ProfileSession?
}
