package app.playcore.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.playcore.core.model.Game
import app.playcore.core.model.GameCategory

@Entity(tableName = "library_games")
data class GameEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false,
    val categoryId: Long? = null,
    val lastLaunchedAt: Long? = null,
    val addedAt: Long = 0L,
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
)

fun GameEntity.toModel(isInstalled: Boolean = true): Game = Game(
    packageName = packageName,
    label = label,
    isFavorite = isFavorite,
    categoryId = categoryId,
    lastLaunchedAt = lastLaunchedAt,
    addedAt = addedAt,
    isInstalled = isInstalled,
)

fun CategoryEntity.toModel(): GameCategory = GameCategory(id = id, name = name)
