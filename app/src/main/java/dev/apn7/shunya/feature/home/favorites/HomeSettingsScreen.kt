package dev.apn7.shunya.feature.home.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ChoiceRow
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.SwitchRow
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.model.ProductLimits
import java.util.Locale

/** Settings > Home (stateless): the ordered favorites with their controls, then layout options. */
@Composable
internal fun HomeSettingsScreen(
    state: HomeSettingsUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onRename: (LauncherApp) -> Unit,
    onRemove: (LauncherApp) -> Unit,
    onMove: (index: Int, delta: Int) -> Unit,
    onMaxChange: (Int) -> Unit,
    onShowIntentionChange: (Boolean) -> Unit,
    onEditIntention: () -> Unit,
    onShowAppsButtonChange: (Boolean) -> Unit,
) {
    val home = state.home
    val locale = LocalConfiguration.current.locales.get(0) ?: Locale.getDefault()
    ShunyaScreen(title = stringResource(R.string.home_settings_title), onBack = onBack) {
        SectionHeader(
            text = stringResource(R.string.home_settings_favorites) + " · " +
                stringResource(R.string.home_settings_count, state.favorites.size, home.maxFavorites),
        )
        if (state.favorites.isEmpty()) {
            Text(
                text = stringResource(R.string.home_settings_empty),
                style = ShunyaTheme.typography.bodySmall,
                color = ShunyaTheme.colors.tertiary,
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
            )
        }
        state.favorites.forEachIndexed { index, app ->
            FavoriteEditorRow(
                app = app,
                canMoveUp = index > 0,
                canMoveDown = index < state.favorites.lastIndex,
                onRename = { onRename(app) },
                onRemove = { onRemove(app) },
                onMoveUp = { onMove(index, -1) },
                onMoveDown = { onMove(index, 1) },
            )
        }
        SettingsRow(
            title = stringResource(R.string.home_settings_add),
            summary = if (state.isFull) stringResource(R.string.home_settings_full) else null,
            enabled = !state.isFull,
            onClick = onAdd,
        )

        SectionHeader(text = stringResource(R.string.home_settings_layout))
        ChoiceRow(
            title = stringResource(R.string.home_settings_max),
            options = (0..ProductLimits.FAVORITES_MAX).toList(),
            selected = home.maxFavorites,
            optionLabel = { count -> String.format(locale, "%d", count) },
            onSelect = onMaxChange,
        )
        SwitchRow(
            title = stringResource(R.string.home_settings_intention),
            summary = stringResource(R.string.home_settings_intention_summary),
            checked = home.showIntention,
            onCheckedChange = onShowIntentionChange,
        )
        if (home.showIntention) {
            SettingsRow(
                title = stringResource(R.string.home_settings_intention_text),
                summary = home.intention.ifBlank { stringResource(R.string.home_settings_intention_empty) },
                onClick = onEditIntention,
            )
        }
        SwitchRow(
            title = stringResource(R.string.home_settings_apps_button),
            summary = stringResource(R.string.home_settings_apps_button_summary),
            checked = home.showAppsButton,
            onCheckedChange = onShowAppsButtonChange,
        )
    }
}

/** One favorite: tap the name to rename; Up / Down reorder; Remove takes it off home. */
@Composable
private fun FavoriteEditorRow(
    app: LauncherApp,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRename: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val colors = ShunyaTheme.colors
    val moveUpLabel = stringResource(R.string.home_settings_move_up_label, app.displayLabel)
    val moveDownLabel = stringResource(R.string.home_settings_move_down_label, app.displayLabel)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = Spacing.screenHorizontal, end = Spacing.s),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClickLabel = stringResource(R.string.apps_action_rename), onClick = onRename)
                .padding(vertical = Spacing.s),
        ) {
            Text(
                text = app.displayLabel,
                style = ShunyaTheme.typography.body,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (app.customLabel != null) {
                Text(
                    text = stringResource(R.string.apps_original_name, app.label),
                    style = ShunyaTheme.typography.bodySmall,
                    color = colors.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        ShunyaTextButton(
            text = stringResource(R.string.home_settings_move_up),
            onClick = onMoveUp,
            enabled = canMoveUp,
            style = ShunyaButtonStyle.Secondary,
            modifier = Modifier.semantics { contentDescription = moveUpLabel },
        )
        ShunyaTextButton(
            text = stringResource(R.string.home_settings_move_down),
            onClick = onMoveDown,
            enabled = canMoveDown,
            style = ShunyaButtonStyle.Secondary,
            modifier = Modifier.semantics { contentDescription = moveDownLabel },
        )
        ShunyaTextButton(
            text = stringResource(R.string.common_remove),
            onClick = onRemove,
            style = ShunyaButtonStyle.Secondary,
        )
    }
}
