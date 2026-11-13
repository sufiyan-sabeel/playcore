package app.playcore

import app.playcore.core.model.AppMode
import app.playcore.core.model.AppSettings
import app.playcore.core.model.CubeCard
import app.playcore.core.model.CubeMode
import app.playcore.core.model.GameProfile
import app.playcore.core.model.OverlayStyle
import app.playcore.core.model.ReferenceContent
import app.playcore.data.local.toEntity
import app.playcore.data.local.toModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileMappingTest {

    @Test
    fun `profile survives a storage round trip`() {
        val profile = GameProfile(
            packageName = "com.example.game",
            overlayStyle = OverlayStyle.COMPACT,
            cubeMode = CubeMode.ECO,
            enabledCards = setOf(CubeCard.CROSSHAIR, CubeCard.STOPWATCH, CubeCard.DND),
            dndEnabled = true,
            brightnessLock = true,
            refreshRateHz = 90,
            cpuMode = "Rise",
            notes = "cooldowns",
            updatedAt = 1234L,
        )
        val restored = profile.toEntity().toModel()
        assertEquals(profile.packageName, restored.packageName)
        assertEquals(OverlayStyle.COMPACT, restored.overlayStyle)
        assertEquals(CubeMode.ECO, restored.cubeMode)
        assertEquals(profile.enabledCards, restored.enabledCards)
        assertTrue(restored.dndEnabled)
        assertTrue(restored.brightnessLock)
        assertEquals(90, restored.refreshRateHz)
        assertEquals("Rise", restored.cpuMode)
        assertEquals("cooldowns", restored.notes)
    }

    @Test
    fun `unknown stored enum names fall back instead of throwing`() {
        val entity = GameProfile(
            packageName = "a.b",
            overlayStyle = OverlayStyle.X_MODE,
        ).toEntity().copy(overlayStyle = "REMOVED_STYLE", cubeMode = "REMOVED_MODE", enabledCards = "CROSSHAIR,REMOVED")
        val restored = entity.toModel()
        assertEquals(OverlayStyle.ENERGY_CUBE, restored.overlayStyle)
        assertEquals(CubeMode.BALANCE, restored.cubeMode)
        assertEquals(setOf(CubeCard.CROSSHAIR), restored.enabledCards)
    }

    @Test
    fun `default profile carries the documented default cards`() {
        val profile = GameProfile(packageName = "a.b")
        assertTrue(profile.enabledCards.contains(CubeCard.CROSSHAIR))
        assertEquals(CubeMode.BALANCE, profile.overlayStyle.let { CubeMode.BALANCE })
        assertEquals(OverlayStyle.ENERGY_CUBE, profile.overlayStyle)
    }

    @Test
    fun `reference content matches the verified reference inventory`() {
        assertEquals(20, ReferenceContent.plugins.size)
        assertEquals(10, ReferenceContent.backdrops.size)
        assertEquals(7, ReferenceContent.helpTopics.size)
        assertEquals(19, ReferenceContent.cubeCards.size)
        assertEquals(4, OverlayStyle.entries.size)
        assertEquals(3, AppMode.entries.size)
        assertEquals(3, ReferenceContent.cubeModes.size)
        assertEquals(5, ReferenceContent.ranks.size)
    }

    @Test
    fun `settings default to the dark console mode`() {
        val settings = AppSettings()
        assertEquals(AppMode.EXPLORE, settings.appMode)
        assertEquals(OverlayStyle.ENERGY_CUBE, settings.overlayStyle)
        assertEquals(CubeMode.BALANCE, settings.cubeMode)
        assertEquals("starfield", settings.backdrop)
        assertTrue(settings.vignette)
    }
}
