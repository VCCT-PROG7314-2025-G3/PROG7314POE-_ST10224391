package com.example.swoptrader.service

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class PlacesService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val placesClient: PlacesClient by lazy {
        Places.initialize(context, context.getString(com.example.swoptrader.R.string.google_maps_key))
        Places.createClient(context)
    }
    
    suspend fun getAutocompletePredictions(
        query: String,
        location: LatLng? = null,
        radius: Int = 50000 // 50km radius
    ): List<AutocompletePrediction> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val token = AutocompleteSessionToken.newInstance()
                
                val requestBuilder = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setSessionToken(token)
                
                // Add location bias if available
                location?.let { loc ->
                    val bounds = com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
                        LatLng(loc.latitude - 0.1, loc.longitude - 0.1),
                        LatLng(loc.latitude + 0.1, loc.longitude + 0.1)
                    )
                    requestBuilder.setLocationBias(bounds)
                }
                
                val request = requestBuilder.build()
                
                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        continuation.resume(response.autocompletePredictions)
                    }
                    .addOnFailureListener { exception ->
                        println("Places API Error: ${exception.message}")
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                println("Places Service Error: ${e.message}")
                continuation.resumeWithException(e)
            }
        }
    }
    
    suspend fun getPlaceDetails(placeId: String): Place? {
        return suspendCancellableCoroutine { continuation ->
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
            
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
            
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    continuation.resume(response.place)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}
