package dev.apn7.shunya.core.designsystem

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Edge-to-edge with transparent system bars whose icons match the Shunya theme (not the system
 * theme: Ink on a light phone still needs light icons). Call it again whenever the theme or the
 * "show status bar" setting changes; it is cheap and idempotent.
 */
fun ComponentActivity.applySystemBars(darkTheme: Boolean, showStatusBar: Boolean = true) {
    val style = if (darkTheme) {
        SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)

    val controller = WindowCompat.getInsetsController(window, window.decorView)
    if (showStatusBar) {
        controller.show(WindowInsetsCompat.Type.statusBars())
    } else {
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
}
