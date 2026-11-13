package app.playcore

import app.playcore.core.model.Game
import app.playcore.core.model.LibraryQuery
import app.playcore.core.model.LibrarySort
import app.playcore.domain.LibraryFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFilterTest {

    private val games = listOf(
        Game(packageName = "com.studio.alpha", label = "Alpha Strike", isFavorite = false, categoryId = 1L, lastLaunchedAt = 300L),
        Game(packageName = "com.studio.bravo", label = "bravo racing", isFavorite = true, categoryId = null, lastLaunchedAt = null),
        Game(packageName = "com.studio.charlie", label = "Charlie Quest", isFavorite = false, categoryId = 2L, lastLaunchedAt = 100L),
        Game(packageName = "org.example.alpha2", label = "Delta", isFavorite = true, categoryId = 1L, lastLaunchedAt = 200L),
    )

    @Test
    fun `empty query returns everything`() {
        val result = LibraryFilter.apply(games, LibraryQuery())
        assertEquals(4, result.size)
    }

    @Test
    fun `search matches label case insensitively`() {
        val result = LibraryFilter.apply(games, LibraryQuery(search = "ALPHA"))
        assertEquals(2, result.size)
        assertTrue(result.any { it.packageName == "com.studio.alpha" })
    }

    @Test
    fun `search matches package name`() {
        val result = LibraryFilter.apply(games, LibraryQuery(search = "org.example"))
        assertEquals(1, result.size)
        assertEquals("org.example.alpha2", result.first().packageName)
    }

    @Test
    fun `search ignores surrounding whitespace`() {
        val result = LibraryFilter.apply(games, LibraryQuery(search = "  charlie  "))
        assertEquals(1, result.size)
        assertEquals("com.studio.charlie", result.first().packageName)
    }

    @Test
    fun `no match returns empty list`() {
        val result = LibraryFilter.apply(games, LibraryQuery(search = "nothing-matches"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `sort by name is alphabetical and ignores case`() {
        val result = LibraryFilter.apply(games, LibraryQuery(sort = LibrarySort.NAME))
        assertEquals(listOf("Alpha Strike", "bravo racing", "Charlie Quest", "Delta"), result.map { it.label })
    }

    @Test
    fun `sort by recent puts never launched last`() {
        val result = LibraryFilter.apply(games, LibraryQuery(sort = LibrarySort.RECENT))
        assertEquals("com.studio.alpha", result.first().packageName)
        assertEquals("com.studio.bravo", result.last().packageName)
    }

    @Test
    fun `sort favourites first puts favourites on top`() {
        val result = LibraryFilter.apply(games, LibraryQuery(sort = LibrarySort.FAVORITES_FIRST))
        assertTrue(result.take(2).all { it.isFavorite })
    }

    @Test
    fun `favourites only filter`() {
        val result = LibraryFilter.apply(games, LibraryQuery(favoritesOnly = true))
        assertEquals(2, result.size)
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `category filter`() {
        val result = LibraryFilter.apply(games, LibraryQuery(categoryId = 1L))
        assertEquals(2, result.size)
        assertTrue(result.all { it.categoryId == 1L })
    }

    @Test
    fun `filters combine`() {
        val result = LibraryFilter.apply(
            games,
            LibraryQuery(search = "alpha", favoritesOnly = true, categoryId = 1L),
        )
        assertEquals(1, result.size)
        assertEquals("org.example.alpha2", result.first().packageName)
    }
}
