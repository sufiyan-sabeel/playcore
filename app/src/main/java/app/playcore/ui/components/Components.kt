package app.playcore.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import app.playcore.core.designsystem.BannerShape
import app.playcore.core.designsystem.Chamfer
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.PanelShape
import app.playcore.core.designsystem.Space
import app.playcore.core.designsystem.TabShape
import app.playcore.core.designsystem.TileShape
import app.playcore.core.model.Capability
import app.playcore.core.model.CapabilityStatus
import app.playcore.core.model.Metric

enum class BadgeTone { READY, PREVIEW, BLOCKED, NEUTRAL }

/**
 * Header used by every screen: angular back control, italic display title and a
 * slash-separated uppercase subtitle, exactly as the reference lays it out.
 */
@Composable
fun ReferenceHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Space.l, vertical = Space.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(TileShape)
                    .background(Console.Elevated)
                    .border(Chamfer.stroke, Console.VioletDeep, TileShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Console.Text,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(Space.m))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Console.TextDim,
            )
        }
        if (trailing != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
        }
    }
}

/** ✦ banner that sits on the top edge of a panel. */
@Composable
fun BannerHeader(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(BannerShape)
            .background(Console.VioletDeep)
            .padding(horizontal = Space.m, vertical = Space.xs),
    ) {
        Text(
            text = "✦  ${text.uppercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = Console.Text,
        )
    }
}

/** Chamfered panel: the reference's primary container. */
@Composable
fun ChamferPanel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier.fillMaxWidth().clip(PanelShape)
    Column(
        modifier = (if (onClick != null) base.clickable { onClick() } else base)
            .background(Console.Panel)
            .border(Chamfer.stroke, Console.VioletDeep, PanelShape)
            .padding(Space.l),
        content = content,
    )
}

/** Panel with a ✦ banner clipped onto its top edge. */
@Composable
fun BanneredPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(start = Space.l)) { BannerHeader(title) }
        Spacer(Modifier.height((-7).dp))
        ChamferPanel(content = content)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Console.TextDim,
        modifier = modifier,
    )
}

@Composable
fun StatusBadge(text: String, tone: BadgeTone, modifier: Modifier = Modifier) {
    val color = when (tone) {
        BadgeTone.READY -> Console.Ready
        BadgeTone.PREVIEW -> Console.Preview
        BadgeTone.BLOCKED -> Console.Blocked
        BadgeTone.NEUTRAL -> Console.TextDim
    }
    Box(
        modifier = modifier
            .clip(TabShape)
            .background(color.copy(alpha = 0.18f))
            .border(Chamfer.stroke, color.copy(alpha = 0.5f), TabShape)
            .padding(horizontal = Space.s, vertical = 2.dp),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/** Parallelogram tab strip. */
@Composable
fun AngularTabs(
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.s),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(TabShape)
                    .background(if (selected) Console.Violet else Color.Transparent)
                    .border(Chamfer.stroke, if (selected) Console.Violet else Console.VioletDeep, TabShape)
                    .clickable { onSelect(index) }
                    .padding(vertical = Space.s),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Console.Text else Console.TextDim,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Parallelogram ON/OFF control with a sliding block, as in the reference. */
@Composable
fun AngularToggle(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .width(78.dp)
            .height(28.dp)
            .clip(TabShape)
            .background(if (checked) Console.Violet else Color.Transparent)
            .border(Chamfer.stroke, if (checked) Console.Violet else Console.VioletDeep, TabShape)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = Space.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (checked) "ON" else "OFF",
            style = MaterialTheme.typography.labelSmall,
            color = if (checked) Console.Text else Console.TextDim,
        )
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (checked) Console.Text else Console.VioletDeep),
        )
    }
}

/** Square icon tile with a caption, used by the bottom bars. */
@Composable
fun IconTile(
    icon: ImageVector,
    caption: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(TileShape)
                .background(if (selected) Console.Violet else Console.PanelAlt)
                .border(Chamfer.stroke, if (selected) Console.Violet else Console.VioletDeep, TileShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = caption,
                tint = if (selected) Console.Text else Console.TextDim,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(Space.xs))
        Text(
            text = caption.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Console.Text else Console.TextFaint,
            maxLines = 1,
        )
    }
}

/** Action button in the reference's outlined/filled parallelogram style. */
@Composable
fun AngularButton(
    label: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val bg = when {
        !enabled -> Console.PanelAlt
        filled -> Console.Violet
        else -> Color.Transparent
    }
    val fg = when {
        !enabled -> Console.TextFaint
        filled -> Console.Text
        else -> Console.VioletLight
    }
    Box(
        modifier = modifier
            .clip(TabShape)
            .background(bg)
            .border(Chamfer.stroke, if (enabled) Console.Violet else Console.VioletDeep, TabShape)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = Space.l, vertical = Space.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = fg, maxLines = 1)
    }
}

@Composable
fun MetricTile(metric: Metric, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(Console.Panel)
            .border(Chamfer.stroke, Console.VioletDeep, PanelShape)
            .padding(Space.m),
    ) {
        Text(
            text = metric.metricLabel().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Console.TextDim,
        )
        Spacer(Modifier.height(Space.s))
        when (metric) {
            is Metric.Value -> {
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleLarge,
                    color = Console.Text,
                )
                if (metric.detail != null) {
                    Text(
                        text = metric.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Console.TextDim,
                    )
                }
            }

            is Metric.Unavailable -> {
                Text(
                    text = "Unavailable",
                    style = MaterialTheme.typography.titleMedium,
                    color = Console.Preview,
                )
                Spacer(Modifier.height(Space.xxs))
                Text(
                    text = metric.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = Console.TextDim,
                )
            }
        }
    }
}

private fun Metric.metricLabel(): String = when (this) {
    is Metric.Value -> label
    is Metric.Unavailable -> label
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Space.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Console.TextDim,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(Space.m))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Console.Text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.2f),
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    glyph: String = "+",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Space.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = glyph, style = MaterialTheme.typography.displaySmall, color = Console.VioletLight)
        Spacer(Modifier.height(Space.m))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(Space.s))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Console.TextDim,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Space.l))
            AngularButton(label = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun LoadingState(message: String = "Loading", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Space.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(26.dp),
            color = Console.Violet,
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.height(Space.m))
        Text(message, style = MaterialTheme.typography.bodySmall, color = Console.TextDim)
    }
}

@Composable
fun ErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Space.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Console.Blocked)
        Spacer(Modifier.height(Space.s))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Console.TextDim)
        if (onRetry != null) {
            Spacer(Modifier.height(Space.l))
            AngularButton(label = "Try again", onClick = onRetry)
        }
    }
}

@Composable
fun AppIcon(packageName: String, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap: ImageBitmap? = remember(packageName) {
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toBitmap(96, 96).asImageBitmap()
        }.getOrNull()
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(TileShape)
            .background(Console.PanelAlt),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun CapabilityRow(
    capability: Capability,
    modifier: Modifier = Modifier,
    onAction: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(Console.Panel)
            .border(Chamfer.stroke, Console.VioletDeep, PanelShape)
            .padding(Space.l),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = capability.title,
                style = MaterialTheme.typography.titleMedium,
                color = Console.Text,
                modifier = Modifier.weight(1f),
            )
            StatusBadge(text = capability.status.label(), tone = capability.status.tone())
        }
        Spacer(Modifier.height(Space.s))
        Text(
            text = capability.explanation,
            style = MaterialTheme.typography.bodySmall,
            color = Console.TextDim,
        )
        if (capability.actionLabel != null) {
            Spacer(Modifier.height(Space.m))
            AngularButton(label = capability.actionLabel, onClick = onAction)
        }
    }
}

fun CapabilityStatus.label(): String = when (this) {
    CapabilityStatus.AVAILABLE -> "Ready"
    CapabilityStatus.PERMISSION_REQUIRED -> "Permission required"
    CapabilityStatus.BACKEND_REQUIRED -> "Backend required"
    CapabilityStatus.DEVICE_UNSUPPORTED -> "Device unsupported"
    CapabilityStatus.TEMPORARILY_UNAVAILABLE -> "Temporarily unavailable"
    CapabilityStatus.UNAVAILABLE -> "Unavailable"
}

fun CapabilityStatus.tone(): BadgeTone = when (this) {
    CapabilityStatus.AVAILABLE -> BadgeTone.READY
    CapabilityStatus.PERMISSION_REQUIRED, CapabilityStatus.BACKEND_REQUIRED,
    CapabilityStatus.TEMPORARILY_UNAVAILABLE -> BadgeTone.PREVIEW
    CapabilityStatus.DEVICE_UNSUPPORTED, CapabilityStatus.UNAVAILABLE -> BadgeTone.BLOCKED
}

@Composable
fun ScreenScaffold(
    header: @Composable () -> Unit,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        header()
        Column(modifier = Modifier.weight(1f), content = content)
        if (bottomBar != null) {
            bottomBar()
        }
    }
}

@Composable
fun BackIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Console.Text,
        )
    }
}
