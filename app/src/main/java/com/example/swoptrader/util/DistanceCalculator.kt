package com.example.swoptrader.util

import kotlin.math.*

object DistanceCalculator {
    
    /**
     * Calculate the distance between two points on Earth using the Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Calculate distance between two locations
     * @param userLocation User's current location
     * @param itemLocation Item's location
     * @return Distance in kilometers, or null if either location is null
     */
    fun calculateDistanceBetweenLocations(
        userLocation: com.example.swoptrader.data.model.Location?,
        itemLocation: com.example.swoptrader.data.model.Location?
    ): Double? {
        if (userLocation == null || itemLocation == null) {
            return null
        }
        
        // If locations are exactly the same, return 0
        if (userLocation.latitude == itemLocation.latitude && 
            userLocation.longitude == itemLocation.longitude) {
            return 0.0
        }
        
        return calculateDistance(
            userLocation.latitude,
            userLocation.longitude,
            itemLocation.latitude,
            itemLocation.longitude
        )
    }
}
