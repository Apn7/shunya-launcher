package dev.apn7.shunya.feature.home.drawer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.ShunyaTextField
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.home.drawer.logic.DrawerRow

/** Room kept free on the right of the list for the fast scroller. */
private val ScrollerSpace = 32.dp

/** Search field at the top of the drawer: IME "Go" opens the top result, "Clear" empties it. */
@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onGo: () -> Unit,
) {
    ShunyaTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.drawer_search_placeholder),
        textStyle = ShunyaTheme.typography.listItem,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onGo() }),
        trailing = if (query.isEmpty()) {
            null
        } else {
            {
                ShunyaTextButton(
                    text = stringResource(R.string.common_clear),
                    onClick = { onQueryChange("") },
                    style = ShunyaButtonStyle.Secondary,
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s)
            .focusRequester(focusRequester),
    )
}

/** Every visible app in drawer order, with optional letter headers. */
@Composable
internal fun AppList(
    state: DrawerUiState,
    listState: LazyListState,
    reserveScrollerSpace: Boolean,
    onLaunch: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
) {
    if (state.apps.isEmpty()) {
        EmptyState(title = stringResource(if (state.isLoaded) R.string.drawer_empty else R.string.drawer_loading))
        return
    }
    val rows = state.layout.rows
    val apps = state.apps
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(end = if (reserveScrollerSpace) ScrollerSpace else 0.dp, bottom = Spacing.xl),
    ) {
        items(
            count = rows.size,
            key = { index -> rowKey(rows[index], apps, index) },
            contentType = { index -> if (rows[index] is DrawerRow.Header) CONTENT_HEADER else CONTENT_APP },
        ) { index ->
            when (val row = rows[index]) {
                is DrawerRow.Header -> SectionLetter(row.letter)
                is DrawerRow.App -> {
                    val app = apps[row.appIndex]
                    AppRow(app = app, onClick = { onLaunch(app) }, onLongClick = { onAppLongClick(app) })
                }
            }
        }
    }
}

/** Calculator, matching apps (best first), then web and Play Store search for [query]. */
@Composable
internal fun SearchResultsList(
    state: DrawerUiState,
    query: String,
    listState: LazyListState,
    onLaunch: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onCopy: (String) -> Unit,
    onWebSearch: (String) -> Unit,
    onStoreSearch: (String) -> Unit,
) {
    val results = state.results
    val calculation = state.calculation
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xl),
    ) {
        if (calculation != null) {
            item(key = "calculator") { CalculatorRow(result = calculation, onCopy = { onCopy(calculation) }) }
        }
        items(count = results.size, key = { index -> results[index].key.id }) { index ->
            val app = results[index]
            AppRow(app = app, onClick = { onLaunch(app) }, onLongClick = { onAppLongClick(app) })
        }
        if (results.isEmpty() && calculation == null) {
            item(key = "no-results") {
                Text(
                    text = stringResource(R.string.search_no_results, query),
                    style = ShunyaTheme.typography.bodySmall,
                    color = ShunyaTheme.colors.tertiary,
                    modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.m),
                )
            }
        }
        item(key = "web") {
            SearchActionRow(text = stringResource(R.string.search_web, query), onClick = { onWebSearch(query) })
        }
        item(key = "store") {
            SearchActionRow(text = stringResource(R.string.search_play_store, query), onClick = { onStoreSearch(query) })
        }
    }
}

private const val CONTENT_HEADER = 0
private const val CONTENT_APP = 1

/** Stable, unique list keys: app ids never look like "header-3". */
private fun rowKey(row: DrawerRow, apps: List<LauncherApp>, index: Int): Any = when (row) {
    is DrawerRow.Header -> "header-$index"
    is DrawerRow.App -> apps[row.appIndex].key.id
}
