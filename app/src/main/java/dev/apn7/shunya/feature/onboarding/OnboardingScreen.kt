package dev.apn7.shunya.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.Motion
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.FontChoice
import dev.apn7.shunya.core.model.ThemeChoice
import dev.apn7.shunya.feature.onboarding.logic.OnboardingFlow
import dev.apn7.shunya.feature.onboarding.logic.OnboardingStep

/** Everything the onboarding UI can ask for. */
internal class OnboardingActions(
    val onContinue: (OnboardingStep) -> Unit,
    val onSkip: (OnboardingStep) -> Unit,
    val onBack: () -> Unit,
    val onFinish: () -> Unit,
    val onTheme: (ThemeChoice) -> Unit,
    val onFont: (FontChoice) -> Unit,
    val onToggleFavorite: (String) -> Unit,
    val onSetDefaultLauncher: () -> Unit,
    val onUsageAccess: () -> Unit,
    val onNotificationAccess: () -> Unit,
)

/**
 * Onboarding frame: step counter and "Skip setup" on top, the step in the middle, "Skip" and the
 * step's main action at the bottom. Back goes one step back; on the first step it leaves setup.
 */
@Composable
internal fun OnboardingScreen(state: OnboardingUiState, actions: OnboardingActions) {
    BackHandler { actions.onBack() }
    val step = state.step
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShunyaTheme.colors.background)
            .safeDrawingPadding(),
    ) {
        TopBar(step = step, onSkipSetup = actions.onFinish)
        Crossfade(
            targetState = step,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            animationSpec = tween(Motion.MEDIUM_MILLIS),
            label = "onboarding-step",
        ) { shown ->
            StepContent(step = shown, state = state, actions = actions)
        }
        BottomBar(step = step, isDefaultLauncher = state.permissions.isDefaultLauncher, actions = actions)
    }
}

@Composable
private fun StepContent(step: OnboardingStep, state: OnboardingUiState, actions: OnboardingActions) {
    when (step) {
        OnboardingStep.Welcome -> WelcomeStep()
        OnboardingStep.Look -> LookStep(state.appearance, actions.onTheme, actions.onFont)
        OnboardingStep.Favorites -> FavoritesStep(state.apps, state.selected, state.maxFavorites, actions.onToggleFavorite)
        OnboardingStep.DefaultLauncher -> DefaultLauncherStep(state.permissions.isDefaultLauncher)
        OnboardingStep.Permissions -> PermissionsStep(state.permissions, actions.onUsageAccess, actions.onNotificationAccess)
        OnboardingStep.Done -> DoneStep()
    }
}

@Composable
private fun TopBar(step: OnboardingStep, onSkipSetup: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.minTouchTarget)
            .padding(horizontal = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_count, OnboardingFlow.position(step), OnboardingFlow.total),
            style = ShunyaTheme.typography.caption,
            color = ShunyaTheme.colors.tertiary,
            modifier = Modifier.padding(start = Spacing.m),
        )
        Spacer(modifier = Modifier.weight(1f))
        if (step != OnboardingStep.Done) {
            ShunyaTextButton(
                text = stringResource(R.string.onboarding_skip_setup),
                onClick = onSkipSetup,
                style = ShunyaButtonStyle.Secondary,
            )
        }
    }
}

@Composable
private fun BottomBar(step: OnboardingStep, isDefaultLauncher: Boolean, actions: OnboardingActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (step != OnboardingStep.Welcome && step != OnboardingStep.Done) {
            ShunyaTextButton(
                text = stringResource(R.string.common_skip),
                onClick = { actions.onSkip(step) },
                style = ShunyaButtonStyle.Secondary,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        val needsDefault = step == OnboardingStep.DefaultLauncher && !isDefaultLauncher
        val label = when {
            step == OnboardingStep.Welcome -> stringResource(R.string.onboarding_start)
            step == OnboardingStep.Done -> stringResource(R.string.onboarding_done_action)
            needsDefault -> stringResource(R.string.onboarding_default_action)
            else -> stringResource(R.string.common_continue)
        }
        ShunyaTextButton(
            text = label,
            onClick = {
                when {
                    step == OnboardingStep.Done -> actions.onFinish()
                    needsDefault -> actions.onSetDefaultLauncher()
                    else -> actions.onContinue(step)
                }
            },
            outlined = true,
        )
    }
}
