package com.reas.tracker.ui

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class LocationManager(val context: Context) {
    fun getUpdatedLocation(block: (Location?) -> Unit) {
        val permission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
                block.invoke(it)
            }
        } else {
            block.invoke(null)
        }
    }
}