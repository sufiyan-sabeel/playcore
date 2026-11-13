package app.playcore.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.playcore.PlaycoreApplication
import app.playcore.core.model.Game
import app.playcore.core.model.GameCategory
import app.playcore.core.model.LaunchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val packageName: String = "",
    val game: Game? = null,
    val categories: List<GameCategory> = emptyList(),
    val lastLaunchResult: LaunchResult? = null,
)

class GameDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val container = (application as PlaycoreApplication).container
    private val repository = container.gameRepository
    private val packageName: String = savedStateHandle.get<String>("packageName").orEmpty()
    private val lastLaunchResult = MutableStateFlow<LaunchResult?>(null)

    val uiState: StateFlow<GameDetailUiState> = combine(
        repository.observeGame(packageName),
        repository.observeCategories(),
        lastLaunchResult,
    ) { game, categories, launchResult ->
        GameDetailUiState(
            packageName = packageName,
            game = game,
            categories = categories,
            lastLaunchResult = launchResult,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameDetailUiState(packageName = packageName))

    fun launch() {
        viewModelScope.launch {
            lastLaunchResult.value = repository.launch(packageName)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch { repository.toggleFavorite(packageName) }
    }

    fun setCategory(categoryId: Long?) {
        viewModelScope.launch { repository.setCategory(packageName, categoryId) }
    }

    fun createCategory(name: String) {
        viewModelScope.launch { repository.createCategory(name) }
    }

    fun removeFromLibrary(onRemoved: () -> Unit) {
        viewModelScope.launch {
            repository.removeFromLibrary(packageName)
            onRemoved()
        }
    }
}
