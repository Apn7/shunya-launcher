package dev.apn7.shunya.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.PermissionCard
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.PermissionStatus

/** Big title and a short paragraph at the top of a step. */
@Composable
internal fun StepHeader(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
    ) {
        Text(text = title, style = ShunyaTheme.typography.title, color = ShunyaTheme.colors.ink)
        Text(
            text = body,
            style = ShunyaTheme.typography.body,
            color = ShunyaTheme.colors.secondary,
            modifier = Modifier.padding(top = Spacing.s),
        )
    }
}

/** Name, meaning and tagline: "Zero noise." */
@Composable
internal fun WelcomeStep() {
    val colors = ShunyaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xl),
    ) {
        Text(text = stringResource(R.string.app_name), style = ShunyaTheme.typography.clockMedium, color = colors.ink)
        Text(text = stringResource(R.string.onboarding_name_bangla), style = ShunyaTheme.typography.title, color = colors.secondary)
        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = ShunyaTheme.typography.listItem,
            color = colors.ink,
            modifier = Modifier.padding(top = Spacing.l),
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_body),
            style = ShunyaTheme.typography.body,
            color = colors.secondary,
            modifier = Modifier.padding(top = Spacing.m),
        )
    }
}

/** Why Shunya wants to be the home app, and whether it already is. The button lives in the bottom bar. */
@Composable
internal fun DefaultLauncherStep(isDefaultLauncher: Boolean) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        StepHeader(
            title = stringResource(R.string.onboarding_default_title),
            body = stringResource(R.string.onboarding_default_body),
        )
        Text(
            text = stringResource(if (isDefaultLauncher) R.string.onboarding_default_done else R.string.onboarding_default_not_yet),
            style = ShunyaTheme.typography.label,
            color = if (isDefaultLauncher) ShunyaTheme.colors.ink else ShunyaTheme.colors.secondary,
            modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.m),
        )
    }
}

/** The two optional accesses, each with an honest explanation. */
@Composable
internal fun PermissionsStep(permissions: PermissionStatus, onUsageAccess: () -> Unit, onNotificationAccess: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        StepHeader(
            title = stringResource(R.string.onboarding_permissions_title),
            body = stringResource(R.string.onboarding_permissions_body),
        )
        PermissionCard(
            title = stringResource(R.string.settings_perm_usage),
            description = stringResource(R.string.onboarding_usage_why),
            granted = permissions.hasUsageAccess,
            onGrant = onUsageAccess,
        )
        PermissionCard(
            title = stringResource(R.string.settings_perm_notifications),
            description = stringResource(R.string.onboarding_notifications_why),
            granted = permissions.hasNotificationAccess,
            onGrant = onNotificationAccess,
        )
    }
}

@Composable
internal fun DoneStep() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        StepHeader(
            title = stringResource(R.string.onboarding_done_title),
            body = stringResource(R.string.onboarding_done_body),
        )
    }
}
