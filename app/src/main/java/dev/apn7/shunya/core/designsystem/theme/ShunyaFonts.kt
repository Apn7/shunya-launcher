package dev.apn7.shunya.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.apn7.shunya.R
import dev.apn7.shunya.core.model.FontChoice

/**
 * Font families for every [FontChoice], built from the bundled OFL fonts in `res/font`.
 * Shunya uses three weights: Light (clock, titles), Normal (text) and Medium (emphasis).
 *
 * Bangla: Inter, Space Grotesk, IBM Plex Mono and Lora have no Bengali glyphs. When the UI
 * language is Bangla, [forChoice] therefore returns Hind Siliguri (which also covers Latin)
 * for those choices, so both scripts share one design, weight and line height. "System" keeps
 * the phone's default font, which already includes Bengali. In an English UI the chosen font is
 * used as is; any Bangla app label then falls back to the system's Bengali font automatically.
 */
object ShunyaFonts {

    val Inter: FontFamily = FontFamily(
        Font(R.font.inter_light, FontWeight.Light),
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
    )

    val SpaceGrotesk: FontFamily = FontFamily(
        Font(R.font.space_grotesk_light, FontWeight.Light),
        Font(R.font.space_grotesk_regular, FontWeight.Normal),
        Font(R.font.space_grotesk_medium, FontWeight.Medium),
    )

    val IbmPlexMono: FontFamily = FontFamily(
        Font(R.font.ibm_plex_mono_light, FontWeight.Light),
        Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
        Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
    )

    /** Lora ships no light weight; Regular stands in for it. */
    val Lora: FontFamily = FontFamily(
        Font(R.font.lora_regular, FontWeight.Light),
        Font(R.font.lora_regular, FontWeight.Normal),
        Font(R.font.lora_medium, FontWeight.Medium),
    )

    val HindSiliguri: FontFamily = FontFamily(
        Font(R.font.hind_siliguri_light, FontWeight.Light),
        Font(R.font.hind_siliguri_regular, FontWeight.Normal),
        Font(R.font.hind_siliguri_medium, FontWeight.Medium),
    )

    /** The family to render with for [choice]; see the class comment for the Bangla rule. */
    fun forChoice(choice: FontChoice, banglaUi: Boolean): FontFamily = when (choice) {
        FontChoice.System -> FontFamily.Default
        FontChoice.HindSiliguri -> HindSiliguri
        else -> if (banglaUi) HindSiliguri else latinFamily(choice)
    }

    private fun latinFamily(choice: FontChoice): FontFamily = when (choice) {
        FontChoice.Inter -> Inter
        FontChoice.SpaceGrotesk -> SpaceGrotesk
        FontChoice.IbmPlexMono -> IbmPlexMono
        FontChoice.Lora -> Lora
        FontChoice.HindSiliguri -> HindSiliguri
        FontChoice.System -> FontFamily.Default
    }
}
