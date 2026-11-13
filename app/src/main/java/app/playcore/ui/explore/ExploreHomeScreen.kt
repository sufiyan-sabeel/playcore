package app.playcore.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.core.model.Game
import app.playcore.overlay.label
import app.playcore.ui.components.AngularButton
import app.playcore.ui.components.AppIcon
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.components.SectionLabel
import app.playcore.ui.components.StatusBadge
import app.playcore.ui.components.BadgeTone
import app.playcore.ui.navigation.HeaderGear
import app.playcore.ui.system.SystemViewModel

/**
 * Explore home. Evidence level: INFERRED — the destination, its purpose and every
 * label are verified from the reference string table, the composition is not.
 */
@Composable
fun ExploreHomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenMonitor: () -> Unit,
    onOpenSuperBase: () -> Unit,
    onOpenGame: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
    modeBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    ConsoleBackground(
        backdrop = state.settings.backdrop,
        vignette = state.settings.vignette,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReferenceHeader(
                title = "PLAYCORE",
                subtitle = "PLAYCORE / GAME SYSTEM",
                trailing = { HeaderGear(onOpenSettings) },
            )
            modeBar()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                BanneredPanel(title = "Session") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.m)) {
                        Stat("In library", state.games.size.toString(), Modifier.weight(1f))
                        Stat("Playtime", formatPlaytime(state.totalPlayMs), Modifier.weight(1f))
                        Stat("Rank", state.rank, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(Space.m))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                        AngularButton("Add game", filled = true, onClick = onOpenLibrary)
                        AngularButton("Super Base", onClick = onOpenSuperBase)
                    }
                }

                Spacer(Modifier.height(Space.xl))

                SectionLabel("Continue playing")
                Spacer(Modifier.height(Space.s))
                val recent = state.games
                    .filter { it.lastLaunchedAt != null }
                    .sortedByDescending { it.lastLaunchedAt }
                    .take(8)
                if (recent.isEmpty()) {
                    EmptyState(
                        title = "No sessions yet",
                        message = "Launch a game from the library and it appears here.",
                        actionLabel = "Open library",
                        onAction = onOpenLibrary,
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Space.m),
                        contentPadding = PaddingValues(end = Space.l),
                    ) {
                        items(recent, key = { it.packageName }) { game ->
                            RecentCard(game = game, onClick = { onOpenGame(game.packageName) })
                        }
                    }
                }

                Spacer(Modifier.height(Space.xl))

                SectionLabel("Console")
                Spacer(Modifier.height(Space.s))
                ChamferPanel {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Overlay panel",
                                style = MaterialTheme.typography.titleMedium,
                                color = Console.Text,
                            )
                            Text(
                                text = state.settings.overlayStyle.label(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Console.TextDim,
                            )
                        }
                        if (state.overlayPermission) {
                            StatusBadge("Ready", BadgeTone.READY)
                        } else {
                            StatusBadge("Permission required", BadgeTone.PREVIEW)
                        }
                    }
                    Spacer(Modifier.height(Space.m))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                        AngularButton("Monitor", onClick = onOpenMonitor)
                        AngularButton("Help", onClick = onOpenHelp)
                    }
                }

                Spacer(Modifier.height(Space.xxl))
            }
            bottomBar()
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Console.VioletLight)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Console.TextDim)
    }
}

@Composable
private fun RecentCard(game: Game, onClick: () -> Unit) {
    ChamferPanel(modifier = Modifier.width(150.dp), onClick = onClick) {
        AppIcon(packageName = game.packageName, size = 38.dp)
        Spacer(Modifier.height(Space.s))
        Text(
            text = game.label,
            style = MaterialTheme.typography.titleMedium,
            color = Console.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = formatPlaytime(game.totalPlayMs),
            style = MaterialTheme.typography.labelSmall,
            color = Console.TextDim,
        )
    }
}

internal fun formatPlaytime(ms: Long): String {
    if (ms <= 0L) return "0m"
    val minutes = ms / 60_000L
    return when {
        minutes < 60 -> "${minutes}m"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
