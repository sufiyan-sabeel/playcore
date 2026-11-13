package app.playcore

import app.playcore.core.model.AppSettings
import app.playcore.core.model.LayoutMode
import app.playcore.core.model.LibrarySort
import app.playcore.core.model.ThemeMode
import app.playcore.data.local.CategoryEntity
import app.playcore.data.local.GameEntity
import app.playcore.data.local.toModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryMappingTest {

    @Test
    fun `game entity maps every field`() {
        val entity = GameEntity(
            packageName = "com.example.game",
            label = "Example Game",
            isFavorite = true,
            categoryId = 7L,
            lastLaunchedAt = 1_000L,
            addedAt = 500L,
        )
        val model = entity.toModel(isInstalled = true)
        assertEquals("com.example.game", model.packageName)
        assertEquals("Example Game", model.label)
        assertTrue(model.isFavorite)
        assertEquals(7L, model.categoryId)
        assertEquals(1_000L, model.lastLaunchedAt)
        assertEquals(500L, model.addedAt)
        assertTrue(model.isInstalled)
    }

    @Test
    fun `installed flag is carried through`() {
        val entity = GameEntity(packageName = "a.b", label = "B", addedAt = 1L)
        assertFalse(entity.toModel(isInstalled = false).isInstalled)
    }

    @Test
    fun `category entity maps to model`() {
        val model = CategoryEntity(id = 3L, name = "Shooters").toModel()
        assertEquals(3L, model.id)
        assertEquals("Shooters", model.name)
    }

    @Test
    fun `settings defaults are local-first and conservative`() {
        val defaults = AppSettings()
        assertEquals(ThemeMode.SYSTEM, defaults.themeMode)
        assertTrue(defaults.amoled)
        assertEquals(LibrarySort.RECENT, defaults.librarySort)
        assertEquals(LayoutMode.GRID, defaults.layout)
        assertTrue(defaults.dashboardIntervalMs >= 1_000L)
    }
}
