package app.playcore.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RestorationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(entity: ProfileSessionEntity)

    @Query("UPDATE profile_sessions SET endedAt = :endedAt, state = :state WHERE id = :sessionId")
    suspend fun closeSession(sessionId: String, endedAt: Long, state: String)

    @Query("SELECT * FROM profile_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun findSession(sessionId: String): ProfileSessionEntity?

    @Query("SELECT * FROM profile_sessions WHERE state = 'ACTIVE' ORDER BY startedAt ASC")
    suspend fun activeSessions(): List<ProfileSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(entity: RestorationRecordEntity)

    @Query("SELECT * FROM restoration_records WHERE sessionId = :sessionId ORDER BY target ASC")
    suspend fun recordsForSession(sessionId: String): List<RestorationRecordEntity>

    @Query(
        """
        SELECT r.* FROM restoration_records r
        INNER JOIN profile_sessions s ON s.id = r.sessionId
        WHERE r.target = :target
          AND s.state = 'ACTIVE'
          AND r.restoreStatus IN ('PENDING', 'FAILED_RESTORE', 'UNRESOLVED')
        ORDER BY r.updatedAt ASC
        """
    )
    suspend fun openRecordsForTarget(target: String): List<RestorationRecordEntity>

    @Query("SELECT * FROM restoration_records ORDER BY updatedAt DESC")
    suspend fun allRecords(): List<RestorationRecordEntity>
}
