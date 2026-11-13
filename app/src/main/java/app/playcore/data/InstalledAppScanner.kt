package app.playcore.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import app.playcore.core.model.InstalledApp

/**
 * Lists apps that expose a launcher activity.
 *
 * PLAYCORE does not guess which apps are games. The list is every launchable app
 * on the device, and the user decides what belongs in the library.
 */
class InstalledAppScanner(private val context: Context) {

    fun scan(): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved: List<ResolveInfo> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        return resolved
            .asSequence()
            .mapNotNull { info -> info.activityInfo?.applicationInfo?.packageName }
            .distinct()
            .filter { it != context.packageName }
            .map { packageName ->
                InstalledApp(
                    packageName = packageName,
                    label = labelOf(packageName),
                    isLaunchable = pm.getLaunchIntentForPackage(packageName) != null,
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    private fun labelOf(packageName: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)
}
