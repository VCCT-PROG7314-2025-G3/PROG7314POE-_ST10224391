package com.example.swoptrader.service

import com.example.swoptrader.data.model.Item
import com.example.swoptrader.data.model.Location
import com.example.swoptrader.util.DistanceCalculator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DistanceUpdateService @Inject constructor() {
    
    /**
     * Update distance for a list of items based on user's current location
     * @param items List of items to update
     * @param userLocation User's current location
     * @return List of items with updated distances
     */
    fun updateItemDistances(items: List<Item>, userLocation: Location?): List<Item> {
        if (userLocation == null) {
            return items.map { it.copy(distance = null) }
        }
        
        return items.map { item ->
            val distance = DistanceCalculator.calculateDistanceBetweenLocations(userLocation, item.location)
            item.copy(distance = distance)
        }
    }
    
    /**
     * Update distance for a single item based on user's current location
     * @param item Item to update
     * @param userLocation User's current location
     * @return Item with updated distance
     */
    fun updateItemDistance(item: Item, userLocation: Location?): Item {
        if (userLocation == null) {
            return item.copy(distance = null)
        }
        
        val distance = DistanceCalculator.calculateDistanceBetweenLocations(userLocation, item.location)
        return item.copy(distance = distance)
    }
}

