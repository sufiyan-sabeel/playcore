package app.playcore.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.playcore.core.model.AppMode
import app.playcore.core.model.AppSettings
import app.playcore.core.model.CubeMode
import app.playcore.core.model.LayoutMode
import app.playcore.core.model.LibrarySort
import app.playcore.core.model.OverlayStyle
import app.playcore.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.playcoreSettings: DataStore<Preferences> by preferencesDataStore(
    name = "playcore_settings"
)

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.playcoreSettings.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AppSettings(
                themeMode = prefs[Keys.THEME_MODE].toEnum(ThemeMode.DARK),
                appMode = prefs[Keys.APP_MODE].toEnum(AppMode.EXPLORE),
                overlayStyle = prefs[Keys.OVERLAY_STYLE].toEnum(OverlayStyle.ENERGY_CUBE),
                cubeMode = prefs[Keys.CUBE_MODE].toEnum(CubeMode.BALANCE),
                backdrop = prefs[Keys.BACKDROP] ?: "starfield",
                particles = prefs[Keys.PARTICLES] ?: true,
                vignette = prefs[Keys.VIGNETTE] ?: true,
                ledEffect = prefs[Keys.LED] ?: "steady",
                librarySort = prefs[Keys.LIBRARY_SORT].toEnum(LibrarySort.RECENT),
                layout = prefs[Keys.LAYOUT].toEnum(LayoutMode.GRID),
                dashboardIntervalMs = (prefs[Keys.DASHBOARD_INTERVAL] ?: DEFAULT_INTERVAL_MS).toLong(),
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setAppMode(mode: AppMode) = edit { it[Keys.APP_MODE] = mode.name }

    suspend fun setOverlayStyle(style: OverlayStyle) = edit { it[Keys.OVERLAY_STYLE] = style.name }

    suspend fun setCubeMode(mode: CubeMode) = edit { it[Keys.CUBE_MODE] = mode.name }

    suspend fun setBackdrop(id: String) = edit { it[Keys.BACKDROP] = id }

    suspend fun setParticles(enabled: Boolean) = edit { it[Keys.PARTICLES] = enabled }

    suspend fun setVignette(enabled: Boolean) = edit { it[Keys.VIGNETTE] = enabled }

    suspend fun setLedEffect(id: String) = edit { it[Keys.LED] = id }

    suspend fun setLibrarySort(sort: LibrarySort) = edit { it[Keys.LIBRARY_SORT] = sort.name }

    suspend fun setLayout(layout: LayoutMode) = edit { it[Keys.LAYOUT] = layout.name }

    suspend fun setDashboardInterval(intervalMs: Long) =
        edit { it[Keys.DASHBOARD_INTERVAL] = intervalMs.toInt() }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        context.playcoreSettings.edit { prefs -> block(prefs) }
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_MODE = stringPreferencesKey("app_mode")
        val OVERLAY_STYLE = stringPreferencesKey("overlay_style")
        val CUBE_MODE = stringPreferencesKey("cube_mode")
        val BACKDROP = stringPreferencesKey("backdrop")
        val PARTICLES = booleanPreferencesKey("particles")
        val VIGNETTE = booleanPreferencesKey("vignette")
        val LED = stringPreferencesKey("led_effect")
        val LIBRARY_SORT = stringPreferencesKey("library_sort")
        val LAYOUT = stringPreferencesKey("layout")
        val DASHBOARD_INTERVAL = intPreferencesKey("dashboard_interval_ms")
    }

    companion object {
        const val DEFAULT_INTERVAL_MS = 2_000
    }
}

private inline fun <reified T : Enum<T>> String?.toEnum(fallback: T): T =
    this?.let { name -> runCatching { enumValueOf<T>(name) }.getOrNull() } ?: fallback
