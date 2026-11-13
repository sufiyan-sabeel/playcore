package app.playcore.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM library_games ORDER BY label COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<GameEntity>>

    @Query("SELECT * FROM library_games WHERE packageName = :packageName LIMIT 1")
    fun observeOne(packageName: String): Flow<GameEntity?>

    @Query("SELECT * FROM library_games WHERE packageName = :packageName LIMIT 1")
    suspend fun find(packageName: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GameEntity)

    @Query("DELETE FROM library_games WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("UPDATE library_games SET isFavorite = :favorite WHERE packageName = :packageName")
    suspend fun setFavorite(packageName: String, favorite: Boolean)

    @Query("UPDATE library_games SET categoryId = :categoryId WHERE packageName = :packageName")
    suspend fun setCategory(packageName: String, categoryId: Long?)

    @Query("UPDATE library_games SET lastLaunchedAt = :timestamp WHERE packageName = :packageName")
    suspend fun setLastLaunchedAt(packageName: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM library_games")
    suspend fun count(): Int
}
