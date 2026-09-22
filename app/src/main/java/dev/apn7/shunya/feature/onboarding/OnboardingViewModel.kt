package dev.apn7.shunya.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.AppOverridesRepository
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.AppearancePrefs
import dev.apn7.shunya.core.model.FontChoice
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.model.PermissionStatus
import dev.apn7.shunya.core.model.ThemeChoice
import dev.apn7.shunya.core.system.PermissionsRepository
import dev.apn7.shunya.feature.onboarding.logic.FavoriteSelection
import dev.apn7.shunya.feature.onboarding.logic.OnboardingFlow
import dev.apn7.shunya.feature.onboarding.logic.OnboardingStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val appearance: AppearancePrefs = AppearancePrefs(),
    /** Apps the favorites step offers (the drawer's list). */
    val apps: List<LauncherApp> = emptyList(),
    /** Picked favorites as [AppKey.id]s, in pick order. */
    val selected: List<String> = emptyList(),
    val maxFavorites: Int = 0,
    val permissions: PermissionStatus = PermissionStatus(),
)

/**
 * First-run setup (PRD 3.6). Theme and font are written as they are tapped (the whole app
 * re-themes live); favorites are written when the user continues from their step. Finishing or
 * skipping marks onboarding done; the entry then leaves with `navigator.back()`.
 */
class OnboardingViewModel(
    private val settings: SettingsRepository,
    private val overrides: AppOverridesRepository,
    apps: AppsRepository,
    permissions: PermissionsRepository,
) : ViewModel() {

    private val step = MutableStateFlow(OnboardingStep.Welcome)
    private val selection = MutableStateFlow(overrides.overrides.value.favorites)

    val state: StateFlow<OnboardingUiState> = combine(
        step,
        selection,
        settings.settings,
        apps.visibleApps,
        permissions.status,
    ) { currentStep, picked, current, visible, status ->
        OnboardingUiState(
            step = currentStep,
            appearance = current.appearance,
            apps = visible,
            selected = picked,
            maxFavorites = current.home.maxFavorites,
            permissions = status,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState())

    /** Primary action of a step: keeps what the step collected, then moves on. */
    fun continueFrom(current: OnboardingStep) {
        if (current == OnboardingStep.Favorites) saveFavorites()
        moveAfter(current)
    }

    /** Moves on without keeping the step's input. */
    fun skip(current: OnboardingStep) {
        moveAfter(current)
    }

    /** Back one step; false on the first step (the caller then leaves onboarding). */
    fun back(): Boolean {
        val previous = OnboardingFlow.previous(step.value) ?: return false
        step.value = previous
        return true
    }

    fun setTheme(theme: ThemeChoice) {
        settings.edit { it.copy(appearance = it.appearance.copy(theme = theme)) }
    }

    fun setFont(font: FontChoice) {
        settings.edit { it.copy(appearance = it.appearance.copy(font = font)) }
    }

    fun toggleFavorite(id: String) {
        val max = settings.settings.value.home.maxFavorites
        selection.value = FavoriteSelection.toggled(selection.value, id, max)
    }

    /** Marks onboarding as done (finished or skipped); written on the app scope, so it survives leaving. */
    fun markDone() {
        settings.edit { it.copy(onboardingDone = true) }
    }

    private fun moveAfter(current: OnboardingStep) {
        step.value = OnboardingFlow.next(current) ?: OnboardingStep.Done
    }

    private fun saveFavorites() {
        val keys = selection.value.mapNotNull { AppKey.fromId(it) }
        val max = settings.settings.value.home.maxFavorites
        overrides.edit { it.withFavorites(keys, max) }
    }
}
