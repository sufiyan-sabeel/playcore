package app.playcore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Full-bleed scene background. Each verified backdrop id maps to a measured
 * violet gradient; switching the backdrop in Atmosphere changes this immediately,
 * so the control is real rather than decorative.
 */
private fun gradientFor(backdrop: String): List<Color> = when (backdrop) {
    "aurora" -> listOf(Color(0xFF181030), Color(0xFF2A1B5E), Color(0xFF183A4A))
    "carbon" -> listOf(Color(0xFF101018), Color(0xFF1A1A24), Color(0xFF181030))
    "circuit" -> listOf(Color(0xFF181030), Color(0xFF201838), Color(0xFF3A2060))
    "grid" -> listOf(Color(0xFF141026), Color(0xFF201838), Color(0xFF141026))
    "hex" -> listOf(Color(0xFF181030), Color(0xFF282040), Color(0xFF181030))
    "matrix" -> listOf(Color(0xFF0E1A18), Color(0xFF14261F), Color(0xFF181030))
    "neon" -> listOf(Color(0xFF181030), Color(0xFF3A1060), Color(0xFF181030))
    "reactor" -> listOf(Color(0xFF1A0F2E), Color(0xFF302048), Color(0xFF1A0F2E))
    "vortex" -> listOf(Color(0xFF181030), Color(0xFF241640), Color(0xFF0E0A1C))
    else -> listOf(Color(0xFF181030), Color(0xFF201838), Color(0xFF140C28))
}

@Composable
fun ConsoleBackground(
    backdrop: String,
    vignette: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = gradientFor(backdrop)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors)),
    ) {
        if (vignette) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color(0x66000000)),
                        )
                    ),
            )
        }
        content()
    }
}
