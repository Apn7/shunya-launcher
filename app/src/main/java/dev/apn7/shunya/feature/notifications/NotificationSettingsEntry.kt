package dev.apn7.shunya.feature.notifications

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

/** Entry point of [Route.NotificationSettings]. */
@Composable
fun NotificationSettingsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel = viewModel {
        NotificationSettingsViewModel(container.settingsRepository, container.permissionsRepository, container.notificationInbox)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { container.permissionsRepository.refresh() }

    NotificationSettingsScreen(
        state = state,
        onBack = navigator::back,
        onModeChange = { mode -> viewModel.setMode(mode) },
        onGrantAccess = { context.startFirstAvailable(SystemIntents.notificationListenerSettings(context)) },
        onOpenAllowedApps = { navigator.navigate(Route.AllowedNotificationApps) },
        onOpenInbox = { navigator.navigate(Route.Inbox) },
    )
}
