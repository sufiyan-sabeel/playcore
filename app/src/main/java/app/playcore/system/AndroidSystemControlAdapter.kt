package app.playcore.system

import android.app.NotificationManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.provider.Settings
import android.view.Display
import app.playcore.domain.restoration.SystemControlPort
import app.playcore.domain.restoration.SystemTarget
import app.playcore.domain.restoration.TargetValue

/**
 * The real platform implementation of [SystemControlPort].
 *
 * Only targets Android actually exposes to a normal app are writable here. Refresh
 * rate reports as unsupported because there is no public setter — pretending
 * otherwise would produce a control that silently does nothing.
 */
class AndroidSystemControlAdapter(private val context: Context) : SystemControlPort {

    private val notificationManager: NotificationManager? =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    override fun isSupported(target: SystemTarget): Boolean = when (target) {
        SystemTarget.DO_NOT_DISTURB -> notificationManager != null
        SystemTarget.BRIGHTNESS -> true
        SystemTarget.REFRESH_RATE -> false
    }

    override fun isPermitted(target: SystemTarget): Boolean = when (target) {
        SystemTarget.DO_NOT_DISTURB -> notificationManager?.isNotificationPolicyAccessGranted == true
        SystemTarget.BRIGHTNESS -> Settings.System.canWrite(context)
        SystemTarget.REFRESH_RATE -> false
    }

    override fun read(target: SystemTarget): TargetValue? = when (target) {
        SystemTarget.DO_NOT_DISTURB ->
            notificationManager?.let { TargetValue.IntValue(it.currentInterruptionFilter) }

        SystemTarget.BRIGHTNESS -> runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        }.getOrNull()?.let { TargetValue.IntValue(it) }

        SystemTarget.REFRESH_RATE -> currentRefreshRate()
    }

    override fun write(target: SystemTarget, value: TargetValue): Boolean = when (target) {
        SystemTarget.DO_NOT_DISTURB -> {
            val filter = (value as? TargetValue.IntValue)?.value ?: return false
            val manager = notificationManager ?: return false
            runCatching { manager.setInterruptionFilter(filter) }.isSuccess
        }

        SystemTarget.BRIGHTNESS -> {
            val level = (value as? TargetValue.IntValue)?.value ?: return false
            runCatching {
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, level)
            }.getOrDefault(false)
        }

        SystemTarget.REFRESH_RATE -> false
    }

    @Suppress("DEPRECATION")
    private fun currentRefreshRate(): TargetValue? = runCatching {
        val manager = context.getSystemService(DisplayManager::class.java)
        val display: Display? = manager?.getDisplay(Display.DEFAULT_DISPLAY)
        display?.refreshRate?.takeIf { it > 0f }?.let { TargetValue.TextValue(String.format("%.1f", it)) }
    }.getOrNull()
}
