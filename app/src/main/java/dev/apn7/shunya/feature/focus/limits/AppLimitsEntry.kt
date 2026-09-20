package dev.apn7.shunya.feature.focus.limits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startFirstAvailable

/** Entry point of [dev.apn7.shunya.core.navigation.Route.AppLimits] (signature fixed by `ShunyaNavHost`). */
@Composable
fun AppLimitsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel = viewModel {
        AppLimitsViewModel(container.appsRepository, container.focusConfigRepository, container.usageRepository)
    }
    val state: AppLimitsUiState by viewModel.state.collectAsStateWithLifecycle()
    var hasUsageAccess: Boolean by remember { mutableStateOf(container.usageRepository.hasAccess()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasUsageAccess = container.usageRepository.hasAccess()
        viewModel.refreshUsage()
    }
    AppLimitsScreen(
        state = state,
        hasUsageAccess = hasUsageAccess,
        onBack = navigator::back,
        onSetLimit = { pkg, minutes -> viewModel.setLimit(pkg, minutes) },
        onRemoveLimit = { pkg -> viewModel.removeLimit(pkg) },
        onGrantUsage = { context.startFirstAvailable(SystemIntents.usageAccessSettings(context)) },
    )
}
