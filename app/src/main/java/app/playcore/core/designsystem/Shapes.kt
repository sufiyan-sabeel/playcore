package app.playcore.core.designsystem

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A rectangle with its top-left and bottom-right corners cut off. This is the
 * dominant panel shape in the reference interface.
 */
class ChamferShape(
    private val cut: Dp = 14.dp,
    private val corners: Set<Corner> = setOf(Corner.TOP_START, Corner.BOTTOM_END),
) : Shape {

    enum class Corner { TOP_START, TOP_END, BOTTOM_START, BOTTOM_END }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val c = with(density) { cut.toPx() }.coerceAtMost(minOf(size.width, size.height) / 2f)
        val path = Path().apply {
            moveTo(if (Corner.TOP_START in corners) c else 0f, 0f)
            lineTo(size.width, 0f)
            if (Corner.TOP_END in corners) {
                lineTo(size.width, size.height - c)
            } else {
                lineTo(size.width, size.height)
            }
            if (Corner.BOTTOM_END in corners) {
                lineTo(size.width - c, size.height)
            }
            lineTo(0f, size.height)
            if (Corner.BOTTOM_START in corners) {
                lineTo(0f, c)
            } else {
                lineTo(0f, 0f)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

/** Parallelogram used by tabs, toggles and small action buttons. */
class ParallelogramShape(private val skew: Dp = 8.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = with(density) { skew.toPx() }.coerceAtMost(size.width / 3f)
        val path = Path().apply {
            moveTo(s, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - s, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

val PanelShape = ChamferShape(Chamfer.panel)
val TileShape = ChamferShape(Chamfer.tile, setOf(ChamferShape.Corner.TOP_START))
val BannerShape = ParallelogramShape(7.dp)
val TabShape = ParallelogramShape(8.dp)
val ChipShape = ParallelogramShape(5.dp)
