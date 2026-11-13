package app.playcore.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.playcore.core.model.CubeCard
import app.playcore.core.model.CubeMode
import app.playcore.core.model.Game
import app.playcore.core.model.GameCategory
import app.playcore.core.model.GameProfile
import app.playcore.core.model.OverlayStyle

@Entity(tableName = "library_games")
data class GameEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false,
    val categoryId: Long? = null,
    val lastLaunchedAt: Long? = null,
    val addedAt: Long = 0L,
    @ColumnInfo(defaultValue = "0") val totalPlayMs: Long = 0L,
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
)

/**
 * Per-game profile. Cards are stored as a comma-separated list of enum names so
 * a schema change in the enum does not need a migration.
 */
@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey val packageName: String,
    val overlayStyle: String,
    val cubeMode: String,
    val enabledCards: String,
    val dndEnabled: Boolean,
    val brightnessLock: Boolean,
    val refreshRateHz: Int?,
    val cpuMode: String?,
    val notes: String,
    val updatedAt: Long,
)

fun GameEntity.toModel(isInstalled: Boolean = true): Game = Game(
    packageName = packageName,
    label = label,
    isFavorite = isFavorite,
    categoryId = categoryId,
    lastLaunchedAt = lastLaunchedAt,
    addedAt = addedAt,
    totalPlayMs = totalPlayMs,
    isInstalled = isInstalled,
)

fun CategoryEntity.toModel(): GameCategory = GameCategory(id = id, name = name)

fun GameProfileEntity.toModel(): GameProfile = GameProfile(
    packageName = packageName,
    overlayStyle = runCatching { OverlayStyle.valueOf(overlayStyle) }.getOrDefault(OverlayStyle.ENERGY_CUBE),
    cubeMode = runCatching { CubeMode.valueOf(cubeMode) }.getOrDefault(CubeMode.BALANCE),
    enabledCards = enabledCards.split(",")
        .mapNotNull { runCatching { CubeCard.valueOf(it) }.getOrNull() }
        .toSet(),
    dndEnabled = dndEnabled,
    brightnessLock = brightnessLock,
    refreshRateHz = refreshRateHz,
    cpuMode = cpuMode,
    notes = notes,
    updatedAt = updatedAt,
)

fun GameProfile.toEntity(): GameProfileEntity = GameProfileEntity(
    packageName = packageName,
    overlayStyle = overlayStyle.name,
    cubeMode = cubeMode.name,
    enabledCards = enabledCards.joinToString(",") { it.name },
    dndEnabled = dndEnabled,
    brightnessLock = brightnessLock,
    refreshRateHz = refreshRateHz,
    cpuMode = cpuMode,
    notes = notes,
    updatedAt = updatedAt,
)
