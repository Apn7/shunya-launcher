package dev.apn7.shunya.feature.home.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Listens to the protected system broadcast [action] only while the screen is started (home is
 * visible) and [enabled]. [onUpdate] runs on every start with the sticky intent, if the broadcast
 * has one (else null), and again with every broadcast received. Unregisters on stop and dispose.
 */
@Composable
internal fun SystemBroadcastEffect(enabled: Boolean, action: String, onUpdate: (Intent?) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnUpdate by rememberUpdatedState(onUpdate)
    DisposableEffect(enabled, action, context, lifecycleOwner) {
        if (!enabled) return@DisposableEffect onDispose { }
        val appContext = context.applicationContext
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                latestOnUpdate(intent)
            }
        }
        var registered = false
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && !registered) {
                registered = true
                // Protected system broadcast: "exported" lets the system deliver it on every API level.
                val sticky = ContextCompat.registerReceiver(
                    appContext,
                    receiver,
                    IntentFilter(action),
                    ContextCompat.RECEIVER_EXPORTED,
                )
                latestOnUpdate(sticky)
            } else if (event == Lifecycle.Event.ON_STOP && registered) {
                registered = false
                appContext.unregisterReceiver(receiver)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (registered) {
                registered = false
                appContext.unregisterReceiver(receiver)
            }
        }
    }
}
