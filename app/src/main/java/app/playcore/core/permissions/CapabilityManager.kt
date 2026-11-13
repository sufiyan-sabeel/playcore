package app.playcore.core.permissions

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.playcore.core.model.Capability
import app.playcore.core.model.CapabilityId
import app.playcore.core.model.CapabilityStatus

/**
 * Single place that answers "can PLAYCORE do this on this device right now".
 *
 * Phase 1 reports status only. Nothing here performs a privileged operation, and
 * no capability is requested at startup: each screen asks when the user opts in.
 */
class CapabilityManager(private val context: Context) {

    fun snapshot(): List<Capability> = listOf(
        overlay(),
        usageAccess(),
        notifications(),
        mediaLibrary(),
        screenCapture(),
        shizuku(),
    )

    private fun overlay(): Capability {
        val granted = Settings.canDrawOverlays(context)
        return Capability(
            id = CapabilityId.OVERLAY,
            title = "Display over other apps",
            status = if (granted) CapabilityStatus.AVAILABLE else CapabilityStatus.PERMISSION_REQUIRED,
            explanation = if (granted) {
                "Floating panels can be drawn over games. No overlay ships in Phase 1, so nothing is drawn yet."
            } else {
                "Needed before any floating panel, HUD or crosshair can appear over a game. The library, dashboard and settings work without it."
            },
            actionLabel = if (granted) null else "Open overlay settings",
        )
    }

    private fun usageAccess(): Capability {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && appOps != null) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            AppOpsManager.MODE_DEFAULT
        }
        val granted = mode == AppOpsManager.MODE_ALLOWED
        return Capability(
            id = CapabilityId.USAGE_ACCESS,
            title = "Usage access",
            status = if (granted) CapabilityStatus.AVAILABLE else CapabilityStatus.PERMISSION_REQUIRED,
            explanation = if (granted) {
                "PLAYCORE can see which app is in the foreground, which per-game session tracking will use in Phase 4."
            } else {
                "Lets PLAYCORE detect the running game and record play time. Without it the library still works, but session detection does not."
            },
            actionLabel = if (granted) null else "Open usage access settings",
        )
    }

    private fun notifications(): Capability {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        return Capability(
            id = CapabilityId.NOTIFICATIONS,
            title = "Notifications",
            status = if (granted) CapabilityStatus.AVAILABLE else CapabilityStatus.PERMISSION_REQUIRED,
            explanation = if (granted) {
                "PLAYCORE may post status notifications. It posts none in Phase 1."
            } else {
                "Used for foreground-service status and update alerts in later phases. Nothing is posted today."
            },
            actionLabel = if (granted) null else "Open notification settings",
        )
    }

    private fun mediaLibrary(): Capability {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return Capability(
                id = CapabilityId.MEDIA_LIBRARY,
                title = "Media access",
                status = CapabilityStatus.AVAILABLE,
                explanation = "On this Android version PLAYCORE reads media only through the system document picker, so no media permission is needed.",
            )
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
        return Capability(
            id = CapabilityId.MEDIA_LIBRARY,
            title = "Media access",
            status = if (granted) CapabilityStatus.AVAILABLE else CapabilityStatus.PERMISSION_REQUIRED,
            explanation = if (granted) {
                "Custom game covers can be chosen from the gallery. Cover picking ships in Phase 2."
            } else {
                "Needed only if you choose a custom cover from your gallery. Without it PLAYCORE keeps using app icons."
            },
            actionLabel = if (granted) null else "Open app settings",
        )
    }

    private fun screenCapture(): Capability = Capability(
        id = CapabilityId.SCREEN_CAPTURE,
        title = "Screen capture",
        status = CapabilityStatus.TEMPORARILY_UNAVAILABLE,
        explanation = "Android grants screen capture per recording session through the MediaProjection consent dialog. PLAYCORE does not record in Phase 1, so nothing is requested.",
    )

    private fun shizuku(): Capability {
        val installed = runCatching {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
        }.isSuccess

        if (!installed) {
            return Capability(
                id = CapabilityId.SHIZUKU,
                title = "Shizuku backend",
                status = CapabilityStatus.BACKEND_REQUIRED,
                explanation = "Shizuku is not installed. It is optional: PLAYCORE's library, dashboard and settings never need it.",
            )
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            SHIZUKU_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
        return Capability(
            id = CapabilityId.SHIZUKU,
            title = "Shizuku backend",
            status = if (granted) CapabilityStatus.AVAILABLE else CapabilityStatus.PERMISSION_REQUIRED,
            explanation = if (granted) {
                "Shizuku is installed and has granted access. PLAYCORE performs no Shizuku operations in Phase 1."
            } else {
                "Shizuku is installed but has not granted PLAYCORE access. Open Shizuku and allow PLAYCORE there."
            },
            actionLabel = if (granted) null else "Open app settings",
        )
    }

    /** Returns true when a settings screen was actually opened. */
    fun openSettings(id: CapabilityId): Boolean {
        val intent = when (id) {
            CapabilityId.OVERLAY -> Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )

            CapabilityId.USAGE_ACCESS -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

            CapabilityId.NOTIFICATIONS, CapabilityId.MEDIA_LIBRARY, CapabilityId.SHIZUKU -> Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")
            )

            CapabilityId.SCREEN_CAPTURE -> return false
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        const val SHIZUKU_PERMISSION = "moe.shizuku.manager.permission.API_V23"
    }
}
