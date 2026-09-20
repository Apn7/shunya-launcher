package dev.apn7.shunya.feature.focus.hub

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
import dev.apn7.shunya.core.model.PermissionStatus
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startFirstAvailable

/** Entry point of [dev.apn7.shunya.core.navigation.Route.FocusHub] (signature fixed by `ShunyaNavHost`). */
@Composable
fun FocusHubEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel = viewModel {
        FocusHubViewModel(
            configRepository = container.focusConfigRepository,
            settingsRepository = container.settingsRepository,
            focusController = container.focusController,
            grayscale = container.grayscaleController,
        )
    }
    val state: FocusHubUiState by viewModel.state.collectAsStateWithLifecycle()
    val permissions: PermissionStatus by container.permissionsRepository.status.collectAsStateWithLifecycle()
    var grayscaleAvailable: Boolean by remember { mutableStateOf(container.grayscaleController.isAvailable()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        container.permissionsRepository.refresh()
        grayscaleAvailable = container.grayscaleController.isAvailable()
    }
    FocusHubScreen(
        state = state,
        hasUsageAccess = permissions.hasUsageAccess,
        accessibilityOn = permissions.isAccessibilityEnabled,
        grayscaleAvailable = grayscaleAvailable,
        onBack = navigator::back,
        onStart = { minutes -> viewModel.startSession(minutes) },
        onEnd = { viewModel.endSession() },
        onNavigate = { route -> navigator.navigate(route) },
        onBlockingMode = { mode -> viewModel.setBlockingMode(mode) },
        onGrayscale = { on -> viewModel.setGrayscale(on) },
        onGrantUsage = { context.startFirstAvailable(SystemIntents.usageAccessSettings(context)) },
    )
}
