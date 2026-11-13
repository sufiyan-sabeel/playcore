package app.playcore.data

import android.content.Context
import android.content.Intent
import app.playcore.core.model.Game
import app.playcore.core.model.GameCategory
import app.playcore.core.model.GameProfile
import app.playcore.core.model.InstalledApp
import app.playcore.core.model.LaunchResult
import app.playcore.data.local.CategoryDao
import app.playcore.data.local.CategoryEntity
import app.playcore.data.local.GameDao
import app.playcore.data.local.GameEntity
import app.playcore.data.local.GameProfileDao
import app.playcore.data.local.toEntity
import app.playcore.data.local.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The library and the per-game profiles. Room is the source of truth; the package
 * manager is consulted only for discovery and for launching.
 */
class GameRepository(
    private val context: Context,
    private val gameDao: GameDao,
    private val categoryDao: CategoryDao,
    private val profileDao: GameProfileDao,
    private val scanner: InstalledAppScanner,
) {

    private val installedPackages = MutableStateFlow<Set<String>?>(null)

    fun observeLibrary(): Flow<List<Game>> =
        combine(gameDao.observeAll(), installedPackages) { entities, installed ->
            entities.map { entity ->
                entity.toModel(isInstalled = installed == null || installed.contains(entity.packageName))
            }
        }

    fun observeGame(packageName: String): Flow<Game?> =
        gameDao.observeOne(packageName).map { it?.toModel() }

    fun observeCategories(): Flow<List<GameCategory>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toModel() } }

    /** A stored profile, or the documented defaults when the game has none yet. */
    fun observeProfile(packageName: String): Flow<GameProfile> =
        profileDao.observeOne(packageName).map { it?.toModel() ?: GameProfile(packageName = packageName) }

    fun observeAllProfiles(): Flow<List<GameProfile>> =
        profileDao.observeAll().map { rows -> rows.map { it.toModel() } }

    suspend fun saveProfile(profile: GameProfile) =
        profileDao.upsert(profile.copy(updatedAt = System.currentTimeMillis()).toEntity())

    suspend fun refreshInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val apps = scanner.scan()
        installedPackages.value = apps.map { it.packageName }.toSet()
        apps
    }

    suspend fun addToLibrary(app: InstalledApp, categoryId: Long? = null) {
        gameDao.upsert(
            GameEntity(
                packageName = app.packageName,
                label = app.label,
                categoryId = categoryId,
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun removeFromLibrary(packageName: String) {
        profileDao.delete(packageName)
        gameDao.delete(packageName)
    }

    suspend fun toggleFavorite(packageName: String) {
        val current = gameDao.find(packageName) ?: return
        gameDao.setFavorite(packageName, !current.isFavorite)
    }

    suspend fun setFavorite(packageName: String, favorite: Boolean) =
        gameDao.setFavorite(packageName, favorite)

    suspend fun setCategory(packageName: String, categoryId: Long?) =
        gameDao.setCategory(packageName, categoryId)

    suspend fun createCategory(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return -1L
        return categoryDao.insert(CategoryEntity(name = trimmed))
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.clearCategoryFromGames(id)
        categoryDao.delete(id)
    }

    suspend fun isInLibrary(packageName: String): Boolean = gameDao.find(packageName) != null

    suspend fun addPlaytime(packageName: String, deltaMs: Long) {
        if (deltaMs > 0L) gameDao.addPlaytime(packageName, deltaMs)
    }

    suspend fun launch(packageName: String): LaunchResult {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
            ?: return if (isPackageInstalled(packageName)) {
                LaunchResult.NoLaunchIntent
            } else {
                LaunchResult.NotInstalled
            }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val launched = runCatching { context.startActivity(launchIntent) }.isSuccess
        if (!launched) return LaunchResult.NoLaunchIntent
        gameDao.setLastLaunchedAt(packageName, System.currentTimeMillis())
        return LaunchResult.Launched
    }

    private fun isPackageInstalled(packageName: String): Boolean = runCatching {
        context.packageManager.getPackageInfo(packageName, 0)
    }.isSuccess
}
