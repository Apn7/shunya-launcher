package dev.apn7.shunya.feature.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/** A short explanatory paragraph between rows (intros, hints, honest disclosures). */
@Composable
internal fun SettingsNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = ShunyaTheme.typography.bodySmall,
        color = ShunyaTheme.colors.secondary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
    )
}
