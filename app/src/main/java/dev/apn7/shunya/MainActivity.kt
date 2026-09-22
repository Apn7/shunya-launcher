package dev.apn7.shunya

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.apn7.shunya.core.designsystem.applySystemBars
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route
import dev.apn7.shunya.core.navigation.ShunyaNavHost

/**
 * The home screen activity (HOME + LAUNCHER). Hosts the whole Compose UI: theme, dependency
 * container and navigation. Back never leaves it; Home returns to [dev.apn7.shunya.core.navigation.Route.Home].
 */
class MainActivity : ComponentActivity() {

    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySystemBars(darkTheme = isSystemDark())

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        navigator = viewModel.navigator
        val container = appContainer

        setContent {
            val settings by container.settingsRepository.settings.collectAsStateWithLifecycle()
            val settingsLoaded by container.settingsRepository.isLoaded.collectAsStateWithLifecycle()

            ShunyaTheme(appearance = settings.appearance) {
                val dark = ShunyaTheme.colors.isDark
                val showStatusBar = settings.appearance.showStatusBar
                LaunchedEffect(dark, showStatusBar) { applySystemBars(dark, showStatusBar) }

                CompositionLocalProvider(LocalAppContainer provides container) {
                    // Back never leaves home: this root handler swallows whatever no screen handled.
                    BackHandler {}
                    // Wait for the stored settings (a few ms on cold start) so the wrong theme never flashes.
                    if (settingsLoaded) {
                        LaunchedEffect(Unit) { viewModel.showOnboardingIfNeeded(settings.onboardingDone) }
                        // The window shows the wallpaper; without wallpaper mode paint the theme behind every screen.
                        val backdrop = if (settings.appearance.wallpaperMode) {
                            Modifier
                        } else {
                            Modifier.background(ShunyaTheme.colors.background)
                        }
                        Box(Modifier.fillMaxSize().then(backdrop)) {
                            ShunyaNavHost(navigator)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appContainer.permissionsRepository.refresh()
    }

    /** Home pressed while Shunya is the launcher: back to home, closing drawer, search and sheets. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action != Intent.ACTION_MAIN) return
        val wasAlreadyHome = hasWindowFocus() && (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) == 0
        // Leaving onboarding with Home counts as skipping it, so it shows on first launch only.
        if (Route.Onboarding in navigator.backStack) {
            appContainer.settingsRepository.edit { it.copy(onboardingDone = true) }
        }
        navigator.popToRoot()
        appContainer.homeEvents.onHomePressed(wasAlreadyHome)
    }

    private fun isSystemDark(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}
