package app.playcore.ui.gamepad

import android.view.InputDevice
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.ui.components.BadgeTone
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.components.SectionLabel
import app.playcore.ui.components.StatusBadge
import app.playcore.ui.system.SystemViewModel

/**
 * Gamepad mode. Evidence level: INFERRED — labels verified (gs_gamepad_on,
 * gs_gamepad_none), focus-first layout is not.
 */
@Composable
fun GamepadScreen(
    onOpenGame: (String) -> Unit,
    modeBar: @Composable () -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gamepadName: String? = remember {
        InputDevice.getDeviceIds()
            .mapNotNull { runCatching { InputDevice.getDevice(it) }.getOrNull() }
            .firstOrNull { device ->
                (device.sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
            }
            ?.name
    }

    ConsoleBackground(state.settings.backdrop, state.settings.vignette) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReferenceHeader(
                title = "Gamepad",
                subtitle = "PLAYCORE / CONTROLLER MODE",
            )
            modeBar()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                BanneredPanel(title = "Controller") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = gamepadName ?: "No gamepad detected",
                                style = MaterialTheme.typography.titleMedium,
                                color = Console.Text,
                            )
                            Text(
                                text = if (gamepadName != null) {
                                    "Detected by Android's input framework."
                                } else {
                                    "Pair a controller through Android settings; PLAYCORE reads what the system reports."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Console.TextDim,
                            )
                        }
                        StatusBadge(
                            text = if (gamepadName != null) "Connected" else "Not connected",
                            tone = if (gamepadName != null) BadgeTone.READY else BadgeTone.NEUTRAL,
                        )
                    }
                }

                Spacer(Modifier.height(Space.xl))
                SectionLabel("Focus navigation")
                Spacer(Modifier.height(Space.s))
                Text(
                    text = "D-pad and stick move focus between cards; A opens a game and B goes back. " +
                        "Keyboard focus follows the same order.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Console.TextDim,
                )
                Spacer(Modifier.height(Space.m))

                if (state.games.isEmpty()) {
                    EmptyState(
                        title = "Nothing to select",
                        message = "Add games to the console and they appear in controller order.",
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
                        state.games.forEach { game ->
                            ChamferPanel(onClick = { onOpenGame(game.packageName) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = game.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Console.Text,
                                        modifier = Modifier.weight(1f),
                                    )
                                    StatusBadge("A · Start", BadgeTone.NEUTRAL)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Space.xxl))
            }
        }
    }
}
