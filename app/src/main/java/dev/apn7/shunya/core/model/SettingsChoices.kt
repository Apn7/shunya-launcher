package dev.apn7.shunya.core.model

import kotlinx.serialization.Serializable

// Enumerated choices used by LauncherSettings. Constants are persisted by name:
// never rename or remove one (unknown names fall back to the property's default).

/** Colour theme. [System] follows the system dark mode: [Paper] when light, [Ink] when dark. */
@Serializable
enum class ThemeChoice { System, Paper, Ink, Slate }

/** Typeface for all UI text. Bundled fonts live in `res/font`; [System] is the phone's default. */
@Serializable
enum class FontChoice { Inter, SpaceGrotesk, IbmPlexMono, Lora, HindSiliguri, System }

/** Global text size step (S / M / L / XL), applied on top of the system font scale. */
@Serializable
enum class TextSizeChoice { Small, Medium, Large, ExtraLarge }

/** Horizontal alignment of the home screen (clock, lines, favorites). */
@Serializable
enum class HomeAlignment { Start, Center, End }

/** Size of the home clock. */
@Serializable
enum class ClockStyle { Large, Medium }

/** 12/24-hour clock. [System] follows the phone's setting. */
@Serializable
enum class ClockFormat { System, TwelveHour, TwentyFourHour }

/** App drawer order. [MostUsed] needs usage access and falls back to [Alphabetical] without it. */
@Serializable
enum class DrawerSort { Alphabetical, MostUsed }

/** Notification filter: [Hold] moves notifications from non-allowed apps into the Inbox. */
@Serializable
enum class NotificationFilterMode { Off, Hold }

/**
 * Where focus rules are enforced. [LauncherOnly]: only launches from Shunya are gated.
 * [SystemWide]: the accessibility service also gates blocked apps opened from anywhere.
 */
@Serializable
enum class BlockingMode { LauncherOnly, SystemWide }

/** UI language. [tag] is the BCP-47 tag passed to LocaleManager; empty for [System]. */
@Serializable
enum class AppLanguage(val tag: String) {
    System(""),
    English("en"),
    Bangla("bn"),
}
