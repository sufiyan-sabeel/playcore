package app.playcore.ui.superbase

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
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.core.model.Capability
import app.playcore.core.model.CapabilityId
import app.playcore.core.model.CapabilityStatus
import app.playcore.core.model.CubeMode
import app.playcore.core.model.Game
import app.playcore.core.model.ReferenceContent
import app.playcore.ui.components.AngularButton
import app.playcore.ui.components.AngularTabs
import app.playcore.ui.components.AngularToggle
import app.playcore.ui.components.BadgeTone
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.CapabilityRow
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.InfoRow
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.components.SectionLabel
import app.playcore.ui.components.StatusBadge
import app.playcore.ui.explore.formatPlaytime
import app.playcore.ui.system.SystemUiState
import app.playcore.ui.system.SystemViewModel

@Composable
fun TileScreen(
    tileId: String,
    onBack: () -> Unit,
    onOpenGame: (String) -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tile = SuperBaseTile.byId(tileId) ?: SuperBaseTile.GAME_CENTER

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    ConsoleBackground(state.settings.backdrop, state.settings.vignette) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReferenceHeader(
                title = tile.title,
                subtitle = "PLAYCORE / ${tile.subtitle}",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                when (tile) {
                    SuperBaseTile.GAME_CENTER -> GameCenterTile(state, onOpenGame)
                    SuperBaseTile.GAME_LIBRARY -> GameLibraryTile(state, onOpenGame)
                    SuperBaseTile.PLUGIN_LIBRARY -> PluginLibraryTile(state)
                    SuperBaseTile.CONTROL_PANEL -> ControlPanelTile(state, viewModel)
                    SuperBaseTile.CUSTOM_MODE -> CustomModeTile(state)
                    SuperBaseTile.ATMOSPHERE -> AtmosphereTile(state, viewModel)
                    SuperBaseTile.CORE_REACTOR -> CoreReactorTile(state, viewModel)
                    SuperBaseTile.ROOT_TOOLS -> RootToolsTile(state, viewModel)
                }
                Spacer(Modifier.height(Space.xxl))
            }
        }
    }
}

@Composable
private fun GameCenterTile(state: SystemUiState, onOpenGame: (String) -> Unit) {
    BanneredPanel(title = "Career") {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.m)) {
            Metric("Playtime", formatPlaytime(state.totalPlayMs), Modifier.weight(1f))
            Metric("Rank", state.rank, Modifier.weight(1f))
            Metric("Games", state.games.size.toString(), Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(Space.xl))
    SectionLabel("Rank ladder")
    Spacer(Modifier.height(Space.s))
    ChamferPanel {
        ReferenceContent.ranks.forEach { rank ->
            val current = rank == state.rank
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rank,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (current) Console.VioletLight else Console.TextDim,
                    modifier = Modifier.weight(1f),
                )
                if (current) StatusBadge("You", BadgeTone.READY)
            }
        }
        Spacer(Modifier.height(Space.s))
        Text(
            text = "Ranks follow total playtime recorded by PLAYCORE: 1h, 5h, 20h, 50h.",
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
    }
    Spacer(Modifier.height(Space.xl))
    SectionLabel("Most played")
    Spacer(Modifier.height(Space.s))
    val played = state.games.filter { it.totalPlayMs > 0L }.sortedByDescending { it.totalPlayMs }.take(5)
    if (played.isEmpty()) {
        EmptyState(
            title = "Not played yet",
            message = "Playtime is recorded when you launch a game from PLAYCORE.",
        )
    } else {
        ChamferPanel {
            played.forEach { game ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Space.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = game.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Console.Text,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatPlaytime(game.totalPlayMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Console.TextDim,
                    )
                }
            }
        }
    }
}

@Composable
private fun GameLibraryTile(state: SystemUiState, onOpenGame: (String) -> Unit) {
    BanneredPanel(title = "Library") {
        Text(
            text = "${state.games.size} games on the console, ${state.profiles.size} with a saved profile.",
            style = MaterialTheme.typography.bodyMedium,
            color = Console.TextDim,
        )
    }
    Spacer(Modifier.height(Space.xl))
    if (state.games.isEmpty()) {
        EmptyState(
            title = "Your lobby is empty",
            message = "Add games from the Library tab; PLAYCORE never guesses which apps are games.",
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
            state.games.forEach { game ->
                ChamferPanel(onClick = { onOpenGame(game.packageName) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(game.label, style = MaterialTheme.typography.titleMedium, color = Console.Text)
                            Text(
                                text = game.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Console.TextDim,
                            )
                        }
                        Text(
                            text = formatPlaytime(game.totalPlayMs),
                            style = MaterialTheme.typography.bodySmall,
                            color = Console.TextDim,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PluginLibraryTile(state: SystemUiState) {
    val rootReady = state.capabilities
        .firstOrNull { it.id == CapabilityId.ROOT }?.status == CapabilityStatus.AVAILABLE

    BanneredPanel(title = "Plugin library") {
        Text(
            text = "The twenty plugins the reference application ships. Each one lists the access it needs; " +
                "none is switched on in this build, and no plugin reports success it cannot deliver.",
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
    }
    Spacer(Modifier.height(Space.xl))
    Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
        ReferenceContent.plugins.forEach { plugin ->
            ChamferPanel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Console.Text,
                        modifier = Modifier.weight(1f),
                    )
                    if (plugin.requiresPrivilegedAccess) {
                        StatusBadge(if (rootReady) "Ready" else "Root required",
                            if (rootReady) BadgeTone.READY else BadgeTone.PREVIEW)
                    } else {
                        StatusBadge("Ready", BadgeTone.READY)
                    }
                }
                Spacer(Modifier.height(Space.xxs))
                Text(
                    text = plugin.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Console.TextDim,
                )
            }
        }
    }
}

@Composable
private fun ControlPanelTile(state: SystemUiState, viewModel: SystemViewModel) {
    val groups = listOf(
        "Display" to "Brightness lock, screen mode, saturation",
        "GPU" to "Frequency floor and renderer preference",
        "Network" to "Wi-Fi low-latency lock, mobile data standby",
        "Audio" to "Gaming sound profile: standard, bass, voice, footstep",
        "Functions" to "Recording quality, panel shortcuts, crosshair",
        "Game Mode" to "Performance game mode request",
        "Screen Refresh Rate" to "Preferred refresh rate per session",
    )
    BanneredPanel(title = "Global profile") {
        Text(
            text = "These groups exist in the reference. Writing them needs a privileged shell and a device " +
                "that exposes the interface, so PLAYCORE shows the group and the requirement instead of a switch " +
                "that would do nothing.",
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
    }
    Spacer(Modifier.height(Space.xl))
    Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
        groups.forEach { (name, detail) ->
            ChamferPanel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = MaterialTheme.typography.titleMedium, color = Console.Text,
                        modifier = Modifier.weight(1f))
                    StatusBadge("Backend required", BadgeTone.PREVIEW)
                }
                Spacer(Modifier.height(Space.xxs))
                Text(detail, style = MaterialTheme.typography.bodySmall, color = Console.TextDim)
            }
        }
    }
    Spacer(Modifier.height(Space.l))
    SectionLabel("Permissions")
    Spacer(Modifier.height(Space.s))
    state.capabilities.take(4).forEach { capability ->
        CapabilityRow(
            capability = capability,
            modifier = Modifier.padding(bottom = Space.s),
            onAction = { viewModel.openCapabilitySettings(capability.id) },
        )
    }
}

@Composable
private fun CustomModeTile(state: SystemUiState) {
    val rootReady = state.capabilities
        .firstOrNull { it.id == CapabilityId.ROOT }?.status == CapabilityStatus.AVAILABLE
    BanneredPanel(title = "Custom mode") {
        Text(
            text = if (rootReady) {
                "Root is present. Writing governor and frequency limits is not enabled in this build, so these rows stay read-only."
            } else {
                "Editing CPU and GPU limits needs Root or Shizuku. Without it PLAYCORE stores nothing and writes nothing."
            },
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
    }
    Spacer(Modifier.height(Space.xl))
    ChamferPanel {
        InfoRow("CPU governor", "Not readable without a privileged shell")
        InfoRow("Minimum frequency", "Not readable")
        InfoRow("Maximum frequency", "Not readable")
        InfoRow("GPU frequency floor", "Not readable")
        InfoRow("CPU scope", "All clusters")
        Spacer(Modifier.height(Space.s))
        StatusBadge(if (rootReady) "Root detected" else "Root required",
            if (rootReady) BadgeTone.READY else BadgeTone.PREVIEW)
    }
}

@Composable
private fun AtmosphereTile(state: SystemUiState, viewModel: SystemViewModel) {
    BanneredPanel(title = "Atmosphere") {
        Text(
            text = "Backdrop, particles, vignette and edge lighting. The backdrop and vignette apply to PLAYCORE " +
                "immediately; edge lighting needs hardware PLAYCORE cannot detect yet.",
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
    }
    Spacer(Modifier.height(Space.xl))
    SectionLabel("Backdrop")
    Spacer(Modifier.height(Space.s))
    ReferenceContent.backdrops.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s)) {
            row.forEach { option ->
                val selected = state.settings.backdrop == option.id
                ChamferPanel(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setBackdrop(option.id) },
                ) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) Console.VioletLight else Console.Text,
                    )
                    Spacer(Modifier.height(Space.xs))
                    StatusBadge(if (selected) "In use" else "Preview",
                        if (selected) BadgeTone.READY else BadgeTone.NEUTRAL)
                }
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(Space.s))
    }

    Spacer(Modifier.height(Space.l))
    SectionLabel("Effects")
    Spacer(Modifier.height(Space.s))
    ChamferPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Atmosphere particles", style = MaterialTheme.typography.titleMedium, color = Console.Text)
                Text("Adds drifting specks to the scene", style = MaterialTheme.typography.bodySmall, color = Console.TextDim)
            }
            AngularToggle(checked = state.settings.particles, onCheckedChange = viewModel::setParticles)
        }
        Spacer(Modifier.height(Space.m))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Deep vignette", style = MaterialTheme.typography.titleMedium, color = Console.Text)
                Text("Darkens the screen edges", style = MaterialTheme.typography.bodySmall, color = Console.TextDim)
            }
            AngularToggle(checked = state.settings.vignette, onCheckedChange = viewModel::setVignette)
        }
    }

    Spacer(Modifier.height(Space.l))
    SectionLabel("Edge lighting")
    Spacer(Modifier.height(Space.s))
    ChamferPanel {
        Text(
            text = "Console edge light effects are listed by the reference but need LED hardware PLAYCORE cannot query.",
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
        Spacer(Modifier.height(Space.s))
        ReferenceContent.ledEffects.forEach { effect ->
            InfoRow(effect, if (effect == "Steady") "Saved choice" else "Hardware required")
        }
        StatusBadge("Device unsupported", BadgeTone.BLOCKED)
    }
}

@Composable
private fun CoreReactorTile(state: SystemUiState, viewModel: SystemViewModel) {
    val modes = ReferenceContent.cubeModes
    val selected = modes.indexOf(state.settings.cubeMode)
    BanneredPanel(title = "Core reactor") {
        Text(
            text = "The reactor switches the CPU mode PLAYCORE asks for. The choice is stored and shown here; " +
                "actually writing governor limits needs Root or Shizuku.",
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
        Spacer(Modifier.height(Space.m))
        AngularTabs(
            options = modes.map { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            selectedIndex = if (selected < 0) 0 else selected,
            onSelect = { viewModel.setCubeMode(modes[it]) },
        )
    }
    Spacer(Modifier.height(Space.xl))
    ChamferPanel {
        InfoRow("Selected mode", state.settings.cubeMode.name.lowercase().replaceFirstChar(Char::uppercase))
        InfoRow("Balance", "Governor and limits tuned for sustained load")
        InfoRow("Rise", "Higher sustained clocks, more heat")
        InfoRow("Eco", "Lower ceiling, longest battery")
        Spacer(Modifier.height(Space.s))
        StatusBadge("Stored only — writes need privilege", BadgeTone.PREVIEW)
    }
}

@Composable
private fun RootToolsTile(state: SystemUiState, viewModel: SystemViewModel) {
    val relevant = state.capabilities.filter {
        it.id == CapabilityId.ROOT || it.id == CapabilityId.SHIZUKU || it.id == CapabilityId.KERNEL_CONTROL
    }
    BanneredPanel(title = "Root tools") {
        Text(
            text = "PLAYCORE probes for these backends and reports what it finds. It never runs a privileged " +
                "command without a feature asking for one, and no feature in this build asks.",
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
    }
    Spacer(Modifier.height(Space.xl))
    Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
        relevant.forEach { capability: Capability ->
            CapabilityRow(
                capability = capability,
                onAction = { viewModel.openCapabilitySettings(capability.id) },
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Console.VioletLight)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Console.TextDim)
    }
}

@Composable
private fun PluginBadge(requiresPrivilege: Boolean, rootReady: Boolean) {
    if (requiresPrivilege && !rootReady) {
        StatusBadge("Root required", BadgeTone.PREVIEW)
    } else {
        StatusBadge("Ready", BadgeTone.READY)
    }
}
