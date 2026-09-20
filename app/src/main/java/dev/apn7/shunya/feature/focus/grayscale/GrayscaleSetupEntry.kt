package dev.apn7.shunya.feature.focus.grayscale

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.SwitchRow
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.feature.focus.ui.Paragraph

/** Entry point of [dev.apn7.shunya.core.navigation.Route.GrayscaleSetup]: the one-time ADB step, copy button, live status, switch. */
@Composable
fun GrayscaleSetupEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val grayscale = container.grayscaleController
    val enabled: Boolean by grayscale.isEnabled.collectAsStateWithLifecycle()
    var available: Boolean by remember { mutableStateOf(grayscale.isAvailable()) }
    var copied: Boolean by remember { mutableStateOf(false) }
    // The permission is granted from a computer while this screen waits: re-check whenever it comes back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        available = grayscale.isAvailable()
        container.permissionsRepository.refresh()
    }
    val clipLabel = stringResource(R.string.focus_grayscale_clip_label)
    GrayscaleSetupScreen(
        available = available,
        enabled = enabled,
        command = grayscale.adbGrantCommand,
        copied = copied,
        onBack = navigator::back,
        onToggle = { on -> grayscale.setEnabled(on) },
        onCopy = {
            copyToClipboard(context, clipLabel, grayscale.adbGrantCommand)
            copied = true
        },
    )
}

@Composable
internal fun GrayscaleSetupScreen(
    available: Boolean,
    enabled: Boolean,
    command: String,
    copied: Boolean,
    onBack: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onCopy: () -> Unit,
) {
    val colors = ShunyaTheme.colors
    ShunyaScreen(title = stringResource(R.string.focus_grayscale_title), onBack = onBack) {
        Paragraph(
            text = stringResource(if (available) R.string.focus_grayscale_ready else R.string.focus_grayscale_needs_setup),
            style = ShunyaTheme.typography.label,
            color = colors.ink,
        )
        SwitchRow(
            title = stringResource(R.string.focus_grayscale_title),
            checked = enabled,
            onCheckedChange = onToggle,
            summary = stringResource(R.string.focus_grayscale_switch_summary),
            enabled = available,
        )

        if (!available) {
            SectionHeader(stringResource(R.string.focus_grayscale_setup_header))
            Paragraph(stringResource(R.string.focus_grayscale_step_1))
            Paragraph(stringResource(R.string.focus_grayscale_step_2))
            SelectionContainer {
                Text(
                    text = command,
                    style = ShunyaTheme.typography.bodySmall,
                    color = colors.ink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s)
                        .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
                        .padding(Spacing.m),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.s),
                horizontalArrangement = Arrangement.End,
            ) {
                ShunyaTextButton(
                    text = stringResource(if (copied) R.string.common_copied else R.string.common_copy),
                    onClick = onCopy,
                )
            }
            Paragraph(stringResource(R.string.focus_grayscale_step_3))
            Paragraph(stringResource(R.string.focus_grayscale_why), color = colors.tertiary)
        }
        Paragraph(
            text = stringResource(R.string.focus_grayscale_colour_note),
            color = colors.tertiary,
            modifier = Modifier.padding(top = Spacing.m, bottom = Spacing.l),
        )
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
