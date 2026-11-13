package app.playcore.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.core.model.Metric
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.LoadingState
import app.playcore.ui.components.MetricTile
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.system.SystemViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    modeBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: DashboardViewModel = viewModel(),
    systemViewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val system by systemViewModel.uiState.collectAsStateWithLifecycle()
    val snapshot = state.snapshot

    ConsoleBackground(system.settings.backdrop, system.settings.vignette) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReferenceHeader(
                title = "Monitor",
                subtitle = "PLAYCORE / LIVE DEVICE READINGS",
                trailing = {
                    IconButton(onClick = viewModel::refreshNow) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh now", tint = Console.TextDim)
                    }
                },
            )
            modeBar()

            if (snapshot == null) {
                LoadingState("Reading device telemetry")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = Space.l, end = Space.l, bottom = Space.l),
                    horizontalArrangement = Arrangement.spacedBy(Space.m),
                    verticalArrangement = Arrangement.spacedBy(Space.m),
                ) {
                    items(snapshot.metrics, key = { it.metricKey() }) { metric ->
                        MetricTile(metric = metric)
                    }
                }
                Text(
                    text = "Captured ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(snapshot.capturedAt))}. " +
                        "Values marked unavailable were refused by the platform, not skipped by PLAYCORE.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Console.TextFaint,
                    modifier = Modifier.padding(horizontal = Space.l, vertical = Space.s),
                )
            }
            bottomBar()
        }
    }
}

private fun Metric.metricKey(): String = when (this) {
    is Metric.Value -> label
    is Metric.Unavailable -> label
}
