package app.playcore.ui.system

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.playcore.PlaycoreApplication
import app.playcore.core.model.AppMode
import app.playcore.core.model.AppSettings
import app.playcore.core.model.Capability
import app.playcore.core.model.CapabilityId
import app.playcore.core.model.Game
import app.playcore.core.model.GameProfile
import app.playcore.core.model.LayoutMode
import app.playcore.core.model.LibrarySort
import app.playcore.core.model.OverlayStyle
import app.playcore.core.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SystemUiState(
    val settings: AppSettings = AppSettings(),
    val capabilities: List<Capability> = emptyList(),
    val games: List<Game> = emptyList(),
    val profiles: List<GameProfile> = emptyList(),
    val overlayShowing: Boolean = false,
    val overlayPermission: Boolean = false,
    val totalPlayMs: Long = 0L,
    val rank: String = "Rookie",
)

/**
 * Shared state for the shell: mode, appearance, capability report, library totals
 * and the overlay preview. One view model rather than eight, because every screen
 * in the shell reads the same five flows.
 */
class SystemViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PlaycoreApplication).container
    private val repository = container.gameRepository
    private val settingsRepository = container.settingsRepository
    private val capabilityManager = container.capabilityManager
    private val overlayController = container.overlayController

    private val capabilities = MutableStateFlow(capabilityManager.snapshot())
    private val overlayShowing = MutableStateFlow(overlayController.isShowing)

    val uiState: StateFlow<SystemUiState> = combine(
        settingsRepository.settings,
        capabilities,
        repository.observeLibrary(),
        repository.observeAllProfiles(),
        overlayShowing,
    ) { settings, caps, games, profiles, showing ->
        val total = games.sumOf { it.totalPlayMs }
        SystemUiState(
            settings = settings,
            capabilities = caps,
            games = games,
            profiles = profiles,
            overlayShowing = showing,
            overlayPermission = overlayController.canDrawOverlays(),
            totalPlayMs = total,
            rank = rankFor(total),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SystemUiState())

    fun refresh() {
        capabilities.value = capabilityManager.snapshot()
        overlayShowing.value = overlayController.isShowing
    }

    fun setMode(mode: AppMode) = viewModelScope.launch { settingsRepository.setAppMode(mode) }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }

    fun setOverlayStyle(style: OverlayStyle) =
        viewModelScope.launch { settingsRepository.setOverlayStyle(style) }

    fun setBackdrop(id: String) = viewModelScope.launch { settingsRepository.setBackdrop(id) }

    fun setParticles(enabled: Boolean) = viewModelScope.launch { settingsRepository.setParticles(enabled) }

    fun setVignette(enabled: Boolean) = viewModelScope.launch { settingsRepository.setVignette(enabled) }

    fun setLedEffect(id: String) = viewModelScope.launch { settingsRepository.setLedEffect(id) }

    fun setLibrarySort(sort: LibrarySort) = viewModelScope.launch { settingsRepository.setLibrarySort(sort) }

    fun setLayout(layout: LayoutMode) = viewModelScope.launch { settingsRepository.setLayout(layout) }

    fun setDashboardInterval(ms: Long) = viewModelScope.launch { settingsRepository.setDashboardInterval(ms) }

    /** Shows or hides the real floating panel. Reports honestly when it cannot. */
    fun toggleOverlayPreview() {
        if (overlayController.isShowing) {
            overlayController.hide()
            overlayShowing.value = false
            return
        }
        if (!overlayController.canDrawOverlays()) return
        overlayController.show(uiState.value.settings.overlayStyle)
        overlayShowing.value = overlayController.isShowing
    }

    fun openCapabilitySettings(id: CapabilityId): Boolean = capabilityManager.openSettings(id)

    private fun rankFor(totalMs: Long): String = when {
        totalMs >= 50 * 3_600_000L -> "Legend"
        totalMs >= 20 * 3_600_000L -> "Master"
        totalMs >= 5 * 3_600_000L -> "Elite"
        totalMs >= 3_600_000L -> "Veteran"
        else -> "Rookie"
    }
}
