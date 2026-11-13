package app.playcore.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.playcore.BuildConfig
import app.playcore.PlaycoreApplication
import app.playcore.core.model.AccentChoice
import app.playcore.core.model.AppSettings
import app.playcore.core.model.Capability
import app.playcore.core.model.CapabilityId
import app.playcore.core.model.LayoutMode
import app.playcore.core.model.LibrarySort
import app.playcore.core.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val capabilities: List<Capability> = emptyList(),
    val versionName: String = BuildConfig.VERSION_NAME,
    val versionCode: Int = BuildConfig.VERSION_CODE,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PlaycoreApplication).container
    private val settingsRepository = container.settingsRepository
    private val capabilities = MutableStateFlow(container.capabilityManager.snapshot())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        capabilities,
    ) { settings, caps ->
        SettingsUiState(
            settings = settings,
            capabilities = caps,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun refreshCapabilities() {
        capabilities.value = container.capabilityManager.snapshot()
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }

    fun setAmoled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAmoled(enabled) }

    fun setAccent(accent: AccentChoice) = viewModelScope.launch { settingsRepository.setAccent(accent) }

    fun setLibrarySort(sort: LibrarySort) = viewModelScope.launch { settingsRepository.setLibrarySort(sort) }

    fun setLayout(layout: LayoutMode) = viewModelScope.launch { settingsRepository.setLayout(layout) }

    fun setDashboardInterval(intervalMs: Long) =
        viewModelScope.launch { settingsRepository.setDashboardInterval(intervalMs) }

    fun openCapabilitySettings(id: CapabilityId): Boolean = container.capabilityManager.openSettings(id)
}
