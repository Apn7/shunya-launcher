package dev.apn7.shunya.feature.focus.distracting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator

/** Entry point of [dev.apn7.shunya.core.navigation.Route.DistractingApps] (signature fixed by `ShunyaNavHost`). */
@Composable
fun DistractingAppsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val viewModel = viewModel { DistractingAppsViewModel(container.appsRepository, container.focusConfigRepository) }
    val state: DistractingAppsUiState by viewModel.state.collectAsStateWithLifecycle()
    DistractingAppsScreen(
        state = state,
        onBack = navigator::back,
        onToggle = { packageName, on -> viewModel.setDistracting(packageName, on) },
        onPauseSeconds = { seconds -> viewModel.setPauseSeconds(seconds) },
    )
}
