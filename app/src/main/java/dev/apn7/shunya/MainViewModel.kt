package dev.apn7.shunya

import androidx.lifecycle.ViewModel
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route

/** Keeps the back stack across configuration changes (e.g. a language switch recreates the activity). */
class MainViewModel : ViewModel() {

    val navigator: Navigator = Navigator(Route.Home)

    private var onboardingChecked = false

    /** Shows onboarding on top of home once per process, when it was never finished or skipped. */
    fun showOnboardingIfNeeded(onboardingDone: Boolean) {
        if (onboardingChecked) return
        onboardingChecked = true
        if (!onboardingDone) navigator.navigate(Route.Onboarding)
    }

    override fun onCleared() {
        navigator.clearAll()
    }
}
