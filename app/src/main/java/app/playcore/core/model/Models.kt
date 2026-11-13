package app.playcore.core.model

/** An application installed on the device that has a launcher activity. */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val isLaunchable: Boolean,
)

/** An entry the user has explicitly added to the PLAYCORE library. */
data class Game(
    val packageName: String,
    val label: String,
    val isFavorite: Boolean = false,
    val categoryId: Long? = null,
    val lastLaunchedAt: Long? = null,
    val addedAt: Long = 0L,
    val totalPlayMs: Long = 0L,
    val isInstalled: Boolean = true,
)

data class GameCategory(val id: Long, val name: String)

enum class LibrarySort { RECENT, NAME, FAVORITES_FIRST }

enum class LayoutMode { GRID, LIST }

enum class ThemeMode { SYSTEM, DARK, LIGHT }

/** The three modes the reference application ships. */
enum class AppMode { EXPLORE, HANDHELD, GAMEPAD }

/** The four overlay styles named in the reference string table. */
enum class OverlayStyle { ENERGY_CUBE, COMPACT, NOSTALGIA, X_MODE }

enum class CubeMode { BALANCE, RISE, ECO }

enum class ThermalState { COOL, WARM, HOT }

/** The nineteen Energy Cube cards named in the reference. */
enum class CubeCard {
    COUNTER, CROSSHAIR, DOCK_SIDE, PERFORMANCE, EQUALIZER, GAMEPAD, INFO, PROJECTION,
    ROTATION_LOCK, SCREEN_REC, SCREENSHOT, SUPER_SNAP, STOPWATCH, WIFI, DND, BYPASS,
    BRIGHT_LOCK, NO_BANNER, OPTIMIZE,
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val appMode: AppMode = AppMode.EXPLORE,
    val overlayStyle: OverlayStyle = OverlayStyle.ENERGY_CUBE,
    val cubeMode: CubeMode = CubeMode.BALANCE,
    val backdrop: String = "starfield",
    val particles: Boolean = true,
    val vignette: Boolean = true,
    val ledEffect: String = "steady",
    val librarySort: LibrarySort = LibrarySort.RECENT,
    val layout: LayoutMode = LayoutMode.GRID,
    val dashboardIntervalMs: Long = 2_000L,
)

data class LibraryQuery(
    val search: String = "",
    val sort: LibrarySort = LibrarySort.RECENT,
    val favoritesOnly: Boolean = false,
    val categoryId: Long? = null,
)

/**
 * A per-game profile. Phase 1 stores the choices; the settings a profile can
 * actually write are gated by capability, not by this model.
 */
data class GameProfile(
    val packageName: String,
    val overlayStyle: OverlayStyle = OverlayStyle.ENERGY_CUBE,
    val cubeMode: CubeMode = CubeMode.BALANCE,
    val enabledCards: Set<CubeCard> = DEFAULT_CARDS,
    val dndEnabled: Boolean = false,
    val brightnessLock: Boolean = false,
    val refreshRateHz: Int? = null,
    val cpuMode: String? = null,
    val notes: String = "",
    val updatedAt: Long = 0L,
) {
    companion object {
        val DEFAULT_CARDS: Set<CubeCard> = setOf(
            CubeCard.CROSSHAIR, CubeCard.DOCK_SIDE, CubeCard.PERFORMANCE,
            CubeCard.STOPWATCH, CubeCard.SCREENSHOT, CubeCard.DND,
        )
    }
}

data class Plugin(
    val id: String,
    val name: String,
    val summary: String,
    val requiresPrivilegedAccess: Boolean,
)

data class HelpTopic(val title: String, val body: String)

data class BackdropOption(val id: String, val name: String)

/**
 * A dashboard reading. A metric is either a real value read from the platform,
 * or an explicit "unavailable" carrying the reason.
 */
sealed interface Metric {
    data class Value(val label: String, val value: String, val detail: String? = null) : Metric
    data class Unavailable(val label: String, val reason: String) : Metric
}

data class DeviceSnapshot(val metrics: List<Metric>, val capturedAt: Long)

enum class CapabilityId {
    OVERLAY, USAGE_ACCESS, NOTIFICATIONS, MEDIA_LIBRARY, SCREEN_CAPTURE,
    SHIZUKU, ROOT, KERNEL_CONTROL,
}

enum class CapabilityStatus {
    AVAILABLE,
    PERMISSION_REQUIRED,
    BACKEND_REQUIRED,
    DEVICE_UNSUPPORTED,
    TEMPORARILY_UNAVAILABLE,
    UNAVAILABLE,
}

data class Capability(
    val id: CapabilityId,
    val title: String,
    val status: CapabilityStatus,
    val explanation: String,
    val actionLabel: String? = null,
)

sealed interface LaunchResult {
    data object Launched : LaunchResult
    data object NotInstalled : LaunchResult
    data object NoLaunchIntent : LaunchResult
}

/** Evidence grading used by the visual inventory and the tracker. */
enum class EvidenceLevel { VERIFIED, INFERRED, BLOCKED }
