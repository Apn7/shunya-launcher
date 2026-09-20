package dev.apn7.shunya.feature.focus.gate

/**
 * Apps the user has just chosen to open from the gate. The accessibility service leaves such an app
 * alone until a different app comes to the foreground, so "Open" never bounces straight back to the
 * gate (the extension write, for example, may land a moment after the app is already in front).
 * In memory only: the gate and the service run in the same process.
 */
internal object GatePasses {

    private val passes = HashSet<String>()

    @Synchronized
    fun grant(packageName: String) {
        passes.add(packageName)
    }

    @Synchronized
    fun has(packageName: String): Boolean = packageName in passes

    /** [packageName] is now in front: every other app's pass ends. */
    @Synchronized
    fun onForeground(packageName: String) {
        val keep = packageName in passes
        passes.clear()
        if (keep) passes.add(packageName)
    }
}
