package app.playcore.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.core.model.ReferenceContent
import app.playcore.ui.components.BanneredPanel
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.system.SystemViewModel

/** The seven help topics are verified from gsx_help_*_t. */
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ConsoleBackground(state.settings.backdrop, state.settings.vignette) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReferenceHeader(
                title = "Help",
                subtitle = "PLAYCORE / GUIDE",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l),
            ) {
                BanneredPanel(title = "Read first") {
                    Text(
                        text = "PLAYCORE reports what a device will and will not do. If an item needs access " +
                            "you have not granted, it says so instead of pretending to work.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.TextDim,
                    )
                }
                Spacer(Modifier.height(Space.l))
                Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
                    ReferenceContent.helpTopics.forEach { topic ->
                        ChamferPanel {
                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Console.Text,
                            )
                            Spacer(Modifier.height(Space.xs))
                            Text(
                                text = topic.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = Console.TextDim,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Space.xxl))
            }
        }
    }
}
