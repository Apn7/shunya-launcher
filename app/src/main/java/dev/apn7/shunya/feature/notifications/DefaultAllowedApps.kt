package dev.apn7.shunya.feature.notifications

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Telephony
import android.telecom.TelecomManager

/**
 * The built-in allowed list of the notification filter (PRD 3.4), used while the user never
 * customised it (`NotificationPrefs.allowedPackages == null`): the phone's default dialer and SMS
 * app, the apps handling the clock and the calendar, the common stock versions of each, and Shunya.
 * A few quick system calls: fine on any thread, but callers cache the result.
 */
object DefaultAllowedApps {

    private val WELL_KNOWN: Set<String> = setOf(
        // Phone and in-call screens
        "com.android.dialer", "com.google.android.dialer", "com.samsung.android.dialer",
        "com.android.phone", "com.android.server.telecom", "com.android.incallui", "com.samsung.android.incallui",
        // Messages
        "com.android.mms", "com.android.messaging", "com.google.android.apps.messaging", "com.samsung.android.messaging",
        // Clock
        "com.android.deskclock", "com.google.android.deskclock", "com.sec.android.app.clockpackage",
        // Calendar
        "com.android.calendar", "com.google.android.calendar", "com.samsung.android.calendar",
    )

    /** Every package on the built-in allowed list for this phone. */
    fun resolve(context: Context): Set<String> {
        val found = LinkedHashSet<String>()
        found += context.packageName
        found += WELL_KNOWN
        defaultDialer(context)?.let { found += it }
        defaultSms(context)?.let { found += it }
        handlerOf(context, Intent(AlarmClock.ACTION_SHOW_ALARMS))?.let { found += it }
        handlerOf(context, Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()))
            ?.let { found += it }
        return found
    }

    private fun defaultDialer(context: Context): String? =
        try {
            context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage
        } catch (e: SecurityException) {
            null
        }

    private fun defaultSms(context: Context): String? =
        try {
            Telephony.Sms.getDefaultSmsPackage(context)
        } catch (e: SecurityException) {
            null
        }

    @Suppress("DEPRECATION") // resolveActivity(Intent, Int): the flags overload needs API 33.
    private fun handlerOf(context: Context, intent: Intent): String? =
        context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
}
