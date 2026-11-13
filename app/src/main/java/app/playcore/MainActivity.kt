package app.playcore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.playcore.core.designsystem.PlaycoreTheme
import app.playcore.core.model.AppSettings
import app.playcore.ui.navigation.PlaycoreApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as PlaycoreApplication).container

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())

            PlaycoreTheme(
                themeMode = settings.themeMode,
                amoled = settings.amoled,
                accent = settings.accent,
            ) {
                PlaycoreApp()
            }
        }
    }
}
