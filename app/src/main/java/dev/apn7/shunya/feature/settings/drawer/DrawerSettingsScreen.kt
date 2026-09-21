package dev.apn7.shunya.feature.settings.drawer

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ChoiceRow
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.SwitchRow
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.DrawerPrefs
import dev.apn7.shunya.core.model.DrawerSort
import dev.apn7.shunya.feature.settings.SettingsNote
import dev.apn7.shunya.feature.settings.drawerSortLabel

/** Settings > App drawer: sort, section letters, keyboard, auto-launch, work apps, hidden apps. */
@Composable
internal fun DrawerSettingsScreen(
    state: DrawerSettingsUiState,
    onBack: () -> Unit,
    onUpdate: ((DrawerPrefs) -> DrawerPrefs) -> Unit,
    onShowAppsButtonChange: (Boolean) -> Unit,
    onGrantUsageAccess: () -> Unit,
    onOpenHiddenApps: () -> Unit,
) {
    val drawer = state.drawer
    val resources = LocalContext.current.resources
    ShunyaScreen(title = stringResource(R.string.settings_drawer), onBack = onBack) {
        ChoiceRow(
            title = stringResource(R.string.settings_drawer_sort),
            options = DrawerSort.entries,
            selected = drawer.sort,
            optionLabel = { drawerSortLabel(it) },
            onSelect = { choice -> onUpdate { it.copy(sort = choice) } },
        )
        if (drawer.sort == DrawerSort.MostUsed && !state.hasUsageAccess) {
            SettingsNote(stringResource(R.string.settings_drawer_sort_needs_usage))
            ShunyaTextButton(
                text = stringResource(R.string.core_permission_grant),
                onClick = onGrantUsageAccess,
                modifier = Modifier.padding(horizontal = Spacing.s),
            )
        }
        SwitchRow(
            title = stringResource(R.string.settings_drawer_section_letters),
            summary = stringResource(R.string.settings_drawer_section_letters_summary),
            checked = drawer.showSectionLetters,
            onCheckedChange = { on -> onUpdate { it.copy(showSectionLetters = on) } },
        )
        SwitchRow(
            title = stringResource(R.string.settings_drawer_auto_keyboard),
            summary = stringResource(R.string.settings_drawer_auto_keyboard_summary),
            checked = drawer.autoShowKeyboard,
            onCheckedChange = { on -> onUpdate { it.copy(autoShowKeyboard = on) } },
        )
        SwitchRow(
            title = stringResource(R.string.settings_drawer_auto_launch),
            summary = stringResource(R.string.settings_drawer_auto_launch_summary),
            checked = drawer.autoLaunchSingleMatch,
            onCheckedChange = { on -> onUpdate { it.copy(autoLaunchSingleMatch = on) } },
        )
        SwitchRow(
            title = stringResource(R.string.settings_drawer_work_apps),
            summary = stringResource(R.string.settings_drawer_work_apps_summary),
            checked = drawer.showWorkApps,
            onCheckedChange = { on -> onUpdate { it.copy(showWorkApps = on) } },
        )
        SwitchRow(
            title = stringResource(R.string.settings_drawer_apps_button),
            summary = stringResource(R.string.settings_drawer_apps_button_summary),
            checked = state.showAppsButton,
            onCheckedChange = onShowAppsButtonChange,
        )
        SettingsRow(
            title = stringResource(R.string.settings_hidden_apps),
            summary = if (state.hiddenCount == 0) {
                stringResource(R.string.settings_hidden_apps_none)
            } else {
                resources.getQuantityString(R.plurals.settings_hidden_apps_count, state.hiddenCount, state.hiddenCount)
            },
            onClick = onOpenHiddenApps,
        )
    }
}
