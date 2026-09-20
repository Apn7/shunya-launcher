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
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startFirstAvailable
import dev.apn7.shunya.core.system.startSafely

/**
 * Entry point of [dev.apn7.shunya.core.navigation.Route.ScreenTimeAppDetail] (signature fixed by
 * `ShunyaNavHost`). [packageName] comes from the route.
 */
@Composable
fun ScreenTimeAppDetailEntry(navigator: Navigator, packageName: String) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel = viewModel {
        ScreenTimeAppDetailViewModel(
            packageName = packageName,
            usageRepository = container.usageRepository,
            configRepository = container.focusConfigRepository,
            appsRepository = container.appsRepository,
            labels = AppLabels(container.appContext, container.appsRepository),
        )
    }
    val state: AppDetailUiState by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    ScreenTimeAppDetailScreen(
        state = state,
        onBack = navigator::back,
        onDistracting = { on -> viewModel.setDistracting(on) },
        onSetLimit = { minutes -> viewModel.setLimit(minutes) },
        onRemoveLimit = { viewModel.removeLimit() },
        onAppInfo = {
            val key = state.appKey
            if (key != null) {
                container.appLauncher.openAppInfo(key)
            } else {
                context.startSafely(SystemIntents.appDetails(packageName))
            }
        },
        onGrantUsage = { context.startFirstAvailable(SystemIntents.usageAccessSettings(context)) },
    )
}
