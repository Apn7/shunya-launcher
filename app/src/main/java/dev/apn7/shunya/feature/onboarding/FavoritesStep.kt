package dev.apn7.shunya.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
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
import dev.apn7.shunya.core.designsystem.component.ShunyaTextField
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.onboarding.logic.FavoriteSelection
import dev.apn7.shunya.feature.settings.logic.AppSearch

/** Searchable multi-select of home favorites, up to [max]; picked apps can always be unticked. */
@Composable
internal fun FavoritesStep(apps: List<LauncherApp>, selected: List<String>, max: Int, onToggle: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(apps, query) { AppSearch.filter(apps, query) { it.displayLabel } }
    val full = FavoriteSelection.isFull(selected, max)
    Column(modifier = Modifier.fillMaxSize()) {
        StepHeader(
            title = stringResource(R.string.onboarding_favorites_title),
            body = stringResource(R.string.onboarding_favorites_body, max),
        )
        Text(
            text = stringResource(R.string.onboarding_favorites_count, selected.size, max),
            style = ShunyaTheme.typography.caption,
            color = ShunyaTheme.colors.tertiary,
            modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
        )
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
                val id = app.key.id
                val checked = id in selected
                CheckRow(
                    title = app.displayLabel,
                    checked = checked,
                    onCheckedChange = { onToggle(id) },
                    enabled = checked || !full,
                )
            }
        }
    }
}
