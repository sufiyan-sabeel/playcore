package app.playcore.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.playcore.core.model.ThemeMode

/**
 * Design tokens measured from the reference application (see docs/VISUAL_INVENTORY.md).
 *
 * Every colour here came out of the published screenshots by pixel histogram, not
 * from a design preference. The reference is a violet console HUD: chamfered
 * panels, parallelogram tabs, heavy italic display type.
 */
object Console {
    // Dark scene, measured
    val Background = Color(0xFF181030)
    val Panel = Color(0xFF201838)
    val PanelAlt = Color(0xFF282040)
    val Elevated = Color(0xFF302048)
    val ElevatedAlt = Color(0xFF382850)

    // Violet accents, measured
    val Violet = Color(0xFF8060E8)
    val VioletLight = Color(0xFFA080F0)
    val VioletDeep = Color(0xFF483090)

    // Text
    val Text = Color(0xFFF2F0FA)
    val TextDim = Color(0xFFA0A0B0)
    val TextFaint = Color(0xFF6F6885)

    // Light mode, measured
    val LightSurface = Color(0xFFF8F8F8)
    val LightTint = Color(0xFFE8E0F8)
    val LightPanel = Color(0xFFF0E8F8)
    val LightText = Color(0xFF1A1430)
    val LightTextDim = Color(0xFF5A5470)

    // States
    val Ready = Color(0xFF63C6A0)
    val Preview = Color(0xFFE0A458)
    val Blocked = Color(0xFFD9736B)
}

object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp
}

/** Chamfer sizes used across panels and buttons. */
object Chamfer {
    val panel = 14.dp
    val tile = 10.dp
    val chip = 8.dp
    val stroke = 1.dp
}

private val ConsoleTypography = Typography(
    // Display faces are heavy and italic in the reference.
    displayLarge = TextStyle(fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic),
    displayMedium = TextStyle(fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic),
    displaySmall = TextStyle(fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic),
    headlineMedium = TextStyle(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
    headlineSmall = TextStyle(fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
    titleLarge = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontSize = 12.5.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontSize = 11.5.sp, lineHeight = 16.sp),
    // Labels are uppercase and letterspaced in the reference.
    labelLarge = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
    labelMedium = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp),
    labelSmall = TextStyle(fontSize = 9.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp),
)

private fun consoleDark() = darkColorScheme(
    primary = Console.Violet,
    onPrimary = Color(0xFF100A24),
    primaryContainer = Console.VioletDeep,
    onPrimaryContainer = Console.VioletLight,
    secondary = Console.VioletLight,
    onSecondary = Color(0xFF100A24),
    tertiary = Console.VioletDeep,
    background = Console.Background,
    onBackground = Console.Text,
    surface = Console.Background,
    onSurface = Console.Text,
    surfaceVariant = Console.Panel,
    onSurfaceVariant = Console.TextDim,
    surfaceContainer = Console.Panel,
    surfaceContainerHigh = Console.PanelAlt,
    surfaceContainerHighest = Console.Elevated,
    outline = Console.VioletDeep,
    outlineVariant = Console.PanelAlt,
    error = Console.Blocked,
    onError = Color(0xFF1A0A08),
)

private fun consoleLight() = lightColorScheme(
    primary = Console.Violet,
    onPrimary = Color.White,
    primaryContainer = Console.LightTint,
    onPrimaryContainer = Color(0xFF2A1B5E),
    secondary = Console.VioletDeep,
    onSecondary = Color.White,
    background = Console.LightSurface,
    onBackground = Console.LightText,
    surface = Console.LightSurface,
    onSurface = Console.LightText,
    surfaceVariant = Console.LightTint,
    onSurfaceVariant = Console.LightTextDim,
    surfaceContainer = Console.LightPanel,
    surfaceContainerHigh = Console.LightTint,
    surfaceContainerHighest = Console.LightTint,
    outline = Console.VioletLight,
    outlineVariant = Console.LightTint,
    error = Color(0xFFB3261E),
    onError = Color.White,
)

@Composable
fun PlaycoreTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    amoled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (dark) consoleDark() else consoleLight(),
        typography = ConsoleTypography,
        content = content,
    )
}
