package dev.apn7.shunya.core.system

import android.Manifest
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import dev.apn7.shunya.core.model.PermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Current state of every special access (see [PermissionStatus]). None of these can be observed
 * by callback, so `MainActivity` calls [refresh] on every resume; screens that send the user to
 * system settings should call it again when they come back.
 */
class PermissionsRepository(context: Context) {

    private val appContext = context.applicationContext
    private val current = MutableStateFlow(read())

    val status: StateFlow<PermissionStatus> = current.asStateFlow()

    /** Re-reads everything (a few cheap system calls). */
    fun refresh() {
        current.value = read()
    }

    private fun read() = PermissionStatus(
        isDefaultLauncher = isDefaultLauncher(),
        hasUsageAccess = hasUsageAccess(),
        hasNotificationAccess = hasNotificationAccess(),
        isAccessibilityEnabled = isAccessibilityEnabled(),
        canWriteSecureSettings = canWriteSecureSettings(),
    )

    private fun isDefaultLauncher(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = appContext.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        @Suppress("DEPRECATION")
        val resolved = appContext.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == appContext.packageName
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = appContext.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), appContext.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), appContext.packageName)
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            appContext.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    private fun hasNotificationAccess(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(appContext).contains(appContext.packageName)

    private fun isAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val ours = ShunyaComponents.accessibilityService(appContext)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == ours }
    }

    private fun canWriteSecureSettings(): Boolean =
        appContext.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
}
