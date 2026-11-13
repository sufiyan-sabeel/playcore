package app.playcore.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.playcore.PlaycoreApplication
import app.playcore.core.model.Capability
import app.playcore.core.model.Game
import app.playcore.core.model.LaunchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val libraryCount: Int = 0,
    val favorites: List<Game> = emptyList(),
    val recent: List<Game> = emptyList(),
    val capabilities: List<Capability> = emptyList(),
    val needsAttention: List<Capability> = emptyList(),
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PlaycoreApplication).container
    private val repository = container.gameRepository
    private val capabilities = MutableStateFlow(container.capabilityManager.snapshot())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeLibrary(),
        capabilities,
    ) { games, caps ->
        HomeUiState(
            libraryCount = games.size,
            favorites = games.filter { it.isFavorite }.take(6),
            recent = games.filter { it.lastLaunchedAt != null }
                .sortedByDescending { it.lastLaunchedAt }
                .take(6),
            capabilities = caps,
            needsAttention = caps.filter { it.status != app.playcore.core.model.CapabilityStatus.AVAILABLE },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun refreshCapabilities() {
        capabilities.value = container.capabilityManager.snapshot()
    }

    fun launch(packageName: String, onResult: (LaunchResult) -> Unit) {
        viewModelScope.launch { onResult(repository.launch(packageName)) }
    }
}
