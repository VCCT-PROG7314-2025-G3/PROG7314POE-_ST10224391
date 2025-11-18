package com.example.swoptrader.data.remote.api

import com.example.swoptrader.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SwopTraderApi {
    
    // User Management
    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<ApiResponse<User>>
    
    @POST("users")
    suspend fun createUser(@Body user: User): Response<ApiResponse<User>>
    
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body user: User
    ): Response<ApiResponse<User>>
    
    // Items
    @GET("items")
    suspend fun getItems(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("ownerId") ownerId: String? = null
    ): Response<ApiResponse<PaginatedResponse<Item>>>
    
    @GET("items/{itemId}")
    suspend fun getItemById(@Path("itemId") itemId: String): Response<ApiResponse<Item>>
    
    @POST("items")
    suspend fun createItem(@Body item: Item): Response<ApiResponse<Item>>
    
    @PUT("items/{itemId}")
    suspend fun updateItem(
        @Path("itemId") itemId: String,
        @Body item: Item
    ): Response<ApiResponse<Item>>
    
    @DELETE("items/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: String): Response<ApiResponse<Item>>
    
    // Offers
    @GET("offers")
    suspend fun getOffers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null,
        @Query("userId") userId: String? = null
    ): Response<ApiResponse<PaginatedResponse<Offer>>>
    
    @GET("offers/{offerId}")
    suspend fun getOfferById(@Path("offerId") offerId: String): Response<ApiResponse<Offer>>
    
    @POST("offers")
    suspend fun createOffer(@Body offer: Offer): Response<ApiResponse<Offer>>
    
    @PUT("offers/{offerId}")
    suspend fun updateOffer(
        @Path("offerId") offerId: String,
        @Body offer: Offer
    ): Response<ApiResponse<Offer>>
    
    // Chats
    @GET("chats")
    suspend fun getChats(@Query("userId") userId: String): Response<ApiResponse<List<Chat>>>
    
    @GET("chats/{chatId}/messages")
    suspend fun getChatMessages(
        @Path("chatId") chatId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<ChatMessage>>>
    
    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body message: ChatMessage
    ): Response<ApiResponse<ChatMessage>>
    
    // Trade History
    @GET("trades/history")
    suspend fun getTradeHistory(@Query("userId") userId: String): Response<ApiResponse<List<TradeHistory>>>
    
    @POST("trades/history")
    suspend fun createTradeHistory(@Body tradeHistory: TradeHistory): Response<ApiResponse<TradeHistory>>

    // Notifications
    @POST("notifications/token")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): Response<ApiResponse<DeviceTokenResponse>>

    @POST("notifications/offers")
    suspend fun sendOfferNotification(
        @Body request: OfferNotificationRequest
    ): Response<ApiResponse<NotificationDispatchResponse>>
}


