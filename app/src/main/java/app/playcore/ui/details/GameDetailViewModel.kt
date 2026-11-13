package app.playcore.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import android.app.NotificationManager
import app.playcore.PlaycoreApplication
import app.playcore.core.model.CubeMode
import app.playcore.core.model.Game
import app.playcore.core.model.GameCategory
import app.playcore.core.model.GameProfile
import app.playcore.core.model.LaunchResult
import app.playcore.core.model.OverlayStyle
import app.playcore.domain.restoration.SystemTarget
import app.playcore.domain.restoration.TargetValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val packageName: String = "",
    val game: Game? = null,
    val profile: GameProfile? = null,
    val categories: List<GameCategory> = emptyList(),
    val lastLaunchResult: LaunchResult? = null,
    val activeSessionId: String? = null,
    val sessionMessage: String? = null,
)

private data class SessionUi(val sessionId: String? = null, val message: String? = null)

class GameDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val container = (application as PlaycoreApplication).container
    private val repository = container.gameRepository
    private val packageName: String = savedStateHandle.get<String>("packageName").orEmpty()
    private val lastLaunchResult = MutableStateFlow<LaunchResult?>(null)
    private val restorationEngine = container.restorationEngine
    private val sessionUi = MutableStateFlow(SessionUi())

    val uiState: StateFlow<GameDetailUiState> = combine(
        repository.observeGame(packageName),
        repository.observeProfile(packageName),
        repository.observeCategories(),
        lastLaunchResult,
        sessionUi,
    ) { game, profile, categories, launchResult, session ->
        GameDetailUiState(
            packageName = packageName,
            game = game,
            profile = profile,
            categories = categories,
            lastLaunchResult = launchResult,
            activeSessionId = session.sessionId,
            sessionMessage = session.message,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        GameDetailUiState(packageName = packageName),
    )

    fun launch() {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val result = repository.launch(packageName)
            lastLaunchResult.value = result
            if (result == LaunchResult.Launched) {
                repository.addPlaytime(packageName, System.currentTimeMillis() - startedAt)
            }
        }
    }

    fun toggleFavorite() = viewModelScope.launch { repository.toggleFavorite(packageName) }

    fun setCategory(categoryId: Long?) =
        viewModelScope.launch { repository.setCategory(packageName, categoryId) }

    fun createCategory(name: String) = viewModelScope.launch { repository.createCategory(name) }

    fun setOverlayStyle(style: OverlayStyle) = updateProfile { it.copy(overlayStyle = style) }

    fun setCubeMode(mode: CubeMode) = updateProfile { it.copy(cubeMode = mode) }

    fun setDnd(enabled: Boolean) = updateProfile { it.copy(dndEnabled = enabled) }

    fun setBrightnessLock(enabled: Boolean) = updateProfile { it.copy(brightnessLock = enabled) }

    fun setRefreshRate(hz: Int?) = updateProfile { it.copy(refreshRateHz = hz) }

    private fun updateProfile(block: (GameProfile) -> GameProfile) {
        val current = uiState.value.profile ?: GameProfile(packageName = packageName)
        viewModelScope.launch { repository.saveProfile(block(current)) }
    }

    /**
     * Applies the profile's system intent through the restoration engine. Only
     * settings Android exposes are attempted; the rest are recorded as skipped with
     * the reason, never as applied.
     */
    fun applyLoadout() {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch {
            val outcome = restorationEngine.startSession(packageName, desiredFor(profile))
            sessionUi.value = SessionUi(
                sessionId = outcome.session?.id,
                message = "Applied ${outcome.appliedCount} · skipped ${outcome.skippedCount} · failed ${outcome.failedCount}",
            )
        }
    }

    fun restoreLoadout() {
        val sessionId = sessionUi.value.sessionId ?: return
        viewModelScope.launch {
            val outcome = restorationEngine.restoreSession(sessionId)
            sessionUi.value = SessionUi(
                sessionId = if (outcome.fullyResolved) null else sessionId,
                message = "Restored ${outcome.restoredCount} · left alone ${outcome.skippedChangedCount} · " +
                    "unresolved ${outcome.unresolvedCount} · failed ${outcome.failedCount}",
            )
        }
    }

    private fun desiredFor(profile: GameProfile): Map<SystemTarget, TargetValue> = buildMap {
        if (profile.dndEnabled) {
            put(
                SystemTarget.DO_NOT_DISTURB,
                TargetValue.IntValue(NotificationManager.INTERRUPTION_FILTER_PRIORITY),
            )
        }
        // Refresh rate has no public setter; asking for it records an honest "unsupported".
        profile.refreshRateHz?.let { put(SystemTarget.REFRESH_RATE, TargetValue.TextValue(it.toString())) }
        // Brightness lock stores no target level, so there is nothing to write.
    }

    fun removeFromLibrary(onRemoved: () -> Unit) {
        viewModelScope.launch {
            repository.removeFromLibrary(packageName)
            onRemoved()
        }
    }
}
