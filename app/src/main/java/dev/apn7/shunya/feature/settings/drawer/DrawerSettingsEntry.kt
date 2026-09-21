package dev.apn7.shunya.feature.settings.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startFirstAvailable

/** Entry point of [Route.DrawerSettings]. */
@Composable
fun DrawerSettingsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel = viewModel {
        DrawerSettingsViewModel(container.settingsRepository, container.appsRepository, container.permissionsRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    DrawerSettingsScreen(
        state = state,
        onBack = navigator::back,
        onUpdate = { transform -> viewModel.update(transform) },
        onShowAppsButtonChange = { show -> viewModel.setShowAppsButton(show) },
        onGrantUsageAccess = { context.startFirstAvailable(SystemIntents.usageAccessSettings(context)) },
        onOpenHiddenApps = { navigator.navigate(Route.HiddenApps) },
    )
}
