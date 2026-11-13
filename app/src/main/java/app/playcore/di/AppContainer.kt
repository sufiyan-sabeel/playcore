package app.playcore.di

import android.content.Context
import app.playcore.core.monitoring.DeviceMonitor
import app.playcore.core.permissions.CapabilityManager
import app.playcore.data.GameRepository
import app.playcore.data.InstalledAppScanner
import app.playcore.data.local.PlaycoreDatabase
import app.playcore.data.local.RoomRestorationStore
import app.playcore.data.preferences.SettingsRepository
import app.playcore.domain.restoration.RestorationCoordinator
import app.playcore.domain.restoration.RestorationEngine
import app.playcore.domain.restoration.RestorationStore
import app.playcore.domain.restoration.SystemControlPort
import app.playcore.overlay.OverlayController
import app.playcore.system.AndroidSystemControlAdapter

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

    val restorationStore: RestorationStore by lazy {
        RoomRestorationStore(database.restorationDao())
    }

    val systemControl: SystemControlPort by lazy { AndroidSystemControlAdapter(appContext) }

    val restorationEngine: RestorationEngine by lazy {
        RestorationEngine(control = systemControl, store = restorationStore)
    }

    val restorationCoordinator: RestorationCoordinator by lazy {
        RestorationCoordinator(restorationEngine)
    }

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
