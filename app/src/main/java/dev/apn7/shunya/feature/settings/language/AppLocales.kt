package dev.apn7.shunya.feature.settings.language

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import dev.apn7.shunya.core.model.AppLanguage
import dev.apn7.shunya.feature.settings.language.logic.LanguageTags

/**
 * Per-app language through `LocaleManager` (Android 13+, PRD 3.5; AppCompat is not used). On
 * older versions Shunya follows the phone's language. The system stores the choice itself and
 * recreates the activity when it changes; `LauncherSettings.language` only mirrors it.
 */
object AppLocales {

    private val SUPPORTED: List<String> = AppLanguage.entries.map { it.tag }.filter { it.isNotEmpty() }

    /** True when this Android version has per-app languages. */
    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** The language the system has for Shunya now, or null when per-app languages are unsupported. */
    fun current(context: Context): AppLanguage? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        val locales = context.getSystemService(LocaleManager::class.java)?.applicationLocales ?: return AppLanguage.System
        return fromLocales(locales)
    }

    /** Switches Shunya to [language]; false (nothing changed) when unsupported. */
    fun apply(context: Context, language: AppLanguage): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        val manager = context.getSystemService(LocaleManager::class.java) ?: return false
        manager.applicationLocales = if (language.tag.isEmpty()) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(language.tag)
        }
        return true
    }

    private fun fromLocales(locales: LocaleList): AppLanguage {
        if (locales.isEmpty()) return AppLanguage.System
        val tag = LanguageTags.match(locales.get(0)?.toLanguageTag(), SUPPORTED) ?: return AppLanguage.System
        return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.System
    }
}
