package app.playcore.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.playcore.domain.restoration.ApplyStatus
import app.playcore.domain.restoration.FailureInfo
import app.playcore.domain.restoration.ProfileSession
import app.playcore.domain.restoration.RestorationRecord
import app.playcore.domain.restoration.RestoreStatus
import app.playcore.domain.restoration.SessionState
import app.playcore.domain.restoration.SystemTarget
import app.playcore.domain.restoration.TargetValue

@Entity(tableName = "profile_sessions")
data class ProfileSessionEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val startedAt: Long,
    val endedAt: Long?,
    val state: String,
)

@Entity(
    tableName = "restoration_records",
    primaryKeys = ["sessionId", "target"],
    indices = [Index(value = ["sessionId"])],
)
data class RestorationRecordEntity(
    val sessionId: String,
    val target: String,
    val originalKind: String,
    val originalValue: String?,
    val appliedKind: String?,
    val appliedValue: String?,
    val applyStatus: String,
    val restoreStatus: String,
    val failureCode: String?,
    val failureDetail: String?,
    val updatedAt: Long,
)

/**
 * Values are stored with an explicit kind tag rather than as free-form strings, so a
 * boolean can never come back as the text "true" and be written as a different type.
 */
object TargetValueCodec {
    const val BOOLEAN = "BOOLEAN"
    const val INT = "INT"
    const val TEXT = "TEXT"
    const val UNKNOWN = "UNKNOWN"

    fun encode(value: TargetValue): Pair<String, String?> = when (value) {
        is TargetValue.BooleanValue -> BOOLEAN to value.value.toString()
        is TargetValue.IntValue -> INT to value.value.toString()
        is TargetValue.TextValue -> TEXT to value.value
        TargetValue.Unknown -> UNKNOWN to null
    }

    fun decode(kind: String?, raw: String?): TargetValue = when (kind) {
        BOOLEAN -> raw?.toBooleanStrictOrNull()?.let { TargetValue.BooleanValue(it) } ?: TargetValue.Unknown
        INT -> raw?.toIntOrNull()?.let { TargetValue.IntValue(it) } ?: TargetValue.Unknown
        TEXT -> raw?.let { TargetValue.TextValue(it) } ?: TargetValue.Unknown
        else -> TargetValue.Unknown
    }
}

fun ProfileSessionEntity.toModel(): ProfileSession = ProfileSession(
    id = id,
    packageName = packageName,
    startedAt = startedAt,
    endedAt = endedAt,
    state = runCatching { SessionState.valueOf(state) }.getOrDefault(SessionState.ACTIVE),
)

fun ProfileSession.toEntity(): ProfileSessionEntity = ProfileSessionEntity(
    id = id,
    packageName = packageName,
    startedAt = startedAt,
    endedAt = endedAt,
    state = state.name,
)

fun RestorationRecord.toEntity(): RestorationRecordEntity {
    val (originalKind, originalValue) = TargetValueCodec.encode(original)
    val appliedEncoded = applied?.let { TargetValueCodec.encode(it) }
    return RestorationRecordEntity(
        sessionId = sessionId,
        target = target.key,
        originalKind = originalKind,
        originalValue = originalValue,
        appliedKind = appliedEncoded?.first,
        appliedValue = appliedEncoded?.second,
        applyStatus = applyStatus.name,
        restoreStatus = restoreStatus.name,
        failureCode = failure?.code,
        failureDetail = failure?.detail,
        updatedAt = updatedAt,
    )
}

fun RestorationRecordEntity.toModel(): RestorationRecord = RestorationRecord(
    sessionId = sessionId,
    target = SystemTarget.fromKey(target) ?: SystemTarget.DO_NOT_DISTURB,
    original = TargetValueCodec.decode(originalKind, originalValue),
    applied = if (appliedKind == null) null else TargetValueCodec.decode(appliedKind, appliedValue),
    applyStatus = runCatching { ApplyStatus.valueOf(applyStatus) }.getOrDefault(ApplyStatus.FAILED_APPLY),
    restoreStatus = runCatching { RestoreStatus.valueOf(restoreStatus) }.getOrDefault(RestoreStatus.UNRESOLVED),
    failure = if (failureCode == null) null else FailureInfo(failureCode, failureDetail.orEmpty()),
    updatedAt = updatedAt,
)
