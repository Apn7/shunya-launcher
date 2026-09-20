package dev.apn7.shunya.feature.focus.schedules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route

/** Entry point of [dev.apn7.shunya.core.navigation.Route.Schedules] (signature fixed by `ShunyaNavHost`). */
@Composable
fun SchedulesEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val viewModel = viewModel { SchedulesViewModel(container.focusConfigRepository, container.focusController) }
    val state: SchedulesUiState by viewModel.state.collectAsStateWithLifecycle()
    SchedulesScreen(
        state = state,
        onBack = navigator::back,
        onAdd = { navigator.navigate(Route.ScheduleEditor(null)) },
        onEdit = { id -> navigator.navigate(Route.ScheduleEditor(id)) },
        onAddPreset = { schedule -> viewModel.add(schedule) },
    )
}
