package app.playcore.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.PlaycoreSpacing
import app.playcore.core.model.Metric
import app.playcore.ui.components.InfoRow
import app.playcore.ui.components.PermissionCard
import app.playcore.ui.components.PlaycoreCard
import app.playcore.ui.components.SectionHeader

@Composable
fun ToolsScreen(
    onOpenLicenses: () -> Unit,
    viewModel: ToolsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlaycoreSpacing.l)
            .padding(top = PlaycoreSpacing.l, bottom = PlaycoreSpacing.xxl),
    ) {
        SectionHeader(
            title = "Tools",
            subtitle = "Diagnostics and the capability report for this device",
        )

        Spacer(Modifier.height(PlaycoreSpacing.l))

        SectionHeader(title = "Capability report", subtitle = "What PLAYCORE can and cannot do here")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        Column(verticalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m)) {
            state.capabilities.forEach { capability ->
                PermissionCard(
                    capability = capability,
                    onAction = { viewModel.openCapabilitySettings(capability.id) },
                )
            }
        }

        Spacer(Modifier.height(PlaycoreSpacing.xl))

        SectionHeader(title = "Device", subtitle = "Read from the platform a moment ago")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        PlaycoreCard {
            val metrics = state.deviceMetrics
            if (metrics.isEmpty()) {
                Text(
                    text = "No readings were returned.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                metrics.forEach { metric ->
                    when (metric) {
                        is Metric.Value -> InfoRow(
                            label = metric.label,
                            value = metric.detail?.let { "${metric.value} · $it" } ?: metric.value,
                        )

                        is Metric.Unavailable -> InfoRow(
                            label = metric.label,
                            value = "Unavailable — ${metric.reason}",
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(PlaycoreSpacing.xl))

        SectionHeader(title = "Library health")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        PlaycoreCard {
            InfoRow(label = "Entries in library", value = state.libraryGames.size.toString())
            InfoRow(
                label = "Marked not installed",
                value = state.libraryGames.count { !it.isInstalled }.toString(),
            )
            InfoRow(
                label = "Favourites",
                value = state.libraryGames.count { it.isFavorite }.toString(),
            )
        }

        Spacer(Modifier.height(PlaycoreSpacing.xl))

        SectionHeader(title = "About")
        Spacer(Modifier.height(PlaycoreSpacing.m))
        PlaycoreCard {
            Text(
                text = "PLAYCORE is an independent, local-first Android gaming launcher. It has no account, no advertising SDK and no analytics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlaycoreSpacing.m))
            OutlinedButton(onClick = onOpenLicenses) { Text("Open-source licences") }
        }
    }
}
