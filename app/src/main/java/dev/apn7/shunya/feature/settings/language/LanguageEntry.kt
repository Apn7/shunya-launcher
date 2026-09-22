package dev.apn7.shunya.feature.settings.language

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.model.AppLanguage
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.system.startSafely

/**
 * Entry point of [dev.apn7.shunya.core.navigation.Route.Language]. On Android 13+ the system is
 * the source of truth (the user may also change it in system settings); `settings.language` is
 * kept in sync with it. A change recreates the activity; the back stack survives in `MainViewModel`.
 */
@Composable
fun LanguageEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val systemLanguage = remember(context) { AppLocales.current(context) }

    LaunchedEffect(systemLanguage) {
        if (systemLanguage != null) {
            container.settingsRepository.edit { current ->
                if (current.language == systemLanguage) current else current.copy(language = systemLanguage)
            }
        }
    }

    LanguageScreen(
        selected = systemLanguage ?: AppLanguage.System,
        supported = AppLocales.isSupported,
        onBack = navigator::back,
        onSelect = { language ->
            container.settingsRepository.edit { it.copy(language = language) }
            AppLocales.apply(context, language)
        },
        onOpenSystemSettings = { context.startSafely(Intent(Settings.ACTION_LOCALE_SETTINGS)) },
    )
}
