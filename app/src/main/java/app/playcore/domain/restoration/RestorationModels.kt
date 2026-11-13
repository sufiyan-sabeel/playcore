package app.playcore.domain.restoration

/**
 * A system setting PLAYCORE may change for a game session. Typed rather than a
 * free-form string so a typo cannot silently become a new "setting".
 */
enum class SystemTarget(val key: String) {
    DO_NOT_DISTURB("do_not_disturb"),
    BRIGHTNESS("brightness"),
    REFRESH_RATE("refresh_rate"),
    ;

    companion object {
        fun fromKey(key: String): SystemTarget? = entries.firstOrNull { it.key == key }
    }
}

/**
 * A setting value. [Unknown] is a first-class state: it means "read failed or the
 * platform refused", and it is never treated as a confirmed original.
 */
sealed interface TargetValue {
    data class BooleanValue(val value: Boolean) : TargetValue
    data class IntValue(val value: Int) : TargetValue
    data class TextValue(val value: String) : TargetValue
    data object Unknown : TargetValue
}

/** What happened when the profile value was applied. */
enum class ApplyStatus {
    PENDING_APPLY,
    APPLIED,
    APPLIED_UNVERIFIED,
    SKIPPED_NO_PERMISSION,
    SKIPPED_UNSUPPORTED,
    SKIPPED_UNREADABLE_ORIGINAL,
    FAILED_PERSIST,
    FAILED_APPLY,
}

/** What happened when the original value was put back. */
enum class RestoreStatus {
    NOT_REQUIRED,
    PENDING,
    RESTORED,
    RESTORED_UNVERIFIED,
    SKIPPED_CHANGED_EXTERNALLY,
    FAILED_RESTORE,
    UNRESOLVED,
}

enum class SessionState { ACTIVE, COMPLETED }

/** Failure information that survives a restart, for inspection. */
data class FailureInfo(val code: String, val detail: String)

/** One game session that may have changed system settings. */
data class ProfileSession(
    val id: String,
    val packageName: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val state: SessionState = SessionState.ACTIVE,
)

/**
 * One setting touched by one session: what it was, what it became, and what still
 * has to be put back.
 */
data class RestorationRecord(
    val sessionId: String,
    val target: SystemTarget,
    val original: TargetValue,
    val applied: TargetValue?,
    val applyStatus: ApplyStatus,
    val restoreStatus: RestoreStatus,
    val failure: FailureInfo? = null,
    val updatedAt: Long = 0L,
) {
    /** True when this record still holds a value that must be put back. */
    val needsRestore: Boolean
        get() = restoreStatus == RestoreStatus.PENDING
}

data class SessionOutcome(
    val session: ProfileSession?,
    val records: List<RestorationRecord>,
    val appliedCount: Int,
    val skippedCount: Int,
    val failedCount: Int,
)

data class RestoreOutcome(
    val sessionId: String,
    val restoredCount: Int,
    val skippedChangedCount: Int,
    val unresolvedCount: Int,
    val failedCount: Int,
    val records: List<RestorationRecord>,
) {
    val fullyResolved: Boolean
        get() = unresolvedCount == 0 && failedCount == 0
}

data class RecoveryOutcome(
    val recoveredSessions: List<String>,
    val stillUnresolved: List<String>,
)
