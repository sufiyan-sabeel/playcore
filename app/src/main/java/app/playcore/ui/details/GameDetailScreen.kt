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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.PlaycoreSpacing
import app.playcore.core.model.LaunchResult
import app.playcore.ui.components.AppIcon
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.InfoRow
import app.playcore.ui.components.PlaycoreCard
import app.playcore.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GameDetailScreen(
    onBack: () -> Unit,
    viewModel: GameDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val game = state.game

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PlaycoreSpacing.s, vertical = PlaycoreSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Game details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

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
                .padding(horizontal = PlaycoreSpacing.l)
                .padding(bottom = PlaycoreSpacing.xxl),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(packageName = game.packageName, size = 64.dp)
                Spacer(Modifier.width(PlaycoreSpacing.m))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.label,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = game.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = viewModel::toggleFavorite) {
                    Icon(
                        imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Toggle favourite",
                        tint = if (game.isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(PlaycoreSpacing.l))

            Button(
                onClick = {
                    viewModel.launch()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = game.isInstalled,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(PlaycoreSpacing.s))
                Text(if (game.isInstalled) "Launch" else "Not installed")
            }

            when (val result = state.lastLaunchResult) {
                LaunchResult.NotInstalled -> {
                    Spacer(Modifier.height(PlaycoreSpacing.s))
                    Text(
                        text = "The app is no longer installed on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                LaunchResult.NoLaunchIntent -> {
                    Spacer(Modifier.height(PlaycoreSpacing.s))
                    Text(
                        text = "Android did not return a launcher intent for this package.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                LaunchResult.Launched, null -> Unit
            }

            Spacer(Modifier.height(PlaycoreSpacing.xl))

            SectionHeader(title = "Category")
            Spacer(Modifier.height(PlaycoreSpacing.m))
            PlaycoreCard {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FilterChip(
                        selected = game.categoryId == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("None") },
                    )
                    state.categories.take(2).forEach { category ->
                        FilterChip(
                            selected = game.categoryId == category.id,
                            onClick = { viewModel.setCategory(category.id) },
                            label = { Text(category.name) },
                        )
                    }
                }
                Spacer(Modifier.height(PlaycoreSpacing.m))
                OutlinedButton(
                    onClick = {
                        viewModel.createCategory("Favourites ${state.categories.size + 1}")
                    },
                ) { Text("New category") }
            }

            Spacer(Modifier.height(PlaycoreSpacing.xl))

            SectionHeader(title = "Information")
            Spacer(Modifier.height(PlaycoreSpacing.m))
            PlaycoreCard {
                InfoRow(label = "Package", value = game.packageName)
                InfoRow(label = "Installed", value = if (game.isInstalled) "Yes" else "No")
                InfoRow(
                    label = "Added",
                    value = formatTimestamp(game.addedAt),
                )
                InfoRow(
                    label = "Last launched",
                    value = game.lastLaunchedAt?.let { formatTimestamp(it) } ?: "Never launched from PLAYCORE",
                )
                InfoRow(label = "Favourite", value = if (game.isFavorite) "Yes" else "No")
            }

            Spacer(Modifier.height(PlaycoreSpacing.xl))

            OutlinedButton(
                onClick = {
                    viewModel.removeFromLibrary {
                        Toast.makeText(context, "Removed from library", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Remove from library (app stays installed)")
            }
        }
    }
}

private fun formatTimestamp(value: Long): String {
    if (value <= 0L) return "Unknown"
    return SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(value))
}
