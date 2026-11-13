package app.playcore.ui.home

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.PlaycoreSpacing
import app.playcore.core.model.Capability
import app.playcore.core.model.Game
import app.playcore.ui.components.AppIcon
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.PlaycoreCard
import app.playcore.ui.components.SectionHeader
import app.playcore.ui.components.StatusPill

@Composable
fun HomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenGame: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshCapabilities()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlaycoreSpacing.l)
            .padding(top = PlaycoreSpacing.xl, bottom = PlaycoreSpacing.xxl),
    ) {
        Text(
            text = "PLAYCORE",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Your games, your space. Everything here stays on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(PlaycoreSpacing.xl))

        Row(horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m)) {
            StatBlock(
                label = "In library",
                value = state.libraryCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatBlock(
                label = "Favourites",
                value = state.favorites.size.toString(),
                modifier = Modifier.weight(1f),
            )
            StatBlock(
                label = "Recent",
                value = state.recent.size.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(PlaycoreSpacing.xl))

        SectionHeader(
            title = "Continue playing",
            subtitle = if (state.recent.isEmpty()) "Launch a game once and it appears here" else null,
        )
        Spacer(Modifier.height(PlaycoreSpacing.m))

        if (state.recent.isEmpty()) {
            EmptyState(
                title = "No sessions yet",
                message = "PLAYCORE records a launch timestamp when you start a game from the library.",
                actionLabel = "Open library",
                onAction = onOpenLibrary,
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m),
                contentPadding = PaddingValues(end = PlaycoreSpacing.l),
            ) {
                items(state.recent, key = { it.packageName }) { game ->
                    RecentGameCard(game = game, onClick = { onOpenGame(game.packageName) })
                }
            }
        }

        Spacer(Modifier.height(PlaycoreSpacing.xl))

        SectionHeader(
            title = "Needs attention",
            subtitle = "Optional features waiting on a permission or a backend",
        )
        Spacer(Modifier.height(PlaycoreSpacing.m))

        if (state.needsAttention.isEmpty()) {
            PlaycoreCard {
                Text(
                    text = "Nothing is missing. Every capability PLAYCORE reports is currently available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m)) {
                state.needsAttention.take(3).forEach { capability ->
                    AttentionRow(capability = capability, onOpen = onOpenTools)
                }
            }
        }

        Spacer(Modifier.height(PlaycoreSpacing.xl))

        SectionHeader(title = "Device")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        PlaycoreCard(onClick = onOpenDashboard) {
            Text(
                text = "Open the live dashboard",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Real readings from Android, and an explicit reason wherever a value is not available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    PlaycoreCard(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecentGameCard(game: Game, onClick: () -> Unit) {
    PlaycoreCard(modifier = Modifier.width(150.dp), onClick = onClick) {
        AppIcon(packageName = game.packageName, size = 40.dp)
        Spacer(Modifier.height(PlaycoreSpacing.s))
        Text(
            text = game.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (game.isInstalled) "Installed" else "Not installed",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AttentionRow(capability: Capability, onOpen: () -> Unit) {
    PlaycoreCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = capability.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(PlaycoreSpacing.xxs))
                Text(
                    text = capability.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(PlaycoreSpacing.m))
            StatusPill(status = capability.status)
        }
        Spacer(Modifier.height(PlaycoreSpacing.m))
        OutlinedButton(onClick = onOpen) { Text("Review in Tools") }
    }
}
