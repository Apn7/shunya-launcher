package dev.apn7.shunya.feature.home.system

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Battery level in percent and whether it is charging. */
@Immutable
data class BatteryState(val percent: Int, val isCharging: Boolean)

/**
 * The battery state while home is visible: the sticky `ACTION_BATTERY_CHANGED` on start, then
 * every change until stop. Null while [enabled] is false or before the first reading.
 */
@Composable
internal fun rememberBatteryState(enabled: Boolean): BatteryState? {
    var state by remember { mutableStateOf<BatteryState?>(null) }
    SystemBroadcastEffect(enabled = enabled, action = Intent.ACTION_BATTERY_CHANGED) { intent ->
        val parsed = intent?.let { parseBattery(it) }
        if (parsed != null) state = parsed
    }
    return if (enabled) state else null
}

/**
 * Trigger time (epoch millis) of the next alarm set in any clock app, or null. Re-read on every
 * start and whenever the system announces a change.
 */
@Composable
internal fun rememberNextAlarmMillis(enabled: Boolean): Long? {
    val context = LocalContext.current
    var next by remember { mutableStateOf<Long?>(null) }
    SystemBroadcastEffect(enabled = enabled, action = AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED) {
        next = readNextAlarm(context)
    }
    return if (enabled) next else null
}

private fun parseBattery(intent: Intent): BatteryState? {
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (level < 0 || scale <= 0) return null
    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
    return BatteryState(
        percent = (level * 100 / scale).coerceIn(0, 100),
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING,
    )
}

private fun readNextAlarm(context: Context): Long? {
    val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return null
    return alarmManager.nextAlarmClock?.triggerTime
}
