package dev.apn7.shunya.feature.focus.gate

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.appContainer
import dev.apn7.shunya.core.designsystem.applySystemBars
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.core.system.GateContract
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startSafely
import java.time.LocalDate

/** One request to show the gate; [serial] makes a repeated request for the same app start fresh. */
internal data class GateRequest(
    val app: AppKey,
    val label: String,
    val source: GateContract.Source,
    val serial: Int,
) {
    companion object {
        private var nextSerial = 0

        fun from(intent: Intent): GateRequest? {
            val app = GateContract.appKey(intent) ?: return null
            nextSerial += 1
            return GateRequest(app, GateContract.label(intent), GateContract.source(intent), nextSerial)
        }
    }
}

/**
 * Mindful pause / limit reached / blocked screen, started through [GateContract]. It asks the
 * launch policy itself (see [GateRoute]). `singleTask` in its own task and `noHistory`: a repeat
 * request arrives in [onNewIntent], and leaving the screen closes it.
 *
 * "Not now"/"Close": a launch from Shunya just finishes (Shunya is underneath); a system-wide
 * block goes home first, because the blocked app is underneath.
 */
class GateActivity : ComponentActivity() {

    private var request: GateRequest? by mutableStateOf(null)

    private val actions: GateActions = Actions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val first = GateRequest.from(intent)
        if (first == null) {
            finish()
            return
        }
        request = first
        applySystemBars(darkTheme = isSystemDark())
        val container = appContainer

        setContent {
            val settings by container.settingsRepository.settings.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalAppContainer provides container) {
                ShunyaTheme(appearance = settings.appearance) {
                    val dark = ShunyaTheme.colors.isDark
                    LaunchedEffect(dark) { applySystemBars(dark, showStatusBar = true) }
                    val current: GateRequest? = request
                    if (current != null) {
                        key(current.serial) {
                            GateRoute(request = current, actions = actions)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val next = GateRequest.from(intent)
        if (next != null) request = next
    }

    private fun isSystemDark(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private inner class Actions : GateActions {
        override fun leave() {
            if (request?.source == GateContract.Source.SystemWide) startSafely(SystemIntents.home())
            finish()
        }

        override fun open() {
            val app: AppKey? = request?.app
            if (app != null) {
                GatePasses.grant(app.packageName)
                appContainer.appLauncher.launchBypassingPolicy(app)
            }
            finish()
        }

        override fun extendAndOpen() {
            val app: AppKey? = request?.app
            if (app != null) {
                val today = LocalDate.now().toString()
                appContainer.focusConfigRepository.edit {
                    it.copy(extensions = it.extensions.plus(app.packageName, ProductLimits.LIMIT_EXTENSION_MINUTES, today))
                }
            }
            open()
        }
    }
}
