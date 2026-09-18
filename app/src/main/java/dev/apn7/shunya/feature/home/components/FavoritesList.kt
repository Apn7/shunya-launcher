package dev.apn7.shunya.feature.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.HomeAlignment
import dev.apn7.shunya.core.model.LauncherApp

/**
 * Home favorites: app names only, in the user's order. Tap opens (through the focus policy),
 * long-press opens the app action sheet. Only the text itself is touchable, so the empty space
 * around it stays free for home gestures.
 */
@Composable
internal fun FavoritesList(
    favorites: List<LauncherApp>,
    alignment: HomeAlignment,
    onClick: (LauncherApp) -> Unit,
    onLongClick: (LauncherApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = alignment.horizontal()) {
        favorites.forEach { app ->
            key(app.key.id) {
                FavoriteItem(app = app, onClick = { onClick(app) }, onLongClick = { onLongClick(app) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteItem(app: LauncherApp, onClick: () -> Unit, onLongClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .heightIn(min = Spacing.minTouchTarget)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = stringResource(R.string.common_open),
                onLongClickLabel = stringResource(R.string.common_more),
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .padding(vertical = Spacing.xs),
    ) {
        Text(
            text = app.displayLabel,
            style = ShunyaTheme.typography.homeApp,
            color = ShunyaTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
