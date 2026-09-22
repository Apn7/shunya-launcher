package dev.apn7.shunya.feature.notifications.allowed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.CheckRow
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.ShunyaTextField
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.feature.settings.SettingsNote
import dev.apn7.shunya.feature.settings.logic.AppSearch

/** Searchable checkbox list of apps whose notifications are never held; built-in defaults are tagged. */
@Composable
internal fun AllowedNotificationAppsScreen(
    state: AllowedAppsUiState,
    onBack: () -> Unit,
    onToggle: (packageName: String, allowed: Boolean) -> Unit,
    onReset: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(state.apps, query) { AppSearch.filter(state.apps, query) { it.displayLabel } }
    val allowed = state.allowed
    val builtInTag = stringResource(R.string.notifications_allowed_default_tag)
    ShunyaScreen(
        title = stringResource(R.string.notifications_allowed),
        onBack = onBack,
        scrollable = false,
        actions = {
            if (state.customised != null) {
                ShunyaTextButton(
                    text = stringResource(R.string.notifications_allowed_reset),
                    onClick = onReset,
                    style = ShunyaButtonStyle.Secondary,
                )
            }
        },
    ) {
        SettingsNote(stringResource(R.string.notifications_allowed_intro))
        if (state.customised == null) {
            SettingsNote(stringResource(R.string.notifications_allowed_using_defaults))
        }
        ShunyaTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = stringResource(R.string.settings_search_apps),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal),
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (filtered.isEmpty()) {
                item(key = "empty") { EmptyState(title = stringResource(R.string.settings_no_apps_found)) }
            }
            items(filtered, key = { it.packageName }) { app ->
                CheckRow(
                    title = app.displayLabel,
                    checked = app.packageName in allowed,
                    onCheckedChange = { on -> onToggle(app.packageName, on) },
                    summary = if (app.packageName in state.defaults) builtInTag else null,
                    enabled = state.defaultsReady,
                )
            }
        }
    }
}
