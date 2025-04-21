package com.chadbingham.thereyouare.data

import android.location.Location
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.database
import timber.log.Timber
import javax.inject.Inject

class LocationDatabase @Inject constructor() {

    private val database = Firebase.database
    private val locationsRef = database.getReference("locations")

    private val user: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    fun updateMyLocation(location: Location) {
        user?.let {
            locationsRef.setValue(UserLocation(it.uid, location.latitude, location.longitude))
        } ?: Timber.e("User not signed in")
    }
}