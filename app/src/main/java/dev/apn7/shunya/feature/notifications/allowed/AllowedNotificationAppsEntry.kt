package dev.apn7.shunya.feature.notifications.allowed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator

/** Entry point of [dev.apn7.shunya.core.navigation.Route.AllowedNotificationApps]. */
@Composable
fun AllowedNotificationAppsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val viewModel = viewModel {
        AllowedNotificationAppsViewModel(container.appContext, container.settingsRepository, container.appsRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    AllowedNotificationAppsScreen(
        state = state,
        onBack = navigator::back,
        onToggle = { packageName, allowed -> viewModel.setAllowed(packageName, allowed) },
        onReset = { viewModel.resetToDefaults() },
    )
}
