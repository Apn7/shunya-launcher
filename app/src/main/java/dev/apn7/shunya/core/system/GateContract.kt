package dev.apn7.shunya.core.system

import android.content.Context
import android.content.Intent
import dev.apn7.shunya.core.model.AppKey

/**
 * How anyone opens the GateActivity (mindful pause / limit / blocked screen).
 *
 * The gate receives only the app and where the launch came from; it asks `LaunchPolicy` for the
 * current decision itself, so the rules have a single source of truth. The activity is
 * `singleTask` in its own task: a second request arrives in `onNewIntent`.
 */
object GateContract {

    /** Who asked for the gate. */
    enum class Source {
        /** A launch from Shunya (drawer, search, favorites, gestures). */
        Launcher,

        /** The accessibility service saw a blocked app come to the foreground. */
        SystemWide,
    }

    private const val EXTRA_APP_ID = "dev.apn7.shunya.gate.APP_ID"
    private const val EXTRA_LABEL = "dev.apn7.shunya.gate.LABEL"
    private const val EXTRA_SOURCE = "dev.apn7.shunya.gate.SOURCE"

    /** Intent that shows the gate for [app]; safe to start from any context (adds NEW_TASK). */
    fun intent(context: Context, app: AppKey, label: String, source: Source): Intent =
        Intent()
            .setComponent(ShunyaComponents.gateActivity(context))
            .putExtra(EXTRA_APP_ID, app.id)
            .putExtra(EXTRA_LABEL, label)
            .putExtra(EXTRA_SOURCE, source.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

    /** The app to gate, or null for a malformed intent (the gate should then just finish). */
    fun appKey(intent: Intent): AppKey? = intent.getStringExtra(EXTRA_APP_ID)?.let { AppKey.fromId(it) }

    /** Display label captured at launch time (may be empty). */
    fun label(intent: Intent): String = intent.getStringExtra(EXTRA_LABEL).orEmpty()

    fun source(intent: Intent): Source {
        val name = intent.getStringExtra(EXTRA_SOURCE)
        return Source.entries.firstOrNull { it.name == name } ?: Source.Launcher
    }
}
