package dev.apn7.shunya.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/** Emphasis of a [ShunyaTextButton]: ink, 60 % ink, or the danger colour. */
enum class ShunyaButtonStyle { Primary, Secondary, Danger }

/**
 * Shunya's only button: text, no fill. [outlined] adds a hairline pill border for the one main
 * call to action on a screen (onboarding "Continue", gate "Open"). Always at least 48 dp tall.
 */
@Composable
fun ShunyaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ShunyaButtonStyle = ShunyaButtonStyle.Primary,
    enabled: Boolean = true,
    outlined: Boolean = false,
) {
    val colors = ShunyaTheme.colors
    val color = when {
        !enabled -> colors.tertiary
        style == ShunyaButtonStyle.Primary -> colors.ink
        style == ShunyaButtonStyle.Secondary -> colors.secondary
        else -> colors.danger
    }
    val shape = RoundedCornerShape(percent = 50)
    val frame = if (outlined) Modifier.border(1.dp, color, shape) else Modifier
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = Spacing.minTouchTarget, minHeight = Spacing.minTouchTarget)
            .then(frame)
            .clip(shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (outlined) Spacing.l else Spacing.m, vertical = Spacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = ShunyaTheme.typography.label, color = color)
    }
}
