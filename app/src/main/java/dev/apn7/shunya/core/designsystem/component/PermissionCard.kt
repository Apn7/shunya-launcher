package dev.apn7.shunya.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/**
 * Explains one special access: what it is ([title]), why Shunya asks ([description]), whether it
 * is [granted], and a button to grant it. Used on the Permissions screen, in onboarding and in
 * place of any feature that needs a missing permission ("friendly permission card").
 */
@Composable
fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String = stringResource(R.string.core_permission_grant),
) {
    val colors = ShunyaTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s)
            .border(1.dp, colors.divider, RoundedCornerShape(16.dp))
            .padding(start = Spacing.m, end = Spacing.s, top = Spacing.m, bottom = Spacing.s),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = Spacing.s)) {
            Text(
                text = title,
                style = ShunyaTheme.typography.label,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(if (granted) R.string.core_permission_granted else R.string.core_permission_not_granted),
                style = ShunyaTheme.typography.caption,
                color = if (granted) colors.secondary else colors.tertiary,
            )
        }
        Text(
            text = description,
            style = ShunyaTheme.typography.bodySmall,
            color = colors.secondary,
            modifier = Modifier.padding(top = Spacing.xs, end = Spacing.s, bottom = if (granted) Spacing.s else 0.dp),
        )
        if (!granted) {
            ShunyaTextButton(
                text = actionText,
                onClick = onGrant,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}
