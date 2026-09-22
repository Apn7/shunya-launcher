package dev.apn7.shunya.feature.onboarding

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
import dev.apn7.shunya.core.system.rememberDefaultLauncherRequest
import dev.apn7.shunya.core.system.startFirstAvailable

/**
 * Entry point of [Route.Onboarding]: pushed on top of Home at first start (and from About).
 * Finishing or skipping marks onboarding done and pops back; it never blocks the home screen.
 */
@Composable
fun OnboardingEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val permissions = container.permissionsRepository
    val viewModel = viewModel {
        OnboardingViewModel(container.settingsRepository, container.appOverridesRepository, container.appsRepository, permissions)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { permissions.refresh() }
    val requestDefaultLauncher = rememberDefaultLauncherRequest { permissions.refresh() }
    val finish: () -> Unit = {
        viewModel.markDone()
        if (navigator.current == Route.Onboarding) navigator.back()
    }

    OnboardingScreen(
        state = state,
        actions = OnboardingActions(
            onContinue = { step -> viewModel.continueFrom(step) },
            onSkip = { step -> viewModel.skip(step) },
            onBack = { if (!viewModel.back()) finish() },
            onFinish = finish,
            onTheme = { theme -> viewModel.setTheme(theme) },
            onFont = { font -> viewModel.setFont(font) },
            onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
            onSetDefaultLauncher = requestDefaultLauncher,
            onUsageAccess = { context.startFirstAvailable(SystemIntents.usageAccessSettings(context)) },
            onNotificationAccess = { context.startFirstAvailable(SystemIntents.notificationListenerSettings(context)) },
        ),
    )
}
