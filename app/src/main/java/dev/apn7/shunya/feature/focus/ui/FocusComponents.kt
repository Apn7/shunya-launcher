package dev.apn7.shunya.feature.focus.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
