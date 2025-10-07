package com.example.swoptrader.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null,
    
    @SerializedName("location")
    val location: Location? = null,
    
    @SerializedName("tradeScore")
    val tradeScore: Int = 0,
    
    @SerializedName("level")
    val level: Int = 1,
    
    @SerializedName("carbonSaved")
    val carbonSaved: Double = 0.0,
    
    @SerializedName("isVerified")
    val isVerified: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("lastActive")
    val lastActive: Long = System.currentTimeMillis()
)

data class Location(
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("country")
    val country: String? = null
)