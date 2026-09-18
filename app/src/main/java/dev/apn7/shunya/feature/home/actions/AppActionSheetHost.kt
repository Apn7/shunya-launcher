package dev.apn7.shunya.feature.home.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ChoiceDialog
import dev.apn7.shunya.core.designsystem.durationText
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.AppOverrides
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.AppUsage
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.model.LauncherSettings
import dev.apn7.shunya.feature.home.components.TextInputDialog

/** What the host shows: the sheet, or one of the dialogs the sheet leads to. */
private enum class ActionStep { Sheet, Rename, DailyLimit }

/**
 * Shows the action sheet for the app with [appKey] and the rename / daily-limit dialogs it opens.
 * Reads the live app (renames and home changes show at once) and closes itself if the app is
 * uninstalled meanwhile. [onDone] runs when everything is closed.
 */
@Composable
internal fun AppActionSheetHost(appKey: AppKey, onDone: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val controller = remember(context, container) { AppActionsController(context, container) }
    val allApps: List<LauncherApp> by container.appsRepository.allApps.collectAsStateWithLifecycle()
    val app: LauncherApp? = allApps.firstOrNull { it.key == appKey }
    if (app == null) {
        LaunchedEffect(appKey) { onDone() }
        return
    }
    val settings: LauncherSettings by container.settingsRepository.settings.collectAsStateWithLifecycle()
    val overrides: AppOverrides by container.appOverridesRepository.overrides.collectAsStateWithLifecycle()
    val focusConfig: FocusConfig by container.focusConfigRepository.config.collectAsStateWithLifecycle()
    val usage by produceState<AppUsage?>(null, appKey) { value = controller.usageToday(app) }
    val shortcuts by produceState(emptyList<AppShortcut>(), appKey) { value = controller.shortcutsOf(app) }
    var step by remember { mutableStateOf(ActionStep.Sheet) }
    // Where to go once the sheet has finished closing; null ends everything.
    var next by remember { mutableStateOf<ActionStep?>(null) }

    when (step) {
        ActionStep.Sheet -> {
            val maxFavorites = settings.home.maxFavorites
            AppActionSheet(
                state = AppActionState(
                    app = app,
                    usage = usage,
                    favoritesFull = overrides.isFull(maxFavorites),
                    maxFavorites = maxFavorites,
                    isDistracting = focusConfig.isDistracting(app.packageName),
                    limitMinutes = focusConfig.limitMinutesFor(app.packageName),
                    shortcuts = shortcuts,
                ),
                callbacks = AppActionCallbacks(
                    onRename = { next = ActionStep.Rename },
                    onToggleFavorite = { controller.setFavorite(app, favorite = !app.isFavorite) },
                    onHide = { controller.hide(app) },
                    onToggleDistracting = { controller.setDistracting(app, !focusConfig.isDistracting(app.packageName)) },
                    onDailyLimit = { next = ActionStep.DailyLimit },
                    onShortcut = { shortcut -> controller.openShortcut(app, shortcut) },
                    onAppInfo = { controller.openAppInfo(app) },
                    onUninstall = { controller.uninstall(app) },
                ),
                onDismiss = {
                    val target = next
                    if (target == null) onDone() else step = target
                },
            )
        }
        ActionStep.Rename -> RenameDialog(app = app, controller = controller, onDone = onDone)
        ActionStep.DailyLimit -> DailyLimitDialog(
            app = app,
            current = focusConfig.limitMinutesFor(app.packageName) ?: 0,
            onSelect = { minutes ->
                controller.setDailyLimit(app, minutes)
                onDone()
            },
            onDismiss = onDone,
        )
    }
}

@Composable
private fun RenameDialog(app: LauncherApp, controller: AppActionsController, onDone: () -> Unit) {
    TextInputDialog(
        title = stringResource(R.string.apps_rename_title, app.displayLabel),
        initial = app.displayLabel,
        placeholder = app.label,
        summary = if (app.customLabel != null) stringResource(R.string.apps_original_name, app.label) else null,
        capitalization = KeyboardCapitalization.Words,
        neutralText = if (app.customLabel != null) stringResource(R.string.apps_rename_reset) else null,
        onNeutral = {
            controller.rename(app, null)
            onDone()
        },
        onSave = { text ->
            // Saving the system name (or nothing) clears the rename instead of storing a copy.
            val clean = text.trim()
            controller.rename(app, if (clean.isEmpty() || clean == app.label) null else clean)
            onDone()
        },
        onDismiss = onDone,
    )
}

@Composable
private fun DailyLimitDialog(app: LauncherApp, current: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    // A limit set elsewhere (e.g. 20 min in Focus settings) is offered too, so it shows as selected.
    val options = if (current in DailyLimitPresets) DailyLimitPresets else (DailyLimitPresets + current).sorted()
    val off = stringResource(R.string.common_off)
    ChoiceDialog(
        title = stringResource(R.string.apps_limit_title, app.displayLabel),
        options = options,
        selected = current,
        optionLabel = { minutes -> if (minutes == 0) off else durationText(minutes * 60_000L) },
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}
