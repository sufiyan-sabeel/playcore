package app.playcore.ui.licenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.system.SystemViewModel

private data class LicenceEntry(val name: String, val licence: String, val note: String)

private val entries = listOf(
    LicenceEntry("PLAYCORE", "Apache License 2.0", "This application"),
    LicenceEntry("Kotlin standard library", "Apache License 2.0", "JetBrains"),
    LicenceEntry("AndroidX Core, Lifecycle, Activity", "Apache License 2.0", "Android Open Source Project"),
    LicenceEntry("Jetpack Compose (UI, Material 3)", "Apache License 2.0", "Android Open Source Project"),
    LicenceEntry("Navigation Compose", "Apache License 2.0", "Android Open Source Project"),
    LicenceEntry("Room", "Apache License 2.0", "Android Open Source Project"),
    LicenceEntry("DataStore", "Apache License 2.0", "Android Open Source Project"),
    LicenceEntry("kotlinx.coroutines", "Apache License 2.0", "JetBrains"),
    LicenceEntry("JUnit 4", "Eclipse Public License 1.0", "Tests only"),
)

@Composable
fun LicensesScreen(
    onBack: () -> Unit,
    viewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ConsoleBackground(state.settings.backdrop, state.settings.vignette) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReferenceHeader(
                title = "Licences",
                subtitle = "PLAYCORE / THIRD PARTY",
                onBack = onBack,
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = Space.l, end = Space.l, bottom = Space.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(Space.s),
            ) {
                items(entries, key = { it.name }) { entry ->
                    ChamferPanel {
                        Text(entry.name, style = MaterialTheme.typography.titleMedium, color = Console.Text)
                        Spacer(Modifier.height(Space.xxs))
                        Text(entry.licence, style = MaterialTheme.typography.labelMedium, color = Console.VioletLight)
                        Text(entry.note, style = MaterialTheme.typography.bodySmall, color = Console.TextDim)
                    }
                }
                item {
                    Text(
                        text = "Full licence texts ship in LICENSES/. The Apache 2.0 text for PLAYCORE is in LICENSE.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.TextFaint,
                        modifier = Modifier.padding(top = Space.m),
                    )
                }
            }
        }
    }
}
