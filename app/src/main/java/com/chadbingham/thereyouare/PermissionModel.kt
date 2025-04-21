package com.chadbingham.thereyouare

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionModel @Inject constructor(private val context: Context) {

    private var askedForPermissions = false

    private var deniedPermissions = emptySet<String>()

    fun areLocationPermissionsGranted(): Boolean {
        return checkLocationPermission(context)
    }


    fun areBackgroundPermissionsGranted(): Boolean {
        return checkBackgroundPermission(context)
    }

    fun shouldShowRationale(): Boolean {
        if (!askedForPermissions) {
            askedForPermissions = true
            return false
        }
        return deniedPermissions.any {
            !context.shouldShowRequestPermissionRationale(it)
        }
    }

    private fun Context.shouldShowRequestPermissionRationale(permission: String): Boolean {
        return deniedPermissions.contains(permission)
    }

    fun permissionsDenied(permissions: Array<out String>) {
        deniedPermissions = permissions.toSet()
    }

    /**
     * Checks if the location permission is granted.
     *
     * @return True if the permission is granted, false otherwise.
     */
    private fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            ACCESS_COARSE_LOCATION
        ) == PERMISSION_GRANTED
    }

    private fun checkBackgroundPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            ACCESS_BACKGROUND_LOCATION
        ) == PERMISSION_GRANTED
    }
}