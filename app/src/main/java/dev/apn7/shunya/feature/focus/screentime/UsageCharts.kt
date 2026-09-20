package dev.apn7.shunya.feature.focus.screentime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.core.designsystem.formatDuration
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.DayUsage
import dev.apn7.shunya.core.model.HourUsage
import dev.apn7.shunya.feature.focus.ui.currentLocale
import dev.apn7.shunya.feature.focus.ui.minuteOfDayText
import dev.apn7.shunya.feature.focus.ui.shortDayName

/** Seven daily bars, today last and in full ink; day names below. Read as a list by TalkBack. */
@Composable
internal fun WeekChart(days: List<DayUsage>, modifier: Modifier = Modifier) {
    val colors = ShunyaTheme.colors
    val resources = LocalContext.current.resources
    val locale = currentLocale()
    val names = days.map { shortDayName(it.date.dayOfWeek.value, locale) }
    val description = days.indices.joinToString(", ") { i -> names[i] + " " + formatDuration(resources, days[i].totalMillis) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s)
            .semantics { contentDescription = description },
    ) {
        val values = days.map { it.totalMillis }
        val last = days.lastIndex
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            drawBars(this, values, barFraction = 0.5f, divider = colors.divider) { index -> if (index == last) colors.ink else colors.tertiary }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs)) {
            names.forEachIndexed { index, name ->
                Text(
                    text = name,
                    style = ShunyaTheme.typography.caption,
                    color = if (index == last) colors.ink else colors.tertiary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Twenty-four hourly bars with the clock times of 0, 6, 12 and 18 o'clock below. */
@Composable
internal fun HourlyChart(hours: List<HourUsage>, modifier: Modifier = Modifier) {
    val colors = ShunyaTheme.colors
    val context = LocalContext.current
    val description = hours.filter { it.foregroundMillis > 0L }.joinToString(", ") { hour ->
        minuteOfDayText(context, hour.hour * 60) + " " + formatDuration(context.resources, hour.foregroundMillis)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s)
            .semantics { contentDescription = description },
    ) {
        val values = hours.map { it.foregroundMillis }
        Canvas(modifier = Modifier.fillMaxWidth().height(96.dp)) {
            drawBars(this, values, barFraction = 0.6f, divider = colors.divider) { colors.secondary }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs)) {
            listOf(0, 6, 12, 18).forEach { hour ->
                Text(
                    text = minuteOfDayText(context, hour * 60),
                    style = ShunyaTheme.typography.caption,
                    color = colors.tertiary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Bars for [values] in equal slots across the width, scaled to the largest value, on a hairline
 * baseline. A non-zero value always shows at least a sliver so "a little" differs from "none".
 */
private fun drawBars(
    scope: DrawScope,
    values: List<Long>,
    barFraction: Float,
    divider: Color,
    colorFor: (Int) -> Color,
) {
    if (values.isEmpty()) return
    with(scope) {
        val max = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
        val slot = size.width / values.size
        val barWidth = slot * barFraction
        val minBar = 2.dp.toPx()
        val radius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        values.forEachIndexed { index, value ->
            if (value > 0L) {
                val height = (size.height * value.toFloat() / max.toFloat()).coerceAtLeast(minBar)
                drawRoundRect(
                    color = colorFor(index),
                    topLeft = Offset(slot * index + (slot - barWidth) / 2f, size.height - height),
                    size = Size(barWidth, height),
                    cornerRadius = radius,
                )
            }
        }
        drawLine(
            color = divider,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx(),
        )
    }
}
