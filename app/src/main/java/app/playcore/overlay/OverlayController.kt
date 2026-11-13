package app.playcore.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import app.playcore.core.model.OverlayStyle

/**
 * Real, permission-gated floating panel.
 *
 * Phase 3 scope: this shows a minimal HUD (style name, session marker, live state)
 * using WindowManager and the display-over-other-apps permission. It is the
 * transport layer the Energy Cube, Compact, Nostalgia and X Mode panels will draw
 * into; it is not those panels yet, and it does not claim to be.
 */
class OverlayController(private val context: Context) {

    private val windowManager: WindowManager? =
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    private var panel: View? = null
    private var startedAt: Long = 0L

    val isShowing: Boolean get() = panel != null

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    /** Returns true when the panel was actually attached to the window manager. */
    fun show(style: OverlayStyle): Boolean {
        if (!canDrawOverlays() || windowManager == null) return false
        if (panel != null) return true

        val density = context.resources.displayMetrics.density
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((14 * density).toInt(), (10 * density).toInt(), (14 * density).toInt(), (10 * density).toInt())
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E6201838"))
                setStroke((1 * density).toInt(), Color.parseColor("#483090"))
            }
        }

        val title = TextView(context).apply {
            text = "PLAYCORE · ${style.label().uppercase()}"
            setTextColor(Color.parseColor("#F2F0FA"))
            textSize = 11f
            letterSpacing = 0.12f
        }
        val detail = TextView(context).apply {
            text = "Preview panel — no game session"
            setTextColor(Color.parseColor("#A0A0B0"))
            textSize = 9f
        }
        container.addView(title)
        container.addView(detail)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            android.graphics.PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (24 * density).toInt()
            y = (140 * density).toInt()
        }

        return runCatching {
            windowManager?.addView(container, layoutParams)
            panel = container
            startedAt = System.currentTimeMillis()
            true
        }.getOrDefault(false)
    }

    fun hide() {
        val current = panel ?: return
        runCatching { windowManager?.removeView(current) }
        panel = null
        startedAt = 0L
    }

    fun sessionMillis(): Long = if (startedAt == 0L) 0L else System.currentTimeMillis() - startedAt
}

fun OverlayStyle.label(): String = when (this) {
    OverlayStyle.ENERGY_CUBE -> "Energy Cube"
    OverlayStyle.COMPACT -> "Compact panel"
    OverlayStyle.NOSTALGIA -> "Nostalgia"
    OverlayStyle.X_MODE -> "X Mode"
}
