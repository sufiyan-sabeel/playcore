package app.playcore.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.BuildConfig
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.core.model.LayoutMode
import app.playcore.core.model.LibrarySort
import app.playcore.core.model.ThemeMode
import app.playcore.ui.components.AngularButton
import app.playcore.ui.components.AngularTabs
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.CapabilityRow
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.InfoRow
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.components.SectionLabel
import app.playcore.ui.system.SystemViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    ConsoleBackground(settings.backdrop, settings.vignette) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReferenceHeader(
                title = "Settings",
                subtitle = "PLAYCORE / SYSTEM",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                BanneredPanel(title = "Appearance") {
                    SectionLabel("Theme")
                    Spacer(Modifier.height(Space.s))
                    val themes = ThemeMode.entries
                    AngularTabs(
                        options = themes.map {
                            when (it) {
                                ThemeMode.SYSTEM -> "System"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.LIGHT -> "Light"
                            }
                        },
                        selectedIndex = themes.indexOf(settings.themeMode).coerceAtLeast(0),
                        onSelect = { viewModel.setThemeMode(themes[it]) },
                    )
                }

                Spacer(Modifier.height(Space.l))

                BanneredPanel(title = "Library") {
                    SectionLabel("Default sort")
                    Spacer(Modifier.height(Space.s))
                    val sorts = LibrarySort.entries
                    AngularTabs(
                        options = sorts.map {
                            when (it) {
                                LibrarySort.RECENT -> "Recent"
                                LibrarySort.NAME -> "Name"
                                LibrarySort.FAVORITES_FIRST -> "Favourites"
                            }
                        },
                        selectedIndex = sorts.indexOf(settings.librarySort).coerceAtLeast(0),
                        onSelect = { viewModel.setLibrarySort(sorts[it]) },
                    )
                    Spacer(Modifier.height(Space.m))
                    SectionLabel("Default layout")
                    Spacer(Modifier.height(Space.s))
                    val layouts = LayoutMode.entries
                    AngularTabs(
                        options = layouts.map { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                        selectedIndex = layouts.indexOf(settings.layout).coerceAtLeast(0),
                        onSelect = { viewModel.setLayout(layouts[it]) },
                    )
                }

                Spacer(Modifier.height(Space.l))

                BanneredPanel(title = "Monitor") {
                    SectionLabel("Refresh interval")
                    Spacer(Modifier.height(Space.s))
                    val intervals = listOf(1_000L, 2_000L, 5_000L)
                    AngularTabs(
                        options = intervals.map { "${it / 1000}s" },
                        selectedIndex = intervals.indexOf(settings.dashboardIntervalMs).coerceAtLeast(0),
                        onSelect = { viewModel.setDashboardInterval(intervals[it]) },
                    )
                    Spacer(Modifier.height(Space.s))
                    Text(
                        text = "Sampling stops when the monitor is off screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.TextDim,
                    )
                }

                Spacer(Modifier.height(Space.xl))
                SectionLabel("Permissions")
                Spacer(Modifier.height(Space.s))
                Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
                    state.capabilities.forEach { capability ->
                        CapabilityRow(
                            capability = capability,
                            onAction = { viewModel.openCapabilitySettings(capability.id) },
                        )
                    }
                }

                Spacer(Modifier.height(Space.xl))
                SectionLabel("About")
                Spacer(Modifier.height(Space.s))
                ChamferPanel {
                    InfoRow("Application", "PLAYCORE")
                    InfoRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    InfoRow("Package", "app.playcore")
                    InfoRow("Licence", "Apache License 2.0")
                    InfoRow("Data", "Stored on this device only")
                    Spacer(Modifier.height(Space.m))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AngularButton(label = "Licences", onClick = onOpenLicenses)
                    }
                }
                Spacer(Modifier.height(Space.xxl))
            }
        }
    }
}
