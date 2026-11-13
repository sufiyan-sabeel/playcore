package app.playcore.data.local

import app.playcore.domain.restoration.ProfileSession
import app.playcore.domain.restoration.RestorationRecord
import app.playcore.domain.restoration.RestorationStore
import app.playcore.domain.restoration.SessionState
import app.playcore.domain.restoration.SystemTarget

/** Room-backed implementation of the engine's persistence port. */
class RoomRestorationStore(private val dao: RestorationDao) : RestorationStore {

    override suspend fun openSession(session: ProfileSession) =
        dao.insertSession(session.toEntity())

    override suspend fun closeSession(sessionId: String, endedAt: Long, state: SessionState) =
        dao.closeSession(sessionId, endedAt, state.name)

    override suspend fun insertRecord(record: RestorationRecord) =
        dao.upsertRecord(record.toEntity())

    override suspend fun updateRecord(record: RestorationRecord) =
        dao.upsertRecord(record.toEntity())

    override suspend fun recordsForSession(sessionId: String): List<RestorationRecord> =
        dao.recordsForSession(sessionId).map { it.toModel() }

    override suspend fun activeSessions(): List<ProfileSession> =
        dao.activeSessions().map { it.toModel() }

    override suspend fun findSession(sessionId: String): ProfileSession? =
        dao.findSession(sessionId)?.toModel()

    override suspend fun openRecordsForTarget(target: SystemTarget): List<RestorationRecord> =
        dao.openRecordsForTarget(target.key).map { it.toModel() }
}
