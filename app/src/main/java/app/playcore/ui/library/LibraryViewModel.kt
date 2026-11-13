package app.playcore.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.playcore.PlaycoreApplication
import app.playcore.core.model.Game
import app.playcore.core.model.GameCategory
import app.playcore.core.model.InstalledApp
import app.playcore.core.model.LaunchResult
import app.playcore.core.model.LibraryQuery
import app.playcore.core.model.LibrarySort
import app.playcore.domain.LibraryFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val isLoading: Boolean = true,
    val games: List<Game> = emptyList(),
    val totalInLibrary: Int = 0,
    val categories: List<GameCategory> = emptyList(),
    val query: LibraryQuery = LibraryQuery(),
    val error: String? = null,
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PlaycoreApplication).container
    private val repository = container.gameRepository
    private val settingsRepository = container.settingsRepository

    private val query = MutableStateFlow(LibraryQuery())
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)

    val installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val isScanning = MutableStateFlow(false)

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeLibrary(),
        repository.observeCategories(),
        query,
        combine(loading, error) { isLoading, errorMessage -> isLoading to errorMessage },
    ) { games, categories, currentQuery, loadState ->
        LibraryUiState(
            isLoading = loadState.first,
            games = LibraryFilter.apply(games, currentQuery),
            totalInLibrary = games.size,
            categories = categories,
            query = currentQuery,
            error = loadState.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    init {
        viewModelScope.launch {
            settingsRepository.settings.map { it.librarySort }.collect { sort ->
                query.update { current -> if (current.sort == sort) current else current.copy(sort = sort) }
            }
        }
        refreshInstalled()
    }

    fun refreshInstalled() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching { repository.refreshInstalledApps() }
                .onFailure { error.value = it.message ?: "Installed apps could not be read." }
            loading.value = false
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            isScanning.value = true
            runCatching { repository.refreshInstalledApps() }
                .onSuccess { installedApps.value = it }
                .onFailure { error.value = it.message ?: "Installed apps could not be read." }
            isScanning.value = false
        }
    }

    fun setSearch(text: String) = query.update { it.copy(search = text) }

    fun setSort(sort: LibrarySort) {
        query.update { it.copy(sort = sort) }
        viewModelScope.launch { settingsRepository.setLibrarySort(sort) }
    }

    fun toggleFavoritesOnly() = query.update { it.copy(favoritesOnly = !it.favoritesOnly) }

    fun setCategoryFilter(categoryId: Long?) = query.update { it.copy(categoryId = categoryId) }

    fun addGame(app: InstalledApp, categoryId: Long? = null) {
        viewModelScope.launch { repository.addToLibrary(app, categoryId) }
    }

    fun removeGame(packageName: String) {
        viewModelScope.launch { repository.removeFromLibrary(packageName) }
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch { repository.toggleFavorite(packageName) }
    }

    fun createCategory(name: String) {
        viewModelScope.launch { repository.createCategory(name) }
    }

    fun launch(packageName: String, onResult: (LaunchResult) -> Unit) {
        viewModelScope.launch { onResult(repository.launch(packageName)) }
    }
}
