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
    val isInstalled: Boolean = true,
)

data class GameCategory(
    val id: Long,
    val name: String,
)

enum class LibrarySort { RECENT, NAME, FAVORITES_FIRST }

enum class LayoutMode { GRID, LIST }

enum class ThemeMode { SYSTEM, DARK, LIGHT }

enum class AccentChoice { TEAL, CYAN, VIOLET, AMBER }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val amoled: Boolean = true,
    val accent: AccentChoice = AccentChoice.TEAL,
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
 * A dashboard reading. A metric is either a real value read from the platform,
 * or an explicit "unavailable" carrying the reason. There is no third state and
 * no placeholder number.
 */
sealed interface Metric {
    data class Value(val label: String, val value: String, val detail: String? = null) : Metric
    data class Unavailable(val label: String, val reason: String) : Metric
}

data class DeviceSnapshot(
    val metrics: List<Metric>,
    val capturedAt: Long,
)

enum class CapabilityId { OVERLAY, USAGE_ACCESS, NOTIFICATIONS, MEDIA_LIBRARY, SCREEN_CAPTURE, SHIZUKU }

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
