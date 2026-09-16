package dev.apn7.shunya.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/**
 * Centered dialog in Shunya colours: optional [title], scrollable [content], and text buttons.
 * The dismiss button (shown when [dismissText] is set) calls [onDismiss]; the confirm button is
 * shown when both [confirmText] and [onConfirm] are set. Use [confirmStyle] Danger for
 * destructive confirmations.
 */
@Composable
fun ShunyaDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    confirmStyle: ShunyaButtonStyle = ShunyaButtonStyle.Primary,
    dismissText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        val colors = ShunyaTheme.colors
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .padding(top = Spacing.l, bottom = Spacing.s),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = ShunyaTheme.typography.listItem,
                    color = colors.ink,
                    modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.m),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.l),
                content = content,
            )
            val showConfirm = confirmText != null && onConfirm != null
            if (showConfirm || dismissText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.s, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (dismissText != null) {
                        ShunyaTextButton(text = dismissText, onClick = onDismiss, style = ShunyaButtonStyle.Secondary)
                    }
                    if (confirmText != null && onConfirm != null) {
                        ShunyaTextButton(text = confirmText, onClick = onConfirm, style = confirmStyle)
                    }
                }
            }
        }
    }
}
