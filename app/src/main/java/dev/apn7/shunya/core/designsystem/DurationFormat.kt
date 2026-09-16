package dev.apn7.shunya.core.designsystem

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import dev.apn7.shunya.R

private const val MINUTE_MILLIS = 60_000L

/**
 * Short, localized duration: "1h 12m", "45m", "2h", "under 1m" (Bangla: "১ ঘণ্টা ১২ মিনিট" …).
 * Used for screen time, limits and focus time left; digits follow the UI language.
 */
fun formatDuration(resources: Resources, millis: Long): String {
    val totalMinutes = (millis.coerceAtLeast(0L) / MINUTE_MILLIS).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        totalMinutes == 0 -> resources.getString(R.string.core_duration_under_minute)
        hours == 0 -> resources.getString(R.string.core_duration_minutes, minutes)
        minutes == 0 -> resources.getString(R.string.core_duration_hours, hours)
        else -> resources.getString(R.string.core_duration_hours_minutes, hours, minutes)
    }
}

/** Composable form of [formatDuration]. */
@Composable
@ReadOnlyComposable
fun durationText(millis: Long): String = formatDuration(LocalContext.current.resources, millis)
