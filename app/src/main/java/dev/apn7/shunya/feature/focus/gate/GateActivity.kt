package dev.apn7.shunya.feature.focus.gate

import android.os.Bundle
import androidx.activity.ComponentActivity
import dev.apn7.shunya.appContainer
import dev.apn7.shunya.core.system.GateContract

/**
 * Mindful pause / limit reached / blocked screen, started through [GateContract].
 * Stub behaviour: opens the requested app straight away.
 */
class GateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = GateContract.appKey(intent)
        if (app != null) appContainer.appLauncher.launchBypassingPolicy(app)
        finish()
    }
}
