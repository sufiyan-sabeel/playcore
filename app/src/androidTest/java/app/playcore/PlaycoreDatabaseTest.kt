package app.playcore

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.playcore.data.local.CategoryEntity
import app.playcore.data.local.GameEntity
import app.playcore.data.local.PlaycoreDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on a device or emulator; requires an in-memory Room database. */
@RunWith(AndroidJUnit4::class)
class PlaycoreDatabaseTest {

    private lateinit var database: PlaycoreDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PlaycoreDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadGame() = runBlocking {
        database.gameDao().upsert(GameEntity(packageName = "com.test.one", label = "One", addedAt = 1L))
        val all = database.gameDao().observeAll().first()
        assertEquals(1, all.size)
        assertEquals("One", all.first().label)
    }

    @Test
    fun favouriteTogglePersists() = runBlocking {
        database.gameDao().upsert(GameEntity(packageName = "com.test.two", label = "Two", addedAt = 1L))
        database.gameDao().setFavorite("com.test.two", true)
        val stored = database.gameDao().find("com.test.two")
        assertNotNull(stored)
        assertTrue(stored!!.isFavorite)
    }

    @Test
    fun categoryAssignmentAndClearing() = runBlocking {
        val categoryId = database.categoryDao().insert(CategoryEntity(name = "Racing"))
        database.gameDao().upsert(GameEntity(packageName = "com.test.three", label = "Three", addedAt = 1L))
        database.gameDao().setCategory("com.test.three", categoryId)
        assertEquals(categoryId, database.gameDao().find("com.test.three")?.categoryId)

        database.categoryDao().clearCategoryFromGames(categoryId)
        database.categoryDao().delete(categoryId)
        assertNull(database.gameDao().find("com.test.three")?.categoryId)
    }

    @Test
    fun deleteRemovesEntry() = runBlocking {
        database.gameDao().upsert(GameEntity(packageName = "com.test.four", label = "Four", addedAt = 1L))
        database.gameDao().delete("com.test.four")
        assertNull(database.gameDao().find("com.test.four"))
    }

    @Test
    fun duplicateCategoryNameIsIgnored() = runBlocking {
        val first = database.categoryDao().insert(CategoryEntity(name = "Action"))
        val second = database.categoryDao().insert(CategoryEntity(name = "Action"))
        assertTrue(first > 0L)
        assertEquals(-1L, second)
    }
}
