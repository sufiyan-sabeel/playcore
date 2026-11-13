package app.playcore.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.PlaycoreSpacing
import app.playcore.core.designsystem.accentColor
import app.playcore.core.model.AccentChoice
import app.playcore.core.model.LayoutMode
import app.playcore.core.model.LibrarySort
import app.playcore.core.model.ThemeMode
import app.playcore.ui.components.InfoRow
import app.playcore.ui.components.PermissionCard
import app.playcore.ui.components.PlaycoreCard
import app.playcore.ui.components.SectionHeader

@Composable
fun SettingsScreen(
    onOpenLicenses: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshCapabilities() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlaycoreSpacing.l)
            .padding(top = PlaycoreSpacing.l, bottom = PlaycoreSpacing.xxl),
    ) {
        SectionHeader(title = "Settings", subtitle = "Every control here changes real behaviour")

        Spacer(Modifier.height(PlaycoreSpacing.l))

        SectionHeader(title = "Appearance")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        PlaycoreCard {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(PlaycoreSpacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(mode.label()) },
                    )
                }
            }

            Spacer(Modifier.height(PlaycoreSpacing.m))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AMOLED black",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Uses pure black surfaces in dark mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.amoled,
                    onCheckedChange = { viewModel.setAmoled(it) },
                )
            }

            Spacer(Modifier.height(PlaycoreSpacing.m))
            Text(
                text = "Accent",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(PlaycoreSpacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m)) {
                AccentChoice.entries.forEach { accent ->
                    val selected = settings.accent == accent
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentColor(accent))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            )
                            .clickable { viewModel.setAccent(accent) },
                    )
                }
            }
        }

        Spacer(Modifier.height(PlaycoreSpacing.l))

        SectionHeader(title = "Library")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        PlaycoreCard {
            Text(
                text = "Default sort",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(PlaycoreSpacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s)) {
                LibrarySort.entries.forEach { sort ->
                    FilterChip(
                        selected = settings.librarySort == sort,
                        onClick = { viewModel.setLibrarySort(sort) },
                        label = { Text(sort.label()) },
                    )
                }
            }
            Spacer(Modifier.height(PlaycoreSpacing.m))
            Text(
                text = "Default layout",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(PlaycoreSpacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s)) {
                LayoutMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.layout == mode,
                        onClick = { viewModel.setLayout(mode) },
                        label = { Text(mode.label()) },
                    )
                }
            }
        }

        Spacer(Modifier.height(PlaycoreSpacing.l))

        SectionHeader(title = "Dashboard")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        PlaycoreCard {
            Text(
                text = "Refresh interval",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Sampling stops when the dashboard is off screen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlaycoreSpacing.s))
            Row(horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s)) {
                listOf(1_000L, 2_000L, 5_000L).forEach { interval ->
                    FilterChip(
                        selected = settings.dashboardIntervalMs == interval,
                        onClick = { viewModel.setDashboardInterval(interval) },
                        label = { Text("${interval / 1000}s") },
                    )
                }
            }
        }

        Spacer(Modifier.height(PlaycoreSpacing.l))

        SectionHeader(
            title = "Permissions",
            subtitle = "Requested only when a feature needs them",
        )
        Spacer(Modifier.height(PlaycoreSpacing.m))
        Column(verticalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m)) {
            state.capabilities.forEach { capability ->
                PermissionCard(
                    capability = capability,
                    onAction = { viewModel.openCapabilitySettings(capability.id) },
                )
            }
        }

        Spacer(Modifier.height(PlaycoreSpacing.l))

        SectionHeader(title = "About")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        PlaycoreCard {
            InfoRow(label = "Application", value = "PLAYCORE")
            InfoRow(label = "Version", value = "${state.versionName} (${state.versionCode})")
            InfoRow(label = "Package", value = "app.playcore")
            InfoRow(label = "Licence", value = "Apache License 2.0")
            InfoRow(label = "Data", value = "Stored on this device only")
            Spacer(Modifier.height(PlaycoreSpacing.m))
            OutlinedButton(onClick = onOpenLicenses) { Text("Open-source licences") }
        }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.DARK -> "Dark"
    ThemeMode.LIGHT -> "Light"
}

private fun LibrarySort.label(): String = when (this) {
    LibrarySort.RECENT -> "Recent"
    LibrarySort.NAME -> "Name"
    LibrarySort.FAVORITES_FIRST -> "Favourites"
}

private fun LayoutMode.label(): String = when (this) {
    LayoutMode.GRID -> "Grid"
    LayoutMode.LIST -> "List"
}
