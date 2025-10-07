package com.example.swoptrader.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationPermissionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasCoarseLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasAnyLocationPermission(): Boolean {
        return hasLocationPermission() || hasCoarseLocationPermission()
    }
    
    fun getLocationPermissionRationale(): String {
        return "Location access is needed to show nearby items and help other users find your listings. This helps create a better trading experience for everyone."
    }
}

