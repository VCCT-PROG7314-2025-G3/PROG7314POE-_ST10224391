package com.example.swoptrader.data.repository

import com.example.swoptrader.data.model.User
import com.example.swoptrader.data.remote.api.SwopTraderApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface UserRepository {
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun getAllUsers(): Result<List<User>>
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    fun getUsersFlow(): Flow<List<User>>
}

class UserRepositoryImpl @Inject constructor(
    private val api: SwopTraderApi,
    private val firestoreRepository: FirestoreRepository
) : UserRepository {
    
    private val users = mutableListOf<User>()
    
    init {
        initializeSampleData()
    }
    
    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            // Try REST API first
            try {
                val response = api.getUserById(userId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val user = apiResponse.data
                        if (user != null) {
                            // Cache user locally and in Firebase
                            cacheUserLocally(user)
                            firestoreRepository.saveUser(user) // Sync to Firebase
                            return Result.success(user)
                        }
                    }
                }
            } catch (apiException: Exception) {
                // API failed, try Firebase
                println("API failed for getUserById, trying Firebase: ${apiException.message}")
            }
            
            // Try Firebase
            try {
                val firebaseResult = firestoreRepository.getUser(userId)
                firebaseResult.fold(
                    onSuccess = { user ->
                        if (user != null) {
                            cacheUserLocally(user)
                        }
                        return Result.success(user)
                    },
                    onFailure = { firebaseException ->
                        println("Firebase failed for getUserById: ${firebaseException.message}")
                    }
                )
            } catch (firebaseException: Exception) {
                println("Firebase exception for getUserById: ${firebaseException.message}")
            }
            
            // Fallback to local storage
            val user = users.find { it.id == userId }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun cacheUserLocally(user: User) {
        val existingIndex = users.indexOfFirst { it.id == user.id }
        if (existingIndex >= 0) {
            users[existingIndex] = user
        } else {
            users.add(user)
        }
    }
    
    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            Result.success(users.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createUser(user: User): Result<User> {
        return try {
            // Try REST API first
            try {
                val response = api.createUser(user)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val createdUser = apiResponse.data ?: user
                        // Cache locally
                        cacheUserLocally(createdUser)
                        return Result.success(createdUser)
                    }
                }
            } catch (apiException: Exception) {
                // API failed, fall back to local storage
                println("API failed for createUser, falling back to local storage: ${apiException.message}")
            }
            
            // Fallback to local storage
            users.add(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            // Try REST API first
            try {
                val response = api.updateUser(user.id, user)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val updatedUser = apiResponse.data ?: user
                        // Update local cache and Firebase
                        cacheUserLocally(updatedUser)
                        firestoreRepository.saveUser(updatedUser) // Sync to Firebase
                        return Result.success(updatedUser)
                    }
                }
            } catch (apiException: Exception) {
                println("API failed for updateUser, trying Firebase: ${apiException.message}")
            }
            
            // Try Firebase
            try {
                val firebaseResult = firestoreRepository.saveUser(user)
                firebaseResult.fold(
                    onSuccess = { updatedUser ->
                        cacheUserLocally(updatedUser)
                        return Result.success(updatedUser)
                    },
                    onFailure = { firebaseException ->
                        println("Firebase failed for updateUser: ${firebaseException.message}")
                    }
                )
            } catch (firebaseException: Exception) {
                println("Firebase exception for updateUser: ${firebaseException.message}")
            }
            
            // Fallback to local storage
            val userIndex = users.indexOfFirst { it.id == user.id }
            if (userIndex != -1) {
                users[userIndex] = user
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getUsersFlow(): Flow<List<User>> {
        return flow {
            emit(users.toList())
        }
    }
    
    private fun initializeSampleData() {
        val sampleUsers = listOf(
            User(
                id = "user_1",
                name = "John Doe",
                email = "john@example.com",
                profileImageUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face",
                tradeScore = 25,
                level = 3,
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Johannesburg, South Africa"
                ),
                isVerified = true,
                createdAt = System.currentTimeMillis() - 86400000 * 30
            ),
            User(
                id = "user_2",
                name = "Jane Smith",
                email = "jane@example.com",
                profileImageUrl = "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=150&h=150&fit=crop&crop=face",
                tradeScore = 18,
                level = 2,
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Johannesburg, South Africa"
                ),
                isVerified = true,
                createdAt = System.currentTimeMillis() - 86400000 * 45
            ),
            User(
                id = "user_3",
                name = "Mike Johnson",
                email = "mike@example.com",
                profileImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face",
                tradeScore = 42,
                level = 4,
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Johannesburg, South Africa"
                ),
                isVerified = true,
                createdAt = System.currentTimeMillis() - 86400000 * 60
            ),
            User(
                id = "admin_user_001",
                name = "Admin User",
                email = "admin@swoptrader.com",
                profileImageUrl = "",
                tradeScore = 100,
                level = 5,
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Johannesburg, South Africa"
                ),
                isVerified = true,
                createdAt = System.currentTimeMillis() - 86400000 * 365
            )
        )
        
        users.addAll(sampleUsers)
    }
}
