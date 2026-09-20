package dev.apn7.shunya.feature.focus.screentime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startFirstAvailable

/** Entry point of [dev.apn7.shunya.core.navigation.Route.ScreenTime] (signature fixed by `ShunyaNavHost`). */
@Composable
fun ScreenTimeEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel = viewModel {
        ScreenTimeViewModel(container.usageRepository, AppLabels(container.appContext, container.appsRepository))
    }
    val state: ScreenTimeUiState by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    ScreenTimeScreen(
        state = state,
        onBack = navigator::back,
        onAppClick = { pkg -> navigator.navigate(Route.ScreenTimeAppDetail(pkg)) },
        onGrantUsage = { context.startFirstAvailable(SystemIntents.usageAccessSettings(context)) },
    )
}
