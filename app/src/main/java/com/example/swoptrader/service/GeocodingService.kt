package com.example.swoptrader.service

import android.content.Context
import android.location.Geocoder
import com.example.swoptrader.data.model.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingService @Inject constructor(
    private val context: Context
) {
    
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Location? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                
                if (addresses?.isNotEmpty() == true) {
                    val address = addresses[0]
                    Location(
                        latitude = latitude,
                        longitude = longitude,
                        address = address.getAddressLine(0) ?: "Unknown Address",
                        city = address.locality ?: address.subAdminArea ?: "Unknown City",
                        country = address.countryName ?: "Unknown Country"
                    )
                } else {
                    // Fallback location if geocoding fails
                    Location(
                        latitude = latitude,
                        longitude = longitude,
                        address = "Unknown Address",
                        city = "Unknown City",
                        country = "Unknown Country"
                    )
                }
            } catch (e: Exception) {
                // Fallback location on error
                Location(
                    latitude = latitude,
                    longitude = longitude,
                    address = "Unknown Address",
                    city = "Unknown City",
                    country = "Unknown Country"
                )
            }
        }
    }
    
    suspend fun getCurrentLocationWithGeocoding(): Location? {
        return withContext(Dispatchers.IO) {
            try {
                val locationService = LocationService(context)
                val currentLocation = locationService.getCurrentLocation()
                
                if (currentLocation != null) {
                    reverseGeocode(currentLocation.latitude, currentLocation.longitude)
                } else {
                    // Return null if no location available instead of fallback
                    // This prevents distance calculation issues
                    null
                }
            } catch (e: Exception) {
                // Return null on error instead of fallback
                // This prevents distance calculation issues
                null
            }
        }
    }
    
    suspend fun getCurrentLocationWithGeocodingOrFallback(): Location {
        return withContext(Dispatchers.IO) {
            try {
                val locationService = LocationService(context)
                val currentLocation = locationService.getCurrentLocation()
                
                if (currentLocation != null) {
                    reverseGeocode(currentLocation.latitude, currentLocation.longitude) ?: getDefaultLocation()
                } else {
                    getDefaultLocation()
                }
            } catch (e: Exception) {
                getDefaultLocation()
            }
        }
    }
    
    private fun getDefaultLocation(): Location {
        return Location(
            latitude = -26.2041,
            longitude = 28.0473,
            address = "Johannesburg, South Africa",
            city = "Johannesburg",
            country = "South Africa"
        )
    }
}
