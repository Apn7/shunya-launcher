package dev.apn7.shunya.feature.home.actions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SheetAction
import dev.apn7.shunya.core.designsystem.component.ShunyaBottomSheet
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaDivider
import dev.apn7.shunya.core.designsystem.durationText
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.AppUsage
import dev.apn7.shunya.core.model.LauncherApp

/** What the action sheet shows about one app. */
@Immutable
internal data class AppActionState(
    val app: LauncherApp,
    val usage: AppUsage?,
    val favoritesFull: Boolean,
    val maxFavorites: Int,
    val isDistracting: Boolean,
    val limitMinutes: Int?,
    val shortcuts: List<AppShortcut>,
)

/** Choices of the sheet. Each runs after the sheet has started closing. */
internal class AppActionCallbacks(
    val onRename: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onHide: () -> Unit,
    val onToggleDistracting: () -> Unit,
    val onDailyLimit: () -> Unit,
    val onShortcut: (AppShortcut) -> Unit,
    val onAppInfo: () -> Unit,
    val onUninstall: () -> Unit,
)

/**
 * Long-press on an app (drawer, search or home): shortcuts, rename, home, hide, distracting,
 * daily limit, app info and uninstall (not for system apps), with today's usage on top.
 */
@Composable
internal fun AppActionSheet(state: AppActionState, callbacks: AppActionCallbacks, onDismiss: () -> Unit) {
    val app = state.app
    ShunyaBottomSheet(onDismiss = onDismiss) { close ->
        fun choose(action: () -> Unit): () -> Unit = {
            close()
            action()
        }
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SheetHeader(state)
            if (state.shortcuts.isNotEmpty()) {
                SheetSectionTitle(stringResource(R.string.apps_shortcuts))
                state.shortcuts.forEach { shortcut ->
                    SheetAction(text = shortcut.label, onClick = choose { callbacks.onShortcut(shortcut) })
                }
                ShunyaDivider(modifier = Modifier.padding(vertical = Spacing.s))
            }
            SheetAction(text = stringResource(R.string.apps_action_rename), onClick = choose(callbacks.onRename))
            val canAdd = app.isFavorite || !state.favoritesFull
            SheetAction(
                text = stringResource(if (app.isFavorite) R.string.apps_action_remove_home else R.string.apps_action_add_home),
                summary = if (canAdd) null else stringResource(R.string.apps_home_full, state.maxFavorites),
                enabled = canAdd,
                onClick = choose(callbacks.onToggleFavorite),
            )
            SheetAction(text = stringResource(R.string.apps_action_hide), onClick = choose(callbacks.onHide))
            SheetAction(
                text = stringResource(
                    if (state.isDistracting) R.string.apps_action_distracting_off else R.string.apps_action_distracting_on,
                ),
                summary = if (state.isDistracting) null else stringResource(R.string.apps_distracting_summary),
                onClick = choose(callbacks.onToggleDistracting),
            )
            val limit = state.limitMinutes
            SheetAction(
                text = stringResource(R.string.apps_action_limit),
                summary = if (limit == null) {
                    stringResource(R.string.common_off)
                } else {
                    stringResource(R.string.apps_limit_value, durationText(limit * MINUTE_MILLIS))
                },
                onClick = choose(callbacks.onDailyLimit),
            )
            SheetAction(text = stringResource(R.string.apps_action_info), onClick = choose(callbacks.onAppInfo))
            if (!app.isSystem) {
                SheetAction(
                    text = stringResource(R.string.apps_action_uninstall),
                    style = ShunyaButtonStyle.Danger,
                    onClick = choose(callbacks.onUninstall),
                )
            }
        }
    }
}

private const val MINUTE_MILLIS = 60_000L

@Composable
private fun SheetHeader(state: AppActionState) {
    val app = state.app
    val colors = ShunyaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(bottom = Spacing.s),
    ) {
        Text(
            text = app.displayLabel,
            style = ShunyaTheme.typography.listItem,
            color = colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        if (app.customLabel != null) {
            Text(
                text = stringResource(R.string.apps_original_name, app.label),
                style = ShunyaTheme.typography.bodySmall,
                color = colors.secondary,
            )
        }
        val usage = state.usage
        if (usage != null) {
            Text(
                text = stringResource(R.string.apps_usage_today, durationText(usage.foregroundMillis), usage.launchCount),
                style = ShunyaTheme.typography.bodySmall,
                color = colors.secondary,
            )
        }
    }
}

@Composable
private fun SheetSectionTitle(text: String) {
    Text(
        text = text,
        style = ShunyaTheme.typography.section,
        color = ShunyaTheme.colors.tertiary,
        modifier = Modifier
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs)
            .semantics { heading() },
    )
}
