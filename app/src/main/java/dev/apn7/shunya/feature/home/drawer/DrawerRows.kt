package dev.apn7.shunya.feature.home.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LauncherApp

private val RowMinHeight = 52.dp

/** One app in the drawer or in search results: its name and, for work-profile apps, a small tag. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AppRow(
    app: LauncherApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .combinedClickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.common_open),
                onLongClickLabel = stringResource(R.string.common_more),
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
    ) {
        Text(
            text = app.displayLabel,
            style = ShunyaTheme.typography.listItem,
            color = ShunyaTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (app.isWork) {
            Text(
                text = stringResource(R.string.drawer_work_tag),
                style = ShunyaTheme.typography.caption,
                color = ShunyaTheme.colors.tertiary,
                modifier = Modifier.padding(start = Spacing.s),
            )
        }
    }
}

/** Section letter above a group of apps (optional, "Section letters" setting). */
@Composable
internal fun SectionLetter(letter: String) {
    Text(
        text = letter,
        style = ShunyaTheme.typography.section,
        color = ShunyaTheme.colors.tertiary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.screenHorizontal, top = Spacing.m, bottom = Spacing.xs)
            .semantics { heading() },
    )
}

/** A text action row in search results ("Search the web for …", the calculator). */
@Composable
internal fun SearchActionRow(text: String, onClick: () -> Unit, summary: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
    ) {
        Text(
            text = text,
            style = ShunyaTheme.typography.body,
            color = ShunyaTheme.colors.secondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (summary != null) {
            Text(text = summary, style = ShunyaTheme.typography.caption, color = ShunyaTheme.colors.tertiary)
        }
    }
}

/** The calculator row: "= 84" in list size, "Tap to copy" below. */
@Composable
internal fun CalculatorRow(result: String, onCopy: () -> Unit) {
    val label = stringResource(R.string.search_calculator_label)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .clickable(onClickLabel = stringResource(R.string.common_copy), role = Role.Button, onClick = onCopy)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
    ) {
        Text(
            text = stringResource(R.string.search_calculator_result, result),
            style = ShunyaTheme.typography.listItem,
            color = ShunyaTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label + " · " + stringResource(R.string.search_calculator_copy),
            style = ShunyaTheme.typography.caption,
            color = ShunyaTheme.colors.tertiary,
        )
    }
}
