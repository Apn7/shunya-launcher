package dev.apn7.shunya.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Modal bottom sheet in Shunya colours (app actions, quick menu). Show it by composing it; remove
 * it from composition in [onDismiss]. [content] is laid out in a column and receives `close`,
 * which animates the sheet away and then calls [onDismiss]: call it before acting on a choice.
 *
 * ```
 * if (showMenu) ShunyaBottomSheet(onDismiss = { showMenu = false }) { close ->
 *     SheetAction(text = stringResource(R.string.common_settings), onClick = { close(); openSettings() })
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShunyaBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (close: () -> Unit) -> Unit,
) {
    val colors = ShunyaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = colors.surface,
        contentColor = colors.ink,
        tonalElevation = 0.dp,
        scrimColor = colors.scrim,
        dragHandle = { SheetHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = Spacing.m),
        ) {
            content(close)
        }
    }
}

/** One full-width action in a [ShunyaBottomSheet], with an optional [summary] line. */
@Composable
fun SheetAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    style: ShunyaButtonStyle = ShunyaButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val colors = ShunyaTheme.colors
    val color = when {
        !enabled -> colors.tertiary
        style == ShunyaButtonStyle.Danger -> colors.danger
        style == ShunyaButtonStyle.Secondary -> colors.secondary
        else -> colors.ink
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s + Spacing.xs),
    ) {
        Text(text = text, style = ShunyaTheme.typography.body, color = color)
        if (summary != null) {
            Text(text = summary, style = ShunyaTheme.typography.bodySmall, color = colors.secondary)
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.m),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 4.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(ShunyaTheme.colors.divider),
        )
    }
}
