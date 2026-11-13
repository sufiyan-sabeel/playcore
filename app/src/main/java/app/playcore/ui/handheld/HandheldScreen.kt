package app.playcore.ui.handheld

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
import androidx.compose.ui.Alignment
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
import app.playcore.ui.components.AngularButton
import app.playcore.ui.components.AppIcon
import app.playcore.ui.components.BadgeTone
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.components.SectionLabel
import app.playcore.ui.components.StatusBadge
import app.playcore.ui.explore.formatPlaytime
import app.playcore.ui.system.SystemViewModel

/**
 * Handheld lobby. Evidence level: INFERRED — every label is verified
 * (gs_hh_*: "Your lobby is empty", "Game profile", "Per-game loadout",
 * "Not played yet", "Start"), the landscape composition is not.
 */
@Composable
fun HandheldScreen(
    onOpenGame: (String) -> Unit,
    onOpenSuperBase: () -> Unit,
    onOpenLibrary: () -> Unit,
    modeBar: @Composable () -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    ConsoleBackground(state.settings.backdrop, state.settings.vignette) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReferenceHeader(
                title = "Handheld",
                subtitle = "PLAYCORE / LANDSCAPE LOBBY",
                trailing = {
                    AngularButton(label = "Super Base", onClick = onOpenSuperBase)
                },
            )
            modeBar()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                if (state.games.isEmpty()) {
                    EmptyState(
                        title = "Your lobby is empty",
                        message = "Add games to the console and they line up here, ready to start.",
                        glyph = "+",
                        actionLabel = "Add game",
                        onAction = onOpenLibrary,
                    )
                } else {
                    SectionLabel("Lobby")
                    Spacer(Modifier.height(Space.s))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Space.m),
                        contentPadding = PaddingValues(end = Space.l),
                    ) {
                        items(state.games, key = { it.packageName }) { game ->
                            LobbyCard(game = game, onClick = { onOpenGame(game.packageName) })
                        }
                    }

                    Spacer(Modifier.height(Space.xl))
                    SectionLabel("Loadout")
                    Spacer(Modifier.height(Space.s))
                    BanneredPanel(title = "Per-game loadout") {
                        Text(
                            text = "Each game carries its own overlay style, panel cards, refresh rate and " +
                                "Do Not Disturb preference. Open a game to edit its profile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Console.TextDim,
                        )
                        Spacer(Modifier.height(Space.m))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                            StatusBadge("Crosshair", BadgeTone.NEUTRAL)
                            StatusBadge("Refresh rate", BadgeTone.NEUTRAL)
                            StatusBadge("Game profile", BadgeTone.READY)
                        }
                    }
                }
                Spacer(Modifier.height(Space.xxl))
            }
        }
    }
}

@Composable
private fun LobbyCard(game: Game, onClick: () -> Unit) {
    ChamferPanel(modifier = Modifier.width(190.dp), onClick = onClick) {
        AppIcon(packageName = game.packageName, size = 52.dp)
        Spacer(Modifier.height(Space.s))
        Text(
            text = game.label,
            style = MaterialTheme.typography.titleLarge,
            color = Console.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (game.totalPlayMs > 0L) formatPlaytime(game.totalPlayMs) else "Not played yet",
            style = MaterialTheme.typography.labelSmall,
            color = Console.TextDim,
        )
        Spacer(Modifier.height(Space.m))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge("Start", BadgeTone.READY)
        }
    }
}
