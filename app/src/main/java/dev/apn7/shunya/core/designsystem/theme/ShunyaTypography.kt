package dev.apn7.shunya.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.apn7.shunya.core.model.TextSizeChoice

/**
 * Shunya's type scale. Type is the interface, so every text in the app uses one of these.
 * Sizes are in `sp` (they follow the system font scale) and are multiplied by the user's
 * [TextSizeChoice]. Read them with `ShunyaTheme.typography`.
 */
@Immutable
data class ShunyaTypography(
    /** Home clock, large style. */
    val clockLarge: TextStyle,
    /** Home clock, medium style. */
    val clockMedium: TextStyle,
    /** Home favorites (the app names on the home screen). */
    val homeApp: TextStyle,
    /** Screen titles ("Settings", "Screen time"), big numbers on stat screens. */
    val title: TextStyle,
    /** App rows in the drawer and app lists. */
    val listItem: TextStyle,
    /** Default text: settings rows, paragraphs, text fields. */
    val body: TextStyle,
    /** Summaries under rows, secondary lines. */
    val bodySmall: TextStyle,
    /** Text buttons and emphasised labels. */
    val label: TextStyle,
    /** Section headers above groups of rows. */
    val section: TextStyle,
    /** Tags ("work"), chart axis labels, footnotes. */
    val caption: TextStyle,
)

/** Multiplier applied to every size for [TextSizeChoice]. */
fun TextSizeChoice.scale(): Float = when (this) {
    TextSizeChoice.Small -> 0.88f
    TextSizeChoice.Medium -> 1.0f
    TextSizeChoice.Large -> 1.14f
    TextSizeChoice.ExtraLarge -> 1.3f
}

/** Builds the scale for [family] at [size]. */
fun shunyaTypography(family: FontFamily, size: TextSizeChoice): ShunyaTypography {
    val scale = size.scale()
    fun style(sizeSp: Float, weight: FontWeight, lineHeightFactor: Float = 1.3f, letterSpacingSp: Float = 0f) = TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = (sizeSp * scale).sp,
        lineHeight = (sizeSp * scale * lineHeightFactor).sp,
        letterSpacing = letterSpacingSp.sp,
    )
    return ShunyaTypography(
        clockLarge = style(76f, FontWeight.Light, lineHeightFactor = 1.1f, letterSpacingSp = -1.5f),
        clockMedium = style(54f, FontWeight.Light, lineHeightFactor = 1.1f, letterSpacingSp = -1f),
        homeApp = style(26f, FontWeight.Normal, lineHeightFactor = 1.25f),
        title = style(30f, FontWeight.Light, lineHeightFactor = 1.2f),
        listItem = style(20f, FontWeight.Normal),
        body = style(16f, FontWeight.Normal, lineHeightFactor = 1.4f),
        bodySmall = style(14f, FontWeight.Normal, lineHeightFactor = 1.4f),
        label = style(15f, FontWeight.Medium),
        section = style(13f, FontWeight.Medium, letterSpacingSp = 0.8f),
        caption = style(12f, FontWeight.Normal),
    )
}
