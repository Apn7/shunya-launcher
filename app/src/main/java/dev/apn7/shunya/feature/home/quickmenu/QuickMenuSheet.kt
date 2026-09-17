package dev.apn7.shunya.feature.home.quickmenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SheetAction
import dev.apn7.shunya.core.designsystem.component.ShunyaBottomSheet

/**
 * Long-press on empty home space (PRD 3.1): settings, focus, wallpaper and grayscale toggles,
 * edit home. Navigation choices close the sheet at once (the screen changes anyway); toggles
 * animate it away first.
 */
@Composable
internal fun QuickMenuSheet(
    wallpaperMode: Boolean,
    grayscaleOn: Boolean,
    grayscaleAvailable: Boolean,
    onDismiss: () -> Unit,
    onSettings: () -> Unit,
    onFocusNow: () -> Unit,
    onToggleWallpaper: () -> Unit,
    onGrayscale: () -> Unit,
    onEditHome: () -> Unit,
) {
    ShunyaBottomSheet(onDismiss = onDismiss) { close ->
        SheetAction(text = stringResource(R.string.common_settings), onClick = onSettings)
        SheetAction(text = stringResource(R.string.home_menu_focus_now), onClick = onFocusNow)
        SheetAction(
            text = stringResource(if (wallpaperMode) R.string.home_menu_wallpaper_hide else R.string.home_menu_wallpaper_show),
            onClick = {
                close()
                onToggleWallpaper()
            },
        )
        SheetAction(
            text = stringResource(if (grayscaleOn) R.string.home_menu_grayscale_off else R.string.home_menu_grayscale_on),
            summary = if (grayscaleAvailable) null else stringResource(R.string.home_menu_grayscale_setup),
            onClick = {
                if (grayscaleAvailable) close()
                onGrayscale()
            },
        )
        SheetAction(text = stringResource(R.string.home_menu_edit_home), onClick = onEditHome)
    }
}
