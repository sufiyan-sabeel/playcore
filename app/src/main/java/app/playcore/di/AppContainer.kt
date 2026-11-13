package app.playcore.di

import android.content.Context
import app.playcore.core.monitoring.DeviceMonitor
import app.playcore.core.permissions.CapabilityManager
import app.playcore.data.GameRepository
import app.playcore.data.InstalledAppScanner
import app.playcore.data.local.PlaycoreDatabase
import app.playcore.data.preferences.SettingsRepository
import app.playcore.overlay.OverlayController

/**
 * Manual dependency container.
 *
 * Chosen over Hilt for Phase 1: no annotation processor, no generated code, and
 * every dependency is a singleton with an obvious lifetime. Swapping it later is
 * contained because nothing else constructs these objects.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: PlaycoreDatabase by lazy { PlaycoreDatabase.build(appContext) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }

    val capabilityManager: CapabilityManager by lazy { CapabilityManager(appContext) }

    val deviceMonitor: DeviceMonitor by lazy { DeviceMonitor(appContext) }

    val overlayController: OverlayController by lazy { OverlayController(appContext) }

    val gameRepository: GameRepository by lazy {
        GameRepository(
            context = appContext,
            gameDao = database.gameDao(),
            categoryDao = database.categoryDao(),
            profileDao = database.gameProfileDao(),
            scanner = InstalledAppScanner(appContext),
        )
    }
}
