package app.playcore.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.playcore.core.model.AccentChoice
import app.playcore.core.model.AppSettings
import app.playcore.core.model.LayoutMode
import app.playcore.core.model.LibrarySort
import app.playcore.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.playcoreSettings: DataStore<Preferences> by preferencesDataStore(
    name = "playcore_settings"
)

/**
 * Lightweight preferences in DataStore. Everything here changes real behaviour;
 * there are no decorative switches.
 */
class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.playcoreSettings.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AppSettings(
                themeMode = prefs[Keys.THEME_MODE].toEnum(ThemeMode.SYSTEM),
                amoled = prefs[Keys.AMOLED] ?: true,
                accent = prefs[Keys.ACCENT].toEnum(AccentChoice.TEAL),
                librarySort = prefs[Keys.LIBRARY_SORT].toEnum(LibrarySort.RECENT),
                layout = prefs[Keys.LAYOUT].toEnum(LayoutMode.GRID),
                dashboardIntervalMs = (prefs[Keys.DASHBOARD_INTERVAL] ?: DEFAULT_INTERVAL_MS).toLong(),
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setAmoled(enabled: Boolean) = edit { it[Keys.AMOLED] = enabled }

    suspend fun setAccent(accent: AccentChoice) = edit { it[Keys.ACCENT] = accent.name }

    suspend fun setLibrarySort(sort: LibrarySort) = edit { it[Keys.LIBRARY_SORT] = sort.name }

    suspend fun setLayout(layout: LayoutMode) = edit { it[Keys.LAYOUT] = layout.name }

    suspend fun setDashboardInterval(intervalMs: Long) =
        edit { it[Keys.DASHBOARD_INTERVAL] = intervalMs.toInt() }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.playcoreSettings.edit { prefs -> block(prefs) }
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AMOLED = booleanPreferencesKey("amoled")
        val ACCENT = stringPreferencesKey("accent")
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
