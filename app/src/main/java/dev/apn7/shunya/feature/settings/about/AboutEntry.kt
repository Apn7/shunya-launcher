package dev.apn7.shunya.feature.settings.about

import androidx.compose.runtime.Composable
import dev.apn7.shunya.BuildConfig
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route

/** Entry point of [Route.About]. */
@Composable
fun AboutEntry(navigator: Navigator) {
    AboutScreen(
        version = BuildConfig.VERSION_NAME,
        onBack = navigator::back,
        onRunOnboarding = { navigator.navigate(Route.Onboarding) },
    )
}
