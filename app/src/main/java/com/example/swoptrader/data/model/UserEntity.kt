package com.example.swoptrader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val locationAddress: String? = null,
    val locationCity: String? = null,
    val locationCountry: String? = null,
    val tradeScore: Int = 0,
    val level: Int = 1,
    val carbonSaved: Double = 0.0,
    val isVerified: Boolean = false
)

// Extension functions to convert between User and UserEntity
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        profileImageUrl = profileImageUrl,
        locationLatitude = location?.latitude,
        locationLongitude = location?.longitude,
        locationAddress = location?.address,
        locationCity = location?.city,
        locationCountry = location?.country,
        tradeScore = tradeScore,
        level = level,
        carbonSaved = carbonSaved,
        isVerified = isVerified
    )
}

fun UserEntity.toUser(): User {
    return User(
        id = id,
        name = name,
        email = email,
        profileImageUrl = profileImageUrl,
        location = if (locationLatitude != null && locationLongitude != null) {
            Location(
                latitude = locationLatitude,
                longitude = locationLongitude,
                address = locationAddress ?: "",
                city = locationCity,
                country = locationCountry
            )
        } else null,
        tradeScore = tradeScore,
        level = level,
        carbonSaved = carbonSaved,
        isVerified = isVerified
    )
}

