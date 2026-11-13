package app.playcore.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.playcore.core.model.AccentChoice
import app.playcore.core.model.ThemeMode

object PlaycoreSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp
}

/** AMOLED surfaces are pure black; graphite is the non-AMOLED dark base. */
object PlaycorePalette {
    val Amoled = Color(0xFF000000)
    val Graphite = Color(0xFF0E1214)
    val Charcoal = Color(0xFF151B1D)
    val Elevated = Color(0xFF1C2325)
    val Outline = Color(0xFF2A3335)
    val TextPrimary = Color(0xFFECF1F1)
    val TextSecondary = Color(0xFF9BAAAC)
    val Warning = Color(0xFFE0A458)
    val Danger = Color(0xFFD9736B)
    val Success = Color(0xFF5FB98C)

    val LightSurface = Color(0xFFF6F8F8)
    val LightCard = Color(0xFFFFFFFF)
    val LightOutline = Color(0xFFDCE3E3)
    val LightText = Color(0xFF101716)
    val LightTextSecondary = Color(0xFF5B6A6C)
}

fun accentColor(choice: AccentChoice): Color = when (choice) {
    AccentChoice.TEAL -> Color(0xFF2FB3A6)
    AccentChoice.CYAN -> Color(0xFF35C2E0)
    AccentChoice.VIOLET -> Color(0xFF9B7BF0)
    AccentChoice.AMBER -> Color(0xFFE3A34A)
}

private fun darkScheme(amoled: Boolean, accent: AccentChoice) = darkColorScheme(
    primary = accent,
    onPrimary = Color(0xFF04100F),
    primaryContainer = accent.copy(alpha = 0.18f),
    onPrimaryContainer = accent,
    secondary = PlaycorePalette.TextSecondary,
    onSecondary = Color(0xFF04100F),
    background = if (amoled) PlaycorePalette.Amoled else PlaycorePalette.Graphite,
    onBackground = PlaycorePalette.TextPrimary,
    surface = if (amoled) PlaycorePalette.Amoled else PlaycorePalette.Graphite,
    onSurface = PlaycorePalette.TextPrimary,
    surfaceVariant = PlaycorePalette.Charcoal,
    onSurfaceVariant = PlaycorePalette.TextSecondary,
    surfaceContainer = PlaycorePalette.Charcoal,
    surfaceContainerHigh = PlaycorePalette.Elevated,
    outline = PlaycorePalette.Outline,
    outlineVariant = PlaycorePalette.Outline,
    error = PlaycorePalette.Danger,
    onError = Color(0xFF1A0A08),
)

private fun lightScheme(accent: AccentChoice) = lightColorScheme(
    primary = accent.copy(alpha = 0.95f),
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFF0B3B37),
    background = PlaycorePalette.LightSurface,
    onBackground = PlaycorePalette.LightText,
    surface = PlaycorePalette.LightSurface,
    onSurface = PlaycorePalette.LightText,
    surfaceVariant = PlaycorePalette.LightCard,
    onSurfaceVariant = PlaycorePalette.LightTextSecondary,
    surfaceContainer = PlaycorePalette.LightCard,
    surfaceContainerHigh = PlaycorePalette.LightCard,
    outline = PlaycorePalette.LightOutline,
    outlineVariant = PlaycorePalette.LightOutline,
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val PlaycoreTypography = Typography(
    displaySmall = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
)

private val PlaycoreShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun PlaycoreTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    amoled: Boolean = true,
    accent: AccentChoice = AccentChoice.TEAL,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (dark) darkScheme(amoled, accent) else lightScheme(accent),
        typography = PlaycoreTypography,
        shapes = PlaycoreShapes,
        content = content,
    )
}
