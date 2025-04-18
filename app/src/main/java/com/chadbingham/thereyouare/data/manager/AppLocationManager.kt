package com.chadbingham.thereyouare.data.manager

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppLocationManager(private val context: Context) {

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var locationCallback: LocationCallback
    private var locationListener: LocationListener? = null
    private var isRequestingLocationUpdates = false
    private val scope = CoroutineScope(Dispatchers.Main)

    interface LocationUpdateListener {
        fun onLocationUpdate(location: Location)
    }

    /**
     * Starts requesting location updates.
     *
     * @param listener The listener to receive location updates.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(listener: LocationUpdateListener) {
        if (!checkLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            return
        }

        if (isRequestingLocationUpdates) {
            Log.w(TAG, "Already requesting location updates")
            return
        }

        isRequestingLocationUpdates = true

        // Create a LocationRequest for configuring the location update frequency and accuracy.
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
                setWaitForAccurateLocation(false)
                setMinUpdateIntervalMillis(5000)
                setMaxUpdateDelayMillis(15000)
            }.build()

        // Create a LocationCallback to handle the received location updates.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Location Update: $location")
                    listener.onLocationUpdate(location)
                }
            }
        }

        // Start requesting location updates using the FusedLocationProv
        // iderClient.
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Stops location updates.
     */
    fun stopLocationUpdates() {
        if (!isRequestingLocationUpdates) {
            Log.w(TAG, "Not currently requesting location updates")
            return
        }

        isRequestingLocationUpdates = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Gets the last known location.
     *
     * @param listener The listener to receive the last known location.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(listener: LocationUpdateListener) {
        if (!checkLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            return
        }
        scope.launch {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    Log.d(TAG, "Last Known Location: $it")
                    listener.onLocationUpdate(it)
                } ?: run {
                    Log.d(TAG, "Last known location is null")
                }
            }
        }
    }

    /**
     * Checks if the location permission is granted.
     *
     * @return True if the permission is granted, false otherwise.
     */
    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns if location permission has been granted.
     *
     * @return boolean
     */
    fun hasLocationPermission(): Boolean = checkLocationPermission()

    companion object {
        private const val TAG = "com.chadbingham.thereyouare.data.manager.AppLocationManager"
    }
}