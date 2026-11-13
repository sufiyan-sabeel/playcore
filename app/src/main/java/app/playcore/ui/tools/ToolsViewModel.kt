package app.playcore.ui.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.playcore.PlaycoreApplication
import app.playcore.core.model.Capability
import app.playcore.core.model.CapabilityId
import app.playcore.core.model.Game
import app.playcore.core.model.Metric
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ToolsUiState(
    val capabilities: List<Capability> = emptyList(),
    val deviceMetrics: List<Metric> = emptyList(),
    val libraryGames: List<Game> = emptyList(),
)

class ToolsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PlaycoreApplication).container
    private val capabilities = MutableStateFlow(container.capabilityManager.snapshot())
    private val deviceMetrics = MutableStateFlow(container.deviceMonitor.snapshot().metrics)

    val uiState: StateFlow<ToolsUiState> = combine(
        container.gameRepository.observeLibrary(),
        capabilities,
        deviceMetrics,
    ) { games, caps, metrics ->
        ToolsUiState(capabilities = caps, deviceMetrics = metrics, libraryGames = games)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ToolsUiState())

    fun refresh() {
        capabilities.value = container.capabilityManager.snapshot()
        deviceMetrics.value = container.deviceMonitor.snapshot().metrics
    }

    fun openCapabilitySettings(id: CapabilityId): Boolean = container.capabilityManager.openSettings(id)
}
