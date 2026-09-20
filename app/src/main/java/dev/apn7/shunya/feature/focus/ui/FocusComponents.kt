package dev.apn7.shunya.feature.focus.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.ShunyaTextField
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/** A paragraph of free text with the screen's side padding (disclosures, explanations, notes). */
@Composable
internal fun Paragraph(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = ShunyaTheme.typography.body,
    color: Color = ShunyaTheme.colors.secondary,
) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
    )
}

/** Search box with a clear button, shared by the app lists of this feature. */
@Composable
internal fun AppSearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    ShunyaTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.focus_search_apps),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal),
        trailing = if (query.isEmpty()) {
            null
        } else {
            { ShunyaTextButton(text = stringResource(R.string.common_clear), onClick = { onQueryChange("") }) }
        },
    )
}
