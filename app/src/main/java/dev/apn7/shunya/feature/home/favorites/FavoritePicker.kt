package dev.apn7.shunya.feature.home.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextField
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.home.search.logic.FuzzyMatcher
import dev.apn7.shunya.feature.home.search.logic.SearchKey

/** "Add app" for Settings > Home: every app not on home yet, searchable with the drawer's ranking. */
@Composable
internal fun FavoritePicker(
    candidates: List<LauncherApp>,
    onPick: (LauncherApp) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val keys: Map<AppKey, List<SearchKey>> = remember(candidates) {
        candidates.associate { app -> app.key to listOf(SearchKey(app.displayLabel), SearchKey(app.label)) }
    }
    val shown: List<LauncherApp> = remember(query, keys, candidates) {
        FuzzyMatcher.rank(query, candidates) { app -> keys[app.key] ?: listOf(SearchKey(app.displayLabel)) }
    }
    ShunyaScreen(
        title = stringResource(R.string.home_settings_picker_title),
        onBack = onBack,
        scrollable = false,
    ) {
        ShunyaTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = stringResource(R.string.drawer_search_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal),
        )
        if (shown.isEmpty()) {
            EmptyState(title = stringResource(R.string.drawer_empty))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(count = shown.size, key = { index -> shown[index].key.id }) { index ->
                    val app = shown[index]
                    SettingsRow(title = app.displayLabel, onClick = { onPick(app) })
                }
            }
        }
    }
}
