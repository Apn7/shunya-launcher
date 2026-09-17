package dev.apn7.shunya.feature.home.search.logic

import java.text.Normalizer
import java.util.Locale

/**
 * Text folding for search: case- and accent-insensitive ("Café" matches "cafe").
 *
 * Only the Latin combining accents (U+0300–U+036F) are removed. Bangla vowel signs are combining
 * marks too, but they carry meaning, so they are kept and treated as part of their word.
 */
object SearchText {

    private val accents = Regex("[\\u0300-\\u036F]")

    /** Letters that do not decompose into a base letter plus an accent. */
    private val specialLetters: Map<Char, String> = mapOf(
        'ß' to "ss", 'æ' to "ae", 'Æ' to "AE", 'œ' to "oe", 'Œ' to "OE", 'ø' to "o", 'Ø' to "O",
        'ł' to "l", 'Ł' to "L", 'đ' to "d", 'Đ' to "D", 'ı' to "i",
    )

    /** [text] without accents; the case is kept because word splitting needs it. */
    fun stripAccents(text: String): String {
        val stripped = accents.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "")
        if (stripped.none { it in specialLetters }) return stripped
        val builder = StringBuilder(stripped.length + 4)
        for (c in stripped) builder.append(specialLetters[c] ?: c.toString())
        return builder.toString()
    }

    /** Lower-case, accent-free form of [text]. */
    fun fold(text: String): String = lower(stripAccents(text))

    /**
     * Folded words of an app label. Words end at separators (space, punctuation, symbols), at
     * camel-case humps ("YouTube" → you, tube), at the end of an acronym ("BBCNews" → bbc, news)
     * and where letters and digits meet ("Web3" → web, 3).
     */
    fun words(text: String): List<String> {
        val source = stripAccents(text)
        val words = ArrayList<String>()
        val current = StringBuilder()
        for (i in source.indices) {
            val c = source[i]
            if (!isWordChar(c)) {
                flushInto(words, current)
                continue
            }
            if (current.isNotEmpty() && startsNewWord(source, i)) flushInto(words, current)
            current.append(c)
        }
        flushInto(words, current)
        return words
    }

    /** Folded tokens of what the user typed: split at separators only (queries are lower case). */
    fun queryTokens(query: String): List<String> {
        val folded = fold(query)
        val tokens = ArrayList<String>()
        val current = StringBuilder()
        for (c in folded) {
            if (isWordChar(c)) current.append(c) else flushInto(tokens, current)
        }
        flushInto(tokens, current)
        return tokens
    }

    private fun flushInto(target: MutableList<String>, current: StringBuilder) {
        if (current.isEmpty()) return
        target.add(lower(current.toString()))
        current.setLength(0)
    }

    /** Lower case, then drop accents again: `İ` lower-cases to `i` + a combining dot. */
    private fun lower(text: String): String = accents.replace(text.lowercase(Locale.ROOT), "")

    /** [i] > 0 and the character before it is a word character (callers guarantee both). */
    private fun startsNewWord(text: String, i: Int): Boolean {
        val c = text[i]
        val previous = text[i - 1]
        if (isJoiner(c)) return false
        return when {
            c.isUpperCase() && previous.isLowerCase() -> true
            c.isUpperCase() && previous.isUpperCase() && i + 1 < text.length && text[i + 1].isLowerCase() -> true
            c.isDigit() && previous.isLetter() -> true
            c.isLetter() && previous.isDigit() -> true
            else -> false
        }
    }

    private fun isWordChar(c: Char): Boolean = c.isLetterOrDigit() || isJoiner(c)

    /** Combining marks (Bangla vowel signs, virama) and zero-width joiners continue a word. */
    private fun isJoiner(c: Char): Boolean = when (c.category) {
        CharCategory.NON_SPACING_MARK,
        CharCategory.COMBINING_SPACING_MARK,
        CharCategory.ENCLOSING_MARK,
        CharCategory.FORMAT,
        -> true
        else -> false
    }
}

/**
 * An app label prepared for matching. Build it once per label (folding is the costly part) and
 * match it against every query with [FuzzyMatcher].
 */
class SearchKey(label: String) {

    /** Folded words, see [SearchText.words]. */
    val words: List<String> = SearchText.words(label)

    /** All words joined without separators: "google maps" → "googlemaps". */
    val compact: String = words.joinToString("")

    /** First character of every word: "Google Maps" → "gm", "YouTube" → "yt". */
    val initials: String = words.joinToString("") { it.take(1) }

    /** Where each word starts inside [compact]. */
    val wordStarts: List<Int> = run {
        val starts = ArrayList<Int>(words.size)
        var offset = 0
        for (word in words) {
            starts.add(offset)
            offset += word.length
        }
        starts
    }
}
