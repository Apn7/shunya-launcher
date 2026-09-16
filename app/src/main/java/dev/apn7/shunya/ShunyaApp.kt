package dev.apn7.shunya

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Process entry point. Creates the [AppContainer] once, before any activity or service, with an
 * app-wide scope that lives as long as the process (never cancelled; use it for work that must
 * outlive a screen, e.g. persisting a setting).
 */
class ShunyaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, CoroutineScope(SupervisorJob() + Dispatchers.Default))
    }
}

/**
 * The [AppContainer] from any context: activities, services, receivers. Composables use
 * `LocalAppContainer.current` instead.
 */
val Context.appContainer: AppContainer
    get() = (applicationContext as ShunyaApp).container
