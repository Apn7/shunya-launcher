package dev.apn7.shunya.feature.settings.gestures

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
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextField
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.settings.logic.AppSearch

/** Full-screen, searchable single-app picker ("Open app…" gestures). */
@Composable
internal fun AppPickerScreen(
    apps: List<LauncherApp>,
    onPick: (LauncherApp) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(apps, query) { AppSearch.filter(apps, query) { it.displayLabel } }
    val workTag = stringResource(R.string.settings_work_tag)
    ShunyaScreen(title = stringResource(R.string.settings_pick_app), onBack = onBack, scrollable = false) {
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
            items(filtered, key = { it.key.id }) { app ->
                SettingsRow(
                    title = app.displayLabel,
                    value = if (app.isWork) workTag else null,
                    onClick = { onPick(app) },
                )
            }
        }
    }
}
