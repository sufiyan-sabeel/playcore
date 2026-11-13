package app.playcore.core.monitoring

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import android.view.Display
import app.playcore.core.model.DeviceSnapshot
import app.playcore.core.model.Metric
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Reads device telemetry from documented Android APIs only.
 *
 * Nothing here is simulated: when a value cannot be read, the snapshot carries
 * a [Metric.Unavailable] with the reason instead of a number.
 */
class DeviceMonitor(private val context: Context) {

    fun snapshot(): DeviceSnapshot {
        val metrics = buildList {
            addAll(batteryMetrics())
            addAll(memoryMetrics())
            addAll(storageMetrics())
            add(networkMetric())
            add(refreshRateMetric())
            add(cpuMetric())
            add(androidVersionMetric())
        }
        return DeviceSnapshot(metrics = metrics, capturedAt = System.currentTimeMillis())
    }

    private fun batteryMetrics(): List<Metric> {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return listOf(
            Metric.Unavailable(
                label = "Battery",
                reason = "The system did not return the sticky battery broadcast on this device."
            )
        )

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val statusLabel = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }

        val temperatureTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val temperature = if (temperatureTenths != Int.MIN_VALUE && temperatureTenths > 0) {
            Metric.Value(
                label = "Battery temperature",
                value = String.format("%.1f °C", temperatureTenths / 10f),
                detail = "Reported by the battery driver"
            )
        } else {
            Metric.Unavailable(
                label = "Battery temperature",
                reason = "This device does not report EXTRA_TEMPERATURE."
            )
        }

        val battery = if (percent >= 0) {
            Metric.Value(label = "Battery", value = "$percent%", detail = statusLabel)
        } else {
            Metric.Unavailable(
                label = "Battery",
                reason = "The battery level extras were missing from the broadcast."
            )
        }
        return listOf(battery, temperature)
    }

    private fun memoryMetrics(): List<Metric> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return listOf(
                Metric.Unavailable("RAM usage", "ActivityManager is unavailable on this device.")
            )
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        if (info.totalMem <= 0L) {
            return listOf(Metric.Unavailable("RAM usage", "Total memory was not reported."))
        }
        val used = info.totalMem - info.availMem
        return listOf(
            Metric.Value(
                label = "RAM usage",
                value = Formatter.formatShortFileSize(context, used),
                detail = "of ${Formatter.formatShortFileSize(context, info.totalMem)}"
            ),
            Metric.Value(
                label = "Available memory",
                value = Formatter.formatShortFileSize(context, info.availMem)
            )
        )
    }

    private fun storageMetrics(): List<Metric> {
        return runCatching {
            val stat = StatFs(Environment.getDataDirectory().absolutePath)
            val total = stat.blockCountLong * stat.blockSizeLong
            val available = stat.availableBlocksLong * stat.blockSizeLong
            if (total <= 0L) {
                listOf(Metric.Unavailable("Storage", "The data partition size was not reported."))
            } else {
                listOf(
                    Metric.Value(
                        label = "Storage used",
                        value = Formatter.formatShortFileSize(context, total - available),
                        detail = "of ${Formatter.formatShortFileSize(context, total)}"
                    )
                )
            }
        }.getOrElse {
            listOf(Metric.Unavailable("Storage", "StatFs could not read the data partition."))
        }
    }

    private fun networkMetric(): Metric {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return Metric.Unavailable("Network", "ConnectivityManager is unavailable.")
        val network = manager.activeNetwork
            ?: return Metric.Value(label = "Network", value = "Offline", detail = "No active network")
        val caps = manager.getNetworkCapabilities(network)
            ?: return Metric.Value(label = "Network", value = "Offline", detail = "No capabilities")
        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Connected"
        }
        val validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return Metric.Value(
            label = "Network",
            value = transport,
            detail = if (validated) "Internet reachable" else "Connected, not validated"
        )
    }

    @Suppress("DEPRECATION")
    private fun refreshRateMetric(): Metric {
        return runCatching {
            val displayManager = context.getSystemService(DisplayManager::class.java)
            val display: Display? = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            val rate = display?.refreshRate ?: 0f
            if (rate > 0f) {
                Metric.Value(
                    label = "Refresh rate",
                    value = String.format("%.1f Hz", rate),
                    detail = "Current display mode"
                )
            } else {
                Metric.Unavailable("Refresh rate", "The display did not report a refresh rate.")
            }
        }.getOrElse {
            Metric.Unavailable("Refresh rate", "DisplayManager could not be queried.")
        }
    }

    private fun cpuMetric(): Metric {
        return runCatching {
            val cpuInfo = File("/proc/cpuinfo")
            if (!cpuInfo.canRead()) {
                return@runCatching Metric.Unavailable(
                    label = "CPU",
                    reason = "Android does not expose /proc/cpuinfo to apps on this device."
                )
            }
            val hardware = cpuInfo.useLines { lines ->
                lines.map { it.trim() }
                    .firstOrNull { it.startsWith("Hardware", ignoreCase = true) }
                    ?.substringAfter(':')
                    ?.trim()
            }
            val cores = Runtime.getRuntime().availableProcessors()
            if (hardware.isNullOrBlank()) {
                Metric.Value(
                    label = "CPU",
                    value = "$cores cores",
                    detail = "Model not reported by the kernel"
                )
            } else {
                Metric.Value(label = "CPU", value = hardware, detail = "$cores cores")
            }
        }.getOrElse {
            Metric.Unavailable("CPU", "CPU information could not be read.")
        }
    }

    private fun androidVersionMetric(): Metric = Metric.Value(
        label = "Android version",
        value = "Android ${Build.VERSION.RELEASE}",
        detail = "API ${Build.VERSION.SDK_INT} · ${Build.MANUFACTURER} ${Build.MODEL}"
    )
}

/** Lifecycle-friendly ticker used by the dashboard; never runs without a subscriber. */
object Tickers {
    fun every(intervalMs: Long): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(intervalMs)
        }
    }
}
