package dev.apn7.shunya.feature.settings.language.logic

/** Pure matching of BCP-47 language tags to the UI languages Shunya ships. */
object LanguageTags {

    /**
     * The entry of [supported] (e.g. "en", "bn") whose language matches the primary subtag of
     * [tag] ("bn-BD" → "bn", "EN_us" → "en"); null for an empty or unsupported tag.
     */
    fun match(tag: String?, supported: List<String>): String? {
        val primary = tag.orEmpty().trim().substringBefore('-').substringBefore('_').lowercase()
        if (primary.isEmpty()) return null
        return supported.firstOrNull { it.lowercase() == primary }
    }
}
