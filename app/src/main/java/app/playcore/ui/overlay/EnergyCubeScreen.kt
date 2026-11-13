package app.playcore.ui.overlay

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
import app.playcore.core.model.CubeMode
import app.playcore.core.model.OverlayStyle
import app.playcore.core.model.ReferenceContent
import app.playcore.overlay.label
import app.playcore.ui.components.AngularButton
import app.playcore.ui.components.AngularTabs
import app.playcore.ui.components.BadgeTone
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.InfoRow
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.components.SectionLabel
import app.playcore.ui.components.StatusBadge
import app.playcore.ui.system.SystemViewModel

/**
 * Overlay styles and the Energy Cube card inventory. The four styles and all
 * nineteen cards are verified names; the panel itself is the Phase 3 transport
 * layer, so this screen can show and hide a real floating panel but not pretend
 * to be the finished Energy Cube.
 */
@Composable
fun EnergyCubeScreen(
    onBack: () -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val styles = OverlayStyle.entries
    val selectedStyle = styles.indexOf(state.settings.overlayStyle).coerceAtLeast(0)

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    ConsoleBackground(state.settings.backdrop, state.settings.vignette) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReferenceHeader(
                title = "Overlay panel",
                subtitle = "PLAYCORE / IN-GAME PANEL",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                AngularTabs(
                    options = styles.map { it.label() },
                    selectedIndex = selectedStyle,
                    onSelect = { viewModel.setOverlayStyle(styles[it]) },
                )

                Spacer(Modifier.height(Space.l))

                BanneredPanel(title = "Preview") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.overlayShowing) "Panel is on screen" else "Panel is closed",
                                style = MaterialTheme.typography.titleMedium,
                                color = Console.Text,
                            )
                            Text(
                                text = if (state.overlayPermission) {
                                    "Draws a real floating panel over the system."
                                } else {
                                    "Needs display-over-other-apps permission."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Console.TextDim,
                            )
                        }
                        StatusBadge(
                            text = if (state.overlayPermission) "Ready" else "Permission required",
                            tone = if (state.overlayPermission) BadgeTone.READY else BadgeTone.PREVIEW,
                        )
                    }
                    Spacer(Modifier.height(Space.m))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                        AngularButton(
                            label = if (state.overlayShowing) "Hide panel" else "Show panel",
                            filled = true,
                            enabled = state.overlayPermission,
                            onClick = viewModel::toggleOverlayPreview,
                        )
                        AngularButton(
                            label = "Open permission",
                            onClick = { viewModel.openCapabilitySettings(app.playcore.core.model.CapabilityId.OVERLAY) },
                        )
                    }
                    Spacer(Modifier.height(Space.s))
                    Text(
                        text = "The floating panel is the transport layer for these styles. It currently shows " +
                            "the style name and session state; the full card layout arrives in Phase 3.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.TextFaint,
                    )
                }

                Spacer(Modifier.height(Space.xl))
                SectionLabel("Mode")
                Spacer(Modifier.height(Space.s))
                ChamferPanel {
                    val modes = ReferenceContent.cubeModes
                    val modeIndex = modes.indexOf(state.settings.cubeMode).coerceAtLeast(0)
                    AngularTabs(
                        options = modes.map { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                        selectedIndex = modeIndex,
                        onSelect = { viewModel.setCubeMode(modes[it]) },
                    )
                    Spacer(Modifier.height(Space.s))
                    Text(
                        text = "Colour follows the mode: Balance violet, Rise red, Eco cyan, as in the reference.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.TextDim,
                    )
                }

                Spacer(Modifier.height(Space.xl))
                SectionLabel("Cards · ${ReferenceContent.cubeCards.size} verified")
                Spacer(Modifier.height(Space.s))
                Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
                    ReferenceContent.cubeCards.forEach { card ->
                        ChamferPanel {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ReferenceContent.cardLabel(card),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Console.Text,
                                    modifier = Modifier.weight(1f),
                                )
                                StatusBadge("Per game", BadgeTone.NEUTRAL)
                            }
                            Spacer(Modifier.height(Space.xxs))
                            Text(
                                text = ReferenceContent.cardNote(card),
                                style = MaterialTheme.typography.bodySmall,
                                color = Console.TextDim,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Space.s))
                Text(
                    text = "Cards are chosen per game in its profile. Nothing here reports a card as active " +
                        "while no game session is running.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Console.TextFaint,
                )
                Spacer(Modifier.height(Space.xxl))
            }
        }
    }
}
