package dev.apn7.shunya.feature.settings.logic

import java.text.Normalizer
import java.util.Locale

/**
 * Simple, forgiving filter for the app pickers in Settings, notifications and onboarding (the
 * drawer has its own ranked fuzzy search). Case- and accent-insensitive; matches a substring of
 * the label or the start of its word initials ("gm" finds "Google Maps").
 */
object AppSearch {

    /** Lowercase without Latin accents ("Café" → "cafe"). Bangla text is kept as is. */
    fun normalize(text: String): String {
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
        val stripped = decomposed.filterNot { it in '̀'..'ͯ' }
        return Normalizer.normalize(stripped, Normalizer.Form.NFC).lowercase(Locale.ROOT)
    }

    /** True when [label] matches [query]; a blank query matches everything. */
    fun matches(label: String, query: String): Boolean {
        val wanted = normalize(query.trim())
        if (wanted.isEmpty()) return true
        val normalized = normalize(label)
        return normalized.contains(wanted) || initials(normalized).startsWith(wanted.replace(" ", ""))
    }

    /** The items of [items] whose [label] matches [query], in their original order. */
    fun <T> filter(items: List<T>, query: String, label: (T) -> String): List<T> =
        if (query.isBlank()) items else items.filter { matches(label(it), query) }

    private fun initials(text: String): String =
        text.split(' ', '-', '_', '.')
            .filter { it.isNotEmpty() }
            .map { it.first() }
            .joinToString("")
}
