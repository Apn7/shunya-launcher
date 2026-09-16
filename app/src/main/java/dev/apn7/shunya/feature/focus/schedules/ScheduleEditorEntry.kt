package dev.apn7.shunya.feature.focus.schedules

import androidx.compose.runtime.Composable
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.PlaceholderScreen

/** Entry point of [dev.apn7.shunya.core.navigation.Route.ScheduleEditor] (signature fixed by `ShunyaNavHost`). [scheduleId] comes from the route. */
@Composable
fun ScheduleEditorEntry(navigator: Navigator, scheduleId: String?) {
    PlaceholderScreen(navigator)
}
