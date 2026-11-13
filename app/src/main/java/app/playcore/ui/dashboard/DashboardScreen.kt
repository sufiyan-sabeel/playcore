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
import app.playcore.core.designsystem.PlaycoreSpacing
import app.playcore.ui.components.LoadingState
import app.playcore.ui.components.MetricTile
import app.playcore.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshot = state.snapshot

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PlaycoreSpacing.l, vertical = PlaycoreSpacing.l),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionHeader(
                    title = "Dashboard",
                    subtitle = "Readings straight from Android, refreshed every ${state.intervalMs / 1000}s",
                )
            }
            Spacer(Modifier.width(PlaycoreSpacing.s))
            IconButton(onClick = viewModel::refreshNow) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh now",
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        if (snapshot == null) {
            LoadingState(message = "Reading device telemetry")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = PlaycoreSpacing.l,
                    end = PlaycoreSpacing.l,
                    bottom = PlaycoreSpacing.xxl,
                ),
                horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m),
                verticalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m),
            ) {
                items(snapshot.metrics, key = { it.label() }) { metric ->
                    MetricTile(metric = metric)
                }
            }
            Text(
                text = "Captured ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(snapshot.capturedAt))}. " +
                    "Values marked unavailable were refused by the platform, not skipped by PLAYCORE.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = PlaycoreSpacing.l),
            )
            Spacer(Modifier.height(PlaycoreSpacing.s))
        }
    }
}

private fun app.playcore.core.model.Metric.label(): String = when (this) {
    is app.playcore.core.model.Metric.Value -> label
    is app.playcore.core.model.Metric.Unavailable -> label
}
