package dev.apn7.shunya.feature.focus.distracting

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.CheckRow
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.SliderRow
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.feature.focus.ui.AppSearchField
import dev.apn7.shunya.feature.focus.ui.PackageRow
import dev.apn7.shunya.feature.focus.ui.Paragraph
import dev.apn7.shunya.feature.focus.ui.matching

/** Pause length slider, a search field and every app with a checkbox. */
@Composable
internal fun DistractingAppsScreen(
    state: DistractingAppsUiState,
    onBack: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onPauseSeconds: (Int) -> Unit,
) {
    val resources = LocalContext.current.resources
    var query: String by rememberSaveable { mutableStateOf("") }
    val visible: List<PackageRow> = remember(state.apps, query) { state.apps.matching(query) }
    ShunyaScreen(title = stringResource(R.string.focus_distracting_title), onBack = onBack, scrollable = false) {
        PauseLengthSlider(seconds = state.pauseSeconds, onFinished = onPauseSeconds)
        Paragraph(
            text = stringResource(R.string.focus_distracting_intro),
            style = ShunyaTheme.typography.bodySmall,
            color = ShunyaTheme.colors.tertiary,
        )
        SectionHeader(resources.getQuantityString(R.plurals.focus_chosen_count, state.distracting.size, state.distracting.size))
        AppSearchField(query = query, onQueryChange = { query = it })
        if (visible.isEmpty() && state.apps.isNotEmpty()) {
            EmptyState(title = stringResource(R.string.focus_no_matches))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(visible, key = { it.packageName }) { row ->
                    CheckRow(
                        title = row.label,
                        checked = row.packageName in state.distracting,
                        onCheckedChange = { on -> onToggle(row.packageName, on) },
                    )
                }
            }
        }
    }
}

/** 3–30 seconds; the value is kept locally while dragging and saved when the finger lifts. */
@Composable
private fun PauseLengthSlider(seconds: Int, onFinished: (Int) -> Unit) {
    var value: Float by remember(seconds) { mutableFloatStateOf(seconds.toFloat()) }
    val min = ProductLimits.PAUSE_SECONDS_MIN
    val max = ProductLimits.PAUSE_SECONDS_MAX
    SliderRow(
        title = stringResource(R.string.focus_pause_length),
        value = value,
        onValueChange = { value = it },
        valueRange = min.toFloat()..max.toFloat(),
        steps = max - min - 1,
        valueLabel = stringResource(R.string.focus_seconds_value, Math.round(value)),
        onValueChangeFinished = { onFinished(Math.round(value)) },
    )
}
