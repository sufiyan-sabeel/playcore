package app.playcore.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.playcore.PlaycoreApplication
import app.playcore.core.model.DeviceSnapshot
import app.playcore.core.monitoring.Tickers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val snapshot: DeviceSnapshot? = null,
    val intervalMs: Long = 2_000L,
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PlaycoreApplication).container
    private val monitor = container.deviceMonitor
    private val settingsRepository = container.settingsRepository
    private val refreshRequests = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = combine(
        settingsRepository.settings.map { it.dashboardIntervalMs }.distinctUntilChanged(),
        refreshRequests,
    ) { interval, _ -> interval }
        .flatMapLatest { interval ->
            Tickers.every(interval).map { DashboardUiState(snapshot = monitor.snapshot(), intervalMs = interval) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun refreshNow() {
        refreshRequests.value = refreshRequests.value + 1
    }
}
