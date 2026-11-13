package app.playcore.domain

import app.playcore.core.model.Game
import app.playcore.core.model.LibraryQuery
import app.playcore.core.model.LibrarySort

/**
 * Pure search, filter and sort for the game library. Kept out of the ViewModel
 * and out of composables so it can be unit tested without Android.
 */
object LibraryFilter {

    fun apply(games: List<Game>, query: LibraryQuery): List<Game> {
        val filtered = games.filter { game ->
            val matchesSearch = query.search.isBlank() ||
                game.label.contains(query.search.trim(), ignoreCase = true) ||
                game.packageName.contains(query.search.trim(), ignoreCase = true)
            val matchesFavorite = !query.favoritesOnly || game.isFavorite
            val matchesCategory = query.categoryId == null || game.categoryId == query.categoryId
            matchesSearch && matchesFavorite && matchesCategory
        }
        return when (query.sort) {
            LibrarySort.NAME ->
                filtered.sortedBy { it.label.lowercase() }

            LibrarySort.RECENT ->
                filtered.sortedWith(
                    compareByDescending<Game> { it.lastLaunchedAt ?: Long.MIN_VALUE }
                        .thenBy { it.label.lowercase() }
                )

            LibrarySort.FAVORITES_FIRST ->
                filtered.sortedWith(
                    compareByDescending<Game> { it.isFavorite }
                        .thenBy { it.label.lowercase() }
                )
        }
    }
}
