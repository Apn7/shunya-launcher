package dev.apn7.shunya.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/**
 * Standard full-screen page (every settings, focus and list screen): solid background, insets
 * handled (status bar, navigation bar, cutout, keyboard), a "Back" text button, [actions] (e.g. a
 * [ShunyaTextButton]) on the right, a large light [title] and the [content] below.
 *
 * With [scrollable] = true (default) [content] scrolls as one column. For long lists pass
 * `scrollable = false` and put a `LazyColumn(Modifier.weight(1f))` in [content].
 * Rows provide their own horizontal padding; free-form content should use
 * `Modifier.padding(horizontal = Spacing.screenHorizontal)`.
 */
@Composable
fun ShunyaScreen(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = ShunyaTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Spacing.minTouchTarget)
                .padding(horizontal = Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                ShunyaTextButton(
                    text = stringResource(R.string.common_back),
                    onClick = onBack,
                    style = ShunyaButtonStyle.Secondary,
                )
            }
            Spacer(Modifier.weight(1f))
            actions()
        }
        Text(
            text = title,
            style = ShunyaTheme.typography.title,
            color = colors.ink,
            modifier = Modifier
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(top = Spacing.s, bottom = Spacing.m)
                .semantics { heading() },
        )
        val body = Modifier
            .fillMaxWidth()
            .weight(1f)
        Column(
            modifier = if (scrollable) body.verticalScroll(rememberScrollState()) else body,
            content = content,
        )
    }
}
