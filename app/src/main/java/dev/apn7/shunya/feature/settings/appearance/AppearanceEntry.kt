package dev.apn7.shunya.feature.settings.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startSafely

/** Entry point of [dev.apn7.shunya.core.navigation.Route.Appearance]. */
@Composable
fun AppearanceEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel = viewModel { AppearanceViewModel(container.settingsRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val banglaUi = LocalConfiguration.current.locales.get(0)?.language == "bn"

    AppearanceScreen(
        state = state,
        banglaUi = banglaUi,
        onBack = navigator::back,
        onUpdate = { transform -> viewModel.update(transform) },
        onShowIntentionChange = { show -> viewModel.setShowIntention(show) },
        onChangeWallpaper = { context.startSafely(SystemIntents.setWallpaper()) },
    )
}
