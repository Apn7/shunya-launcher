package dev.apn7.shunya.feature.home.drawer.logic

import java.text.Normalizer
import java.util.Locale

/** A row of the drawer list: a section letter header, or the app at [App.appIndex] of the sorted list. */
sealed interface DrawerRow {
    data class Header(val letter: String) : DrawerRow
    data class App(val appIndex: Int) : DrawerRow
}

/** One fast-scroller entry: a [letter] and the list [row] where its section starts. */
data class Section(val letter: String, val row: Int)

/** The drawer list rows plus the fast-scroller sections pointing into them. */
data class DrawerLayout(val rows: List<DrawerRow>, val sections: List<Section>) {
    companion object {
        val Empty: DrawerLayout = DrawerLayout(emptyList(), emptyList())
    }
}

/**
 * Builds the alphabet index of an alphabetically sorted app list (PRD 3.2): only letters that
 * exist, "#" for names that start with a digit, symbol or punctuation. Accented letters join
 * their base letter (É → E); other scripts keep their own letters (Bangla অ, ক …).
 */
object SectionIndex {

    const val OTHER: String = "#"

    private val accents = Regex("[\\u0300-\\u036F]")

    /** The section letter of [label]. */
    fun letterFor(label: String): String {
        val first = label.trimStart().firstOrNull() ?: return OTHER
        if (!first.isLetter()) return OTHER
        val base = accents.replace(Normalizer.normalize(first.toString(), Normalizer.Form.NFD), "")
        return base.ifEmpty { first.toString() }.uppercase(Locale.ROOT)
    }

    /**
     * Rows for [labels] (already sorted), with a header before each section when [withHeaders].
     * A letter that shows up again later (collators may interleave rare characters) gets a
     * second header, but the fast scroller always jumps to its first section.
     */
    fun layout(labels: List<String>, withHeaders: Boolean): DrawerLayout {
        val rows = ArrayList<DrawerRow>(labels.size + if (withHeaders) 32 else 0)
        val sections = ArrayList<Section>()
        val seen = HashSet<String>()
        var previous: String? = null
        for (index in labels.indices) {
            val letter = letterFor(labels[index])
            if (letter != previous) {
                if (seen.add(letter)) sections.add(Section(letter, rows.size))
                if (withHeaders) rows.add(DrawerRow.Header(letter))
                previous = letter
            }
            rows.add(DrawerRow.App(index))
        }
        return DrawerLayout(rows, sections)
    }

    /**
     * Which of [count] equally tall letters is under [position] along a strip of [extent]
     * (both in pixels). Clamped to the first/last letter; -1 when there is nothing to pick.
     */
    fun letterIndexAt(position: Float, extent: Float, count: Int): Int {
        if (count <= 0 || extent <= 0f) return -1
        val index = (position / extent * count).toInt()
        return index.coerceIn(0, count - 1)
    }
}
