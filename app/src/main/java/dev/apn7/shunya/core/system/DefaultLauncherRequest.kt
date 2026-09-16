package dev.apn7.shunya.core.system

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

/**
 * Returns a function that asks the user to make Shunya the default home app: the RoleManager
 * dialog on Android 10+, otherwise (or when Shunya already holds the role) the system
 * "Default home app" screen. [onResult] runs when the user is back, e.g. to refresh
 * `PermissionsRepository`.
 */
@Composable
fun rememberDefaultLauncherRequest(onResult: () -> Unit = {}): () -> Unit {
    val context = LocalContext.current
    val latestOnResult by rememberUpdatedState(onResult)
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        latestOnResult()
    }
    return remember(context, roleLauncher) {
        {
            val roleIntent = homeRoleRequest(context)
            val launched = roleIntent != null && try {
                roleLauncher.launch(roleIntent)
                true
            } catch (e: ActivityNotFoundException) {
                false
            }
            if (!launched) context.startSafely(SystemIntents.homeSettings())
        }
    }
}

/** RoleManager's request dialog when it can be shown, else null. */
private fun homeRoleRequest(context: Context): Intent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    val roleManager = context.getSystemService(RoleManager::class.java) ?: return null
    if (!roleManager.isRoleAvailable(RoleManager.ROLE_HOME) || roleManager.isRoleHeld(RoleManager.ROLE_HOME)) return null
    return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
}
