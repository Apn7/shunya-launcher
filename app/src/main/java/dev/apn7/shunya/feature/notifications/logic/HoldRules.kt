package dev.apn7.shunya.feature.notifications.logic

/**
 * What the notification filter needs to know about one posted notification. Built from a
 * `StatusBarNotification` by the service, so the decision below stays pure and testable.
 */
data class NotificationFacts(
    val packageName: String,
    /** `FLAG_ONGOING_EVENT`: music, navigation, calls in progress, downloads. */
    val isOngoing: Boolean = false,
    /** False when the user could not swipe it away either (the system would ignore our cancel). */
    val isClearable: Boolean = true,
    /** `FLAG_FOREGROUND_SERVICE`. */
    val isForegroundService: Boolean = false,
    /** `FLAG_GROUP_SUMMARY`: the bundle header of a group, not a message of its own. */
    val isGroupSummary: Boolean = false,
    /** Incoming calls and alarms (category call/alarm, or a full-screen intent). */
    val isTimeCritical: Boolean = false,
    /** Media controls (a media session or the transport category). */
    val isMedia: Boolean = false,
)

/** The user's filter settings, already resolved (built-in defaults applied). */
data class HoldSettings(
    val holdEnabled: Boolean,
    /** Packages whose notifications stay in the shade. */
    val allowedPackages: Set<String>,
    /** Shunya's own package: never held. */
    val ownPackage: String,
)

/** Outcome for one notification. Only [Hold] removes it from the shade (and saves it). */
enum class HoldDecision {
    /** Remove from the shade and keep it in the Inbox. */
    Hold,

    /** Filter is off. */
    FilterOff,

    /** Shunya itself or a core system package. */
    OwnOrSystem,

    /** The app is on the allowed list. */
    AllowedApp,

    /** A kind that is never held: ongoing, foreground service, group summary, call/alarm, media, not clearable. */
    ProtectedKind,
}

/** Pure hold/allow rules of the notification filter (PRD 3.4). */
object HoldRules {

    /** Core system packages whose notifications (battery, USB, updates) are never held. */
    val SYSTEM_PACKAGES: Set<String> = setOf("android", "com.android.systemui")

    /** Decides what happens to the notification described by [facts]. */
    fun decide(facts: NotificationFacts, settings: HoldSettings): HoldDecision = when {
        !settings.holdEnabled -> HoldDecision.FilterOff
        facts.packageName == settings.ownPackage || facts.packageName in SYSTEM_PACKAGES -> HoldDecision.OwnOrSystem
        isProtectedKind(facts) -> HoldDecision.ProtectedKind
        facts.packageName in settings.allowedPackages -> HoldDecision.AllowedApp
        else -> HoldDecision.Hold
    }

    /**
     * The allowed list in force: the user's own list when they customised it, otherwise the
     * built-in [defaults] (`NotificationPrefs.allowedPackages == null` means "never customised").
     */
    fun effectiveAllowed(customised: Set<String>?, defaults: Set<String>): Set<String> = customised ?: defaults

    /**
     * The allowed list after the user ticks or unticks [packageName]. The first change turns the
     * built-in [defaults] into an explicit list, so later default changes don't surprise them.
     */
    fun toggled(customised: Set<String>?, defaults: Set<String>, packageName: String, allowed: Boolean): Set<String> {
        val current = effectiveAllowed(customised, defaults)
        return if (allowed) current + packageName else current - packageName
    }

    private fun isProtectedKind(facts: NotificationFacts): Boolean =
        facts.isOngoing ||
            !facts.isClearable ||
            facts.isForegroundService ||
            facts.isGroupSummary ||
            facts.isTimeCritical ||
            facts.isMedia
}
