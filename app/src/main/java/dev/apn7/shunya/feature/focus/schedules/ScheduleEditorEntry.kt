package dev.apn7.shunya.feature.focus.schedules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route

/**
 * Entry point of [dev.apn7.shunya.core.navigation.Route.ScheduleEditor] (signature fixed by
 * `ShunyaNavHost`). [scheduleId] comes from the route; null creates a new schedule.
 */
@Composable
fun ScheduleEditorEntry(navigator: Navigator, scheduleId: String?) {
    val container = LocalAppContainer.current
    val viewModel = viewModel { ScheduleEditorViewModel(container.focusConfigRepository, scheduleId) }
    val draft: ScheduleDraft by viewModel.draft.collectAsStateWithLifecycle()
    val defaultName = stringResource(R.string.focus_schedule_default_name)
    ScheduleEditorScreen(
        draft = draft,
        isExisting = viewModel.isExisting,
        grayscaleAvailable = container.grayscaleController.isAvailable(),
        onBack = navigator::back,
        onChange = { transform -> viewModel.update(transform) },
        onSave = {
            viewModel.save(defaultName)
            navigator.back()
        },
        onDelete = {
            viewModel.delete()
            navigator.back()
        },
        onGrayscaleSetup = { navigator.navigate(Route.GrayscaleSetup) },
    )
}
