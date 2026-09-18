package dev.apn7.shunya.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.home.components.ClockBlock
import dev.apn7.shunya.feature.home.components.ClockFormatter
import dev.apn7.shunya.feature.home.components.FavoritesList
import dev.apn7.shunya.feature.home.components.InfoLines
import dev.apn7.shunya.feature.home.components.IntentionLine
import dev.apn7.shunya.feature.home.components.horizontal
import dev.apn7.shunya.feature.home.components.textAlign
import dev.apn7.shunya.feature.home.logic.HomeStatus
import dev.apn7.shunya.feature.home.system.BatteryState

/**
 * The home screen content (stateless): clock, date, optional lines, intention and favorites.
 * No background: `MainActivity` paints the theme, or the wallpaper shows through in wallpaper mode.
 * Gestures are attached by the caller through [modifier].
 */
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    now: Long,
    formatter: ClockFormatter,
    battery: BatteryState?,
    nextAlarmMillis: Long?,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
    onIntentionClick: () -> Unit,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
    onAppsButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appearance = state.settings.appearance
    val home = state.settings.home
    val alignment = appearance.alignment
    val status = if (appearance.showStatusLine) {
        HomeStatus.pick(
            focusActive = state.focus.isActive,
            focusEndsAt = state.focus.endsAt,
            nowMillis = now,
            heldCount = state.heldCount,
            screenTimeMillis = state.screenTimeMillis,
        )
    } else {
        null
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = Spacing.screenHorizontal),
        horizontalAlignment = alignment.horizontal(),
    ) {
        // A small fixed gap plus a flexible one: on short screens the flexible part collapses first.
        Spacer(Modifier.height(Spacing.l))
        Spacer(Modifier.weight(0.3f))
        ClockBlock(
            now = now,
            formatter = formatter,
            appearance = appearance,
            onClockClick = onClockClick,
            onDateClick = onDateClick,
        )
        InfoLines(
            battery = battery,
            nextAlarm = nextAlarmMillis?.let { formatter.alarm(it, now) },
            status = status,
            alignment = alignment,
        )
        if (home.showIntention) {
            IntentionLine(intention = home.intention, alignment = alignment, onClick = onIntentionClick)
        }
        Spacer(Modifier.weight(1f))
        // Onboarding or a restored backup may store more than the current maximum: never show more.
        val favorites = state.favorites.take(home.maxFavorites)
        if (favorites.isEmpty() && home.maxFavorites > 0) {
            Text(
                text = stringResource(R.string.home_favorites_empty),
                style = ShunyaTheme.typography.bodySmall,
                color = ShunyaTheme.colors.tertiary,
                textAlign = alignment.textAlign(),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            FavoritesList(
                favorites = favorites,
                alignment = alignment,
                onClick = onAppClick,
                onLongClick = onAppLongClick,
            )
        }
        Spacer(Modifier.weight(0.6f))
        if (home.showAppsButton) {
            ShunyaTextButton(
                text = stringResource(R.string.home_apps_button),
                onClick = onAppsButtonClick,
                style = ShunyaButtonStyle.Secondary,
            )
        } else {
            Spacer(Modifier.height(Spacing.l))
        }
    }
}
