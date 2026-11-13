package app.playcore.ui.details

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.core.model.CubeMode
import app.playcore.core.model.LaunchResult
import app.playcore.core.model.OverlayStyle
import app.playcore.overlay.label
import app.playcore.ui.components.AngularButton
import app.playcore.ui.components.AngularTabs
import app.playcore.ui.components.AngularToggle
import app.playcore.ui.components.AppIcon
import app.playcore.ui.components.BadgeTone
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.InfoRow
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.components.SectionLabel
import app.playcore.ui.components.StatusBadge
import app.playcore.ui.explore.formatPlaytime
import app.playcore.ui.system.SystemViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GameDetailScreen(
    onBack: () -> Unit,
    viewModel: GameDetailViewModel = viewModel(),
    systemViewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val system by systemViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val game = state.game
    val profile = state.profile

    ConsoleBackground(system.settings.backdrop, system.settings.vignette) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReferenceHeader(
                title = game?.label ?: "Game",
                subtitle = "PLAYCORE / GAME PROFILE",
                onBack = onBack,
            )

            if (game == null) {
                EmptyState(
                    title = "Not in the library",
                    message = "This entry was removed. Add it again from the library browser.",
                    actionLabel = "Go back",
                    onAction = onBack,
                )
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(packageName = game.packageName, size = 56.dp)
                    Spacer(Modifier.width(Space.m))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.packageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Console.TextDim,
                        )
                        Text(
                            text = formatPlaytime(game.totalPlayMs) + " recorded",
                            style = MaterialTheme.typography.bodySmall,
                            color = Console.TextDim,
                        )
                    }
                    AngularToggle(checked = game.isFavorite, onCheckedChange = { viewModel.toggleFavorite() })
                }

                Spacer(Modifier.height(Space.l))

                Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                    AngularButton(
                        label = if (game.isInstalled) "Launch" else "Not installed",
                        filled = true,
                        enabled = game.isInstalled,
                        onClick = viewModel::launch,
                    )
                    StatusBadge(
                        text = if (game.isInstalled) "Installed" else "Missing",
                        tone = if (game.isInstalled) BadgeTone.READY else BadgeTone.BLOCKED,
                    )
                }

                when (state.lastLaunchResult) {
                    LaunchResult.NotInstalled -> Text(
                        text = "The app is no longer installed on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.Blocked,
                    )
                    LaunchResult.NoLaunchIntent -> Text(
                        text = "Android did not return a launcher intent for this package.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.Blocked,
                    )
                    LaunchResult.Launched, null -> Unit
                }

                Spacer(Modifier.height(Space.xl))

                if (profile != null) {
                    BanneredPanel(title = "Loadout") {
                        SectionLabel("Overlay style")
                        Spacer(Modifier.height(Space.s))
                        val styles = OverlayStyle.entries
                        AngularTabs(
                            options = styles.map { it.label() },
                            selectedIndex = styles.indexOf(profile.overlayStyle).coerceAtLeast(0),
                            onSelect = { viewModel.setOverlayStyle(styles[it]) },
                        )

                        Spacer(Modifier.height(Space.m))
                        SectionLabel("Panel mode")
                        Spacer(Modifier.height(Space.s))
                        val modes = CubeMode.entries
                        AngularTabs(
                            options = modes.map { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                            selectedIndex = modes.indexOf(profile.cubeMode).coerceAtLeast(0),
                            onSelect = { viewModel.setCubeMode(modes[it]) },
                        )

                        Spacer(Modifier.height(Space.m))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Do Not Disturb", style = MaterialTheme.typography.titleMedium, color = Console.Text)
                                Text(
                                    text = "Needs DND access; stored either way",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Console.TextDim,
                                )
                            }
                            AngularToggle(checked = profile.dndEnabled, onCheckedChange = viewModel::setDnd)
                        }
                        Spacer(Modifier.height(Space.m))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Brightness lock", style = MaterialTheme.typography.titleMedium, color = Console.Text)
                                Text(
                                    text = "Needs write access; stored either way",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Console.TextDim,
                                )
                            }
                            AngularToggle(checked = profile.brightnessLock, onCheckedChange = viewModel::setBrightnessLock)
                        }

                        Spacer(Modifier.height(Space.m))
                        SectionLabel("Refresh rate")
                        Spacer(Modifier.height(Space.s))
                        val rates = listOf<Int?>(null, 60, 90, 120)
                        AngularTabs(
                            options = rates.map { it?.let { hz -> "${hz}Hz" } ?: "Default" },
                            selectedIndex = rates.indexOf(profile.refreshRateHz).coerceAtLeast(0),
                            onSelect = { viewModel.setRefreshRate(rates[it]) },
                        )
                        Spacer(Modifier.height(Space.s))
                        Text(
                            text = "${profile.enabledCards.size} panel cards selected for this game.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Console.TextFaint,
                        )
                    }

                    Spacer(Modifier.height(Space.l))

                    SectionLabel("Category")
                    Spacer(Modifier.height(Space.s))
                    ChamferPanel {
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                            AngularButton(
                                label = "None",
                                filled = game.categoryId == null,
                                onClick = { viewModel.setCategory(null) },
                            )
                            state.categories.take(2).forEach { category ->
                                AngularButton(
                                    label = category.name,
                                    filled = game.categoryId == category.id,
                                    onClick = { viewModel.setCategory(category.id) },
                                )
                            }
                        }
                        Spacer(Modifier.height(Space.m))
                        AngularButton(
                            label = "New category",
                            onClick = { viewModel.createCategory("Category ${state.categories.size + 1}") },
                        )
                    }
                }

                Spacer(Modifier.height(Space.l))

                SectionLabel("Information")
                Spacer(Modifier.height(Space.s))
                ChamferPanel {
                    InfoRow("Package", game.packageName)
                    InfoRow("Installed", if (game.isInstalled) "Yes" else "No")
                    InfoRow("Added", formatTimestamp(game.addedAt))
                    InfoRow(
                        "Last launched",
                        game.lastLaunchedAt?.let { formatTimestamp(it) } ?: "Never launched from PLAYCORE",
                    )
                    InfoRow("Playtime", formatPlaytime(game.totalPlayMs))
                    InfoRow("Favourite", if (game.isFavorite) "Yes" else "No")
                }

                Spacer(Modifier.height(Space.l))

                AngularButton(
                    label = "Remove from console",
                    onClick = {
                        viewModel.removeFromLibrary {
                            Toast.makeText(context, "Removed from the console", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                )
                Spacer(Modifier.height(Space.s))
                Text(
                    text = "Removing an entry never uninstalls the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Console.TextFaint,
                )
                Spacer(Modifier.height(Space.xxl))
            }
        }
    }
}

private fun formatTimestamp(value: Long): String {
    if (value <= 0L) return "Unknown"
    return SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(value))
}
