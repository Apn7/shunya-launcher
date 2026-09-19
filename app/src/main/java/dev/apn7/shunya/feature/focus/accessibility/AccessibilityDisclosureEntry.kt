package dev.apn7.shunya.feature.focus.accessibility

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.PermissionStatus
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startFirstAvailable
import dev.apn7.shunya.feature.focus.ui.Paragraph

/**
 * Entry point of [dev.apn7.shunya.core.navigation.Route.AccessibilityDisclosure]: the prominent
 * disclosure (Play policy style) shown before Shunya ever sends anyone to accessibility settings.
 */
@Composable
fun AccessibilityDisclosureEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val permissions: PermissionStatus by container.permissionsRepository.status.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { container.permissionsRepository.refresh() }
    AccessibilityDisclosureScreen(
        enabled = permissions.isAccessibilityEnabled,
        onBack = navigator::back,
        onOpenSettings = { context.startFirstAvailable(SystemIntents.accessibilitySettings(context)) },
    )
}

/** What the service does, what it sees, what it never does; then an explicit "Agree" to continue. */
@Composable
internal fun AccessibilityDisclosureScreen(
    enabled: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = ShunyaTheme.colors
    val serviceName = stringResource(R.string.focus_accessibility_label)
    ShunyaScreen(title = stringResource(R.string.focus_a11y_title), onBack = onBack) {
        Paragraph(
            text = stringResource(if (enabled) R.string.focus_a11y_status_on else R.string.focus_a11y_status_off),
            style = ShunyaTheme.typography.label,
            color = colors.ink,
        )
        Paragraph(stringResource(R.string.focus_a11y_intro))

        SectionHeader(stringResource(R.string.focus_a11y_does_header))
        Paragraph(stringResource(R.string.focus_a11y_does_lock))
        Paragraph(stringResource(R.string.focus_a11y_does_block))

        SectionHeader(stringResource(R.string.focus_a11y_sees_header))
        Paragraph(stringResource(R.string.focus_a11y_sees_body))

        SectionHeader(stringResource(R.string.focus_a11y_never_header))
        Paragraph(stringResource(R.string.focus_a11y_never_read))
        Paragraph(stringResource(R.string.focus_a11y_never_store))
        Paragraph(stringResource(R.string.focus_a11y_never_act))

        SectionHeader(stringResource(R.string.focus_a11y_control_header))
        Paragraph(stringResource(R.string.focus_a11y_without))
        Paragraph(stringResource(R.string.focus_a11y_steps, serviceName), color = colors.tertiary)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.s, vertical = Spacing.l),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ShunyaTextButton(text = stringResource(R.string.common_not_now), onClick = onBack, style = ShunyaButtonStyle.Secondary)
            ShunyaTextButton(
                text = stringResource(if (enabled) R.string.focus_a11y_manage else R.string.focus_a11y_agree),
                onClick = onOpenSettings,
                outlined = true,
            )
        }
    }
}
