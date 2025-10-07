package com.example.swoptrader.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    private val context: Context
) {
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null
        
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        return try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }
    }
    
    fun getLastKnownLocation(): Location? {
        return getCurrentLocation()
    }
}

