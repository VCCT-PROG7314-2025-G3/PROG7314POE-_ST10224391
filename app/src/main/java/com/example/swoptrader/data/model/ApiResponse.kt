package com.example.swoptrader.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: ApiError? = null
)

data class ApiError(
    @SerializedName("code")
    val code: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("details")
    val details: Map<String, String>? = null
)

data class PaginatedResponse<T>(
    @SerializedName("data")
    val data: List<T>,
    
    @SerializedName("pagination")
    val pagination: PaginationInfo
)

data class PaginationInfo(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("pages")
    val pages: Int
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String? = null,
    
    @SerializedName("provider")
    val provider: AuthProvider? = null,
    
    @SerializedName("idToken")
    val idToken: String? = null
)

data class LoginResponse(
    @SerializedName("user")
    val user: User,
    
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String,
    
    @SerializedName("expiresIn")
    val expiresIn: Long
)

data class CreateItemRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("condition")
    val condition: String,
    
    @SerializedName("images")
    val images: List<String> = emptyList(),
    
    @SerializedName("location")
    val location: Location? = null,
    
    @SerializedName("desiredTrades")
    val desiredTrades: List<String> = emptyList()
)

data class CreateOfferRequest(
    @SerializedName("requestedItemId")
    val requestedItemId: String,
    
    @SerializedName("offeredItemId")
    val offeredItemId: String,
    
    @SerializedName("message")
    val message: String? = null
)

enum class AuthProvider(
    @SerializedName("value")
    val value: String
) {
    EMAIL("email"),
    GOOGLE("google"),
    FACEBOOK("facebook")
}

data class RegisterDeviceTokenRequest(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("token")
    val token: String,
    
    @SerializedName("deviceId")
    val deviceId: String? = null
)

data class DeviceTokenResponse(
    @SerializedName("tokens")
    val tokens: List<String> = emptyList(),
    
    @SerializedName("registeredAt")
    val registeredAt: String? = null
)

data class OfferNotificationRequest(
    @SerializedName("offerId")
    val offerId: String,
    
    @SerializedName("recipientUserId")
    val recipientUserId: String,
    
    @SerializedName("senderUserId")
    val senderUserId: String,
    
    @SerializedName("senderName")
    val senderName: String,
    
    @SerializedName("itemName")
    val itemName: String? = null,
    
    @SerializedName("message")
    val message: String? = null
)

data class NotificationDispatchResponse(
    @SerializedName("successCount")
    val successCount: Int = 0,
    
    @SerializedName("failureCount")
    val failureCount: Int = 0,
    
    @SerializedName("invalidTokens")
    val invalidTokens: List<String> = emptyList(),
    
    @SerializedName("skipped")
    val skipped: Boolean? = null,
    
    @SerializedName("reason")
    val reason: String? = null
)


