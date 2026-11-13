package app.playcore.ui.licenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.playcore.core.designsystem.PlaycoreSpacing
import app.playcore.ui.components.PlaycoreCard

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
    LicenceEntry("JUnit 4", "Eclipse Public License 1.0", "Test only"),
)

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PlaycoreSpacing.s, vertical = PlaycoreSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Open-source licences",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = PlaycoreSpacing.l,
                end = PlaycoreSpacing.l,
                bottom = PlaycoreSpacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s),
        ) {
            items(entries, key = { it.name }) { entry ->
                PlaycoreCard {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(PlaycoreSpacing.xxs))
                    Text(
                        text = entry.licence,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Spacer(Modifier.height(PlaycoreSpacing.m))
                Text(
                    text = "Full licence texts ship in the repository under LICENSES/. The Apache 2.0 text for PLAYCORE itself is in LICENSE.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
