package app.playcore.di

import android.content.Context
import app.playcore.core.monitoring.DeviceMonitor
import app.playcore.core.permissions.CapabilityManager
import app.playcore.data.GameRepository
import app.playcore.data.InstalledAppScanner
import app.playcore.data.local.PlaycoreDatabase
import app.playcore.data.preferences.SettingsRepository

/**
 * Manual dependency container.
 *
 * Chosen over Hilt for Phase 1: it adds no annotation processor, no build-time
 * code generation and no kapt/ksp cost, and every dependency here is a singleton
 * with an obvious lifetime. If the graph grows past a few dozen nodes in a later
 * phase, swapping this for Hilt is a contained change because nothing else
 * constructs these objects.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: PlaycoreDatabase by lazy { PlaycoreDatabase.build(appContext) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }

    val capabilityManager: CapabilityManager by lazy { CapabilityManager(appContext) }

    val deviceMonitor: DeviceMonitor by lazy { DeviceMonitor(appContext) }

    val gameRepository: GameRepository by lazy {
        GameRepository(
            context = appContext,
            gameDao = database.gameDao(),
            categoryDao = database.categoryDao(),
            scanner = InstalledAppScanner(appContext),
        )
    }
}
