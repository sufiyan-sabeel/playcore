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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.core.model.CapabilityId
import app.playcore.core.model.CapabilityStatus
import app.playcore.ui.components.AngularButton
import app.playcore.ui.components.BadgeTone
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.components.SectionLabel
import app.playcore.ui.components.StatusBadge
import app.playcore.ui.navigation.HeaderGear
import app.playcore.ui.system.SystemViewModel

/** The eight tiles are verified from the reference string table (gsx_sb_*). */
enum class SuperBaseTile(val id: String, val title: String, val subtitle: String) {
    GAME_CENTER("game_center", "Game Center", "Playtime, rank and settings"),
    GAME_LIBRARY("game_library", "Game Library", "Add games to the console"),
    PLUGIN_LIBRARY("plugin_library", "Plugin Library", "Helpers for in-game play"),
    CONTROL_PANEL("control_panel", "Control Panel", "Global profile"),
    CUSTOM_MODE("custom_mode", "Custom Mode", "CPU and GPU limits per mode"),
    ATMOSPHERE("atmosphere", "Atmosphere", "Backdrop and LED lighting"),
    CORE_REACTOR("core_reactor", "Core Reactor", "Tap to switch CPU mode"),
    ROOT_TOOLS("root_tools", "Root Tools", "Privileged access and diagnostics"),
    ;

    companion object {
        fun byId(id: String): SuperBaseTile? = entries.firstOrNull { it.id == id }
    }
}

@Composable
fun SuperBaseScreen(
    onOpenTile: (String) -> Unit,
    onOpenOverlay: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenSettings: () -> Unit,
    modeBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    ConsoleBackground(state.settings.backdrop, state.settings.vignette) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReferenceHeader(
                title = "Super Base",
                subtitle = "PLAYCORE / COMMAND DECK",
                trailing = { HeaderGear(onOpenSettings) },
            )
            modeBar()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                BanneredPanel(title = "Command deck") {
                    Text(
                        text = "Command deck for plugins, stats and atmosphere. Tiles marked Ready work now; " +
                            "Preview tiles store your choice until the access they need exists.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.TextDim,
                    )
                    Spacer(Modifier.height(Space.m))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                        AngularButton("Overlay panel", filled = true, onClick = onOpenOverlay)
                        AngularButton("Help", onClick = onOpenHelp)
                    }
                }

                Spacer(Modifier.height(Space.xl))
                SectionLabel("Tiles")
                Spacer(Modifier.height(Space.s))

                SuperBaseTile.entries.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.m),
                    ) {
                        row.forEach { tile ->
                            val (badge, tone) = tileBadge(tile, state)
                            ChamferPanel(
                                modifier = Modifier.weight(1f),
                                onClick = { onOpenTile(tile.id) },
                            ) {
                                Text(
                                    text = tile.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Console.Text,
                                )
                                Spacer(Modifier.height(Space.xxs))
                                Text(
                                    text = tile.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Console.TextDim,
                                )
                                Spacer(Modifier.height(Space.s))
                                StatusBadge(badge, tone)
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(Space.m))
                }

                Spacer(Modifier.height(Space.l))
                bottomBar()
            }
        }
    }
}

private fun tileBadge(tile: SuperBaseTile, state: app.playcore.ui.system.SystemUiState): Pair<String, BadgeTone> {
    val root = state.capabilities.firstOrNull { it.id == CapabilityId.ROOT }?.status
    return when (tile) {
        SuperBaseTile.GAME_CENTER, SuperBaseTile.ATMOSPHERE, SuperBaseTile.GAME_LIBRARY ->
            "Ready" to BadgeTone.READY
        SuperBaseTile.PLUGIN_LIBRARY -> "Ready" to BadgeTone.READY
        SuperBaseTile.CONTROL_PANEL, SuperBaseTile.CORE_REACTOR -> "Preview" to BadgeTone.PREVIEW
        SuperBaseTile.CUSTOM_MODE ->
            if (root == CapabilityStatus.AVAILABLE) "Ready" to BadgeTone.READY
            else "Root required" to BadgeTone.PREVIEW
        SuperBaseTile.ROOT_TOOLS ->
            if (root == CapabilityStatus.AVAILABLE) "Ready" to BadgeTone.READY
            else "Backend required" to BadgeTone.PREVIEW
    }
}
