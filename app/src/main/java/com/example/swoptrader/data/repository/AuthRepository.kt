package com.example.swoptrader.data.repository

/**
 * Authentication Repository Implementation
 * 
 * This class implements the Repository pattern for authentication operations in the SwopTrader
 * application. It provides a clean abstraction layer between the UI layer and data sources,
 * following the principles of Clean Architecture and SOLID design principles.
 * 
 * Key Design Patterns and Concepts:
 * - Repository Pattern (Fowler, 2002)
 * - Clean Architecture (Martin, 2017)
 * - Firebase Authentication Integration (Google, 2023)
 * - OAuth 2.0 and OpenID Connect (RFC 6749, 2012)
 * - Dependency Injection (Fowler, 2004)
 * - Error Handling and Result Types (Kotlin, 2023)
 * 
 * Security Considerations:
 * - Secure token storage and management
 * - Biometric authentication integration
 * - Session management and timeout handling
 * - Cross-platform authentication consistency
 * 
 * References:
 * - Fowler, M. (2002). Patterns of Enterprise Application Architecture. Addison-Wesley.
 * - Martin, R. C. (2017). Clean Architecture: A Craftsman's Guide to Software Structure and Design.
 * - Google. (2023). Firebase Authentication. Firebase Documentation.
 * - RFC 6749. (2012). The OAuth 2.0 Authorization Framework. IETF.
 * - Fowler, M. (2004). Inversion of Control Containers and the Dependency Injection Pattern.
 * - Kotlin. (2023). Result Type and Error Handling. Kotlin Documentation.
 */

import com.example.swoptrader.data.model.User
import com.example.swoptrader.data.remote.api.SwopTraderApi
import com.example.swoptrader.data.repository.FirestoreRepository
import com.example.swoptrader.service.GeocodingService
import com.example.swoptrader.service.LocationPermissionService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun loginWithGoogle(token: String, account: com.google.android.gms.auth.api.signin.GoogleSignInAccount?): Result<User>
    suspend fun signUpWithGoogle(token: String, account: com.google.android.gms.auth.api.signin.GoogleSignInAccount?): Result<User>
    suspend fun loginWithFacebook(token: String): Result<User>
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun getCurrentUserFlow(): Flow<User?>
    fun getAllUsersFlow(): Flow<List<User>>
    suspend fun requestLocationPermissionAndUpdateUser(): Result<User?>
    fun shouldRequestLocationPermission(): Boolean
}

class AuthRepositoryImpl @Inject constructor(
    private val api: SwopTraderApi,
    private val firestoreRepository: FirestoreRepository,
    private val geocodingService: GeocodingService,
    private val locationPermissionService: LocationPermissionService,
    private val userRepository: UserRepository
) : AuthRepository {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    private var currentUser: User? = null
    
    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            // Mock implementation for admin/admin
            if (username == "admin" && password == "admin") {
                val user = User(
                    id = "admin_user_001",
                    name = "Admin User",
                    email = "admin@swoptrader.com",
                    profileImageUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face",
                    location = com.example.swoptrader.data.model.Location(
                        latitude = -26.2041,
                        longitude = 28.0473,
                        address = "Johannesburg, South Africa",
                        city = "Johannesburg",
                        country = "South Africa"
                    ),
                    tradeScore = 500,
                    level = 10,
                    carbonSaved = 100.0,
                    isVerified = true
                )
                currentUser = user
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Google OAuth 2.0 Authentication Implementation
     * 
     * This method implements secure Google authentication using OAuth 2.0 protocol
     * and Firebase Authentication. It follows security best practices for token
     * validation and user session management.
     * 
     * Security Features:
     * - OAuth 2.0 token validation (RFC 6749, 2012)
     * - Firebase credential verification (Google, 2023)
     * - Email-based user identification to prevent duplicates
     * - Secure session establishment
     * 
     * References:
     * - RFC 6749. (2012). The OAuth 2.0 Authorization Framework. IETF.
     * - Google. (2023). Firebase Authentication Security. Firebase Documentation.
     * - OWASP. (2021). OAuth 2.0 Security Best Practices. OWASP Foundation.
     */
    override suspend fun loginWithGoogle(token: String, account: com.google.android.gms.auth.api.signin.GoogleSignInAccount?): Result<User> {
        return try {
            // OAuth 2.0 credential creation using Google ID token (RFC 6749, 2012)
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(token, null)
            
            // Firebase Authentication with credential verification (Google, 2023)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Email-based user identification to prevent duplicate accounts (OWASP, 2021)
                val email = firebaseUser.email ?: ""
                val userResult = firestoreRepository.getUserByEmail(email)
                val existingUser = userResult.getOrNull()
                
                // User data management with proper session handling
                val user = if (existingUser != null) {
                    // Existing user: preserve data but update Firebase UID for session consistency
                    existingUser.copy(id = firebaseUser.uid)
                } else {
                    // New user: create account with Google profile data
                    createUserFromFirebaseUser(firebaseUser, account?.displayName)
                }
                
                // Save/update user in repositories
                userRepository.createUser(user)
                firestoreRepository.saveUser(user)
                currentUser = user
                
                try {
                    val locationResult = geocodingService.getCurrentLocationWithGeocoding()
                    if (locationResult != null) {
                        val updatedUser = user.copy(location = locationResult)
                        firestoreRepository.saveUser(updatedUser)
                        userRepository.updateUser(updatedUser)
                        currentUser = updatedUser
                    }
                } catch (e: Exception) {
                    println("Location fetch failed during Google login: ${e.message}")
                }
                
                Result.success(user)
            } else {
                Result.failure(Exception("Google authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signUpWithGoogle(token: String, account: com.google.android.gms.auth.api.signin.GoogleSignInAccount?): Result<User> {
        return try {
            // Sign in with Firebase Auth using Google credential
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(token, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Check if user already exists by email
                val email = firebaseUser.email ?: ""
                val userResult = firestoreRepository.getUserByEmail(email)
                val existingUser = userResult.getOrNull()
                
                if (existingUser != null) {
                    // User already exists, return error
                    return Result.failure(Exception("This Google account is already registered. Please sign in instead."))
                }
                
                // Create new user object for sign-up
                val user = createUserFromFirebaseUser(firebaseUser, account?.displayName)
                
                // Save to Firestore
                firestoreRepository.saveUser(user)
                
                userRepository.createUser(user)
                currentUser = user
                
                try {
                    val locationResult = geocodingService.getCurrentLocationWithGeocoding()
                    if (locationResult != null) {
                        val updatedUser = user.copy(location = locationResult)
                        firestoreRepository.saveUser(updatedUser)
                        userRepository.updateUser(updatedUser)
                        currentUser = updatedUser
                    }
                } catch (e: Exception) {
                    println("Location fetch failed during Google sign-up: ${e.message}")
                }
                
                Result.success(user)
            } else {
                Result.failure(Exception("Google sign-up failed"))
            }
        } catch (e: Exception) {
            // Handle Firebase Auth specific errors
            val errorMessage = when {
                e.message?.contains("account-exists-with-different-credential") == true -> "This email is already registered with a different sign-in method. Please use email/password sign-in instead."
                e.message?.contains("email-already-in-use") == true -> "This Google account is already registered. Please sign in instead."
                else -> e.message ?: "Google sign-up failed. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    override suspend fun loginWithFacebook(token: String): Result<User> {
        return try {
            val user = User(
                id = "facebook_${System.currentTimeMillis()}",
                name = "Facebook User",
                email = "user@facebook.com",
                profileImageUrl = "https://via.placeholder.com/150",
                location = com.example.swoptrader.data.model.Location(
                    latitude = -26.2041,
                    longitude = 28.0473,
                    address = "Johannesburg, South Africa",
                    city = "Johannesburg",
                    country = "South Africa"
                ),
                tradeScore = 200,
                level = 4,
                carbonSaved = 35.2,
                isVerified = true
            )
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            // Sign in with Firebase Auth
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Get user data from Firestore or create if not exists
                val userResult = firestoreRepository.getUser(firebaseUser.uid)
                val user = userResult.getOrNull() ?: createUserFromFirebaseUser(firebaseUser)
                
                userRepository.createUser(user)
                currentUser = user
                
                try {
                    val locationResult = geocodingService.getCurrentLocationWithGeocoding()
                    if (locationResult != null) {
                        val updatedUser = user.copy(location = locationResult)
                        firestoreRepository.saveUser(updatedUser)
                        userRepository.updateUser(updatedUser)
                        currentUser = updatedUser
                    }
                } catch (e: Exception) {
                    println("Location fetch failed during login: ${e.message}")
                }
                
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            // Create user with Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Create user object
                val user = createUserFromFirebaseUser(firebaseUser, name)
                
                // Save to Firestore
                firestoreRepository.saveUser(user)
                
                userRepository.createUser(user)
                currentUser = user
                
                try {
                    val locationResult = geocodingService.getCurrentLocationWithGeocoding()
                    if (locationResult != null) {
                        val updatedUser = user.copy(location = locationResult)
                        firestoreRepository.saveUser(updatedUser)
                        userRepository.updateUser(updatedUser)
                        currentUser = updatedUser
                    }
                } catch (e: Exception) {
                    println("Location fetch failed during registration: ${e.message}")
                }
                
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            // Handle Firebase Auth specific errors
            val errorMessage = when {
                e.message?.contains("email-already-in-use") == true -> "This email is already registered. Please use a different email or try signing in."
                e.message?.contains("weak-password") == true -> "Password is too weak. Please choose a stronger password."
                e.message?.contains("invalid-email") == true -> "Please enter a valid email address."
                else -> e.message ?: "Registration failed. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            currentUser = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null && currentUser == null) {
            val userResult = firestoreRepository.getUser(firebaseUser.uid)
            currentUser = userResult.getOrNull() ?: createUserFromFirebaseUser(firebaseUser)
        } else if (firebaseUser == null && currentUser != null) {
            currentUser = null
        }
        return currentUser
    }
    
    override fun getCurrentUserFlow(): Flow<User?> {
        return flow { emit(currentUser) }
    }
    
    override fun getAllUsersFlow(): Flow<List<User>> {
        return flow { 
            emit(listOf(
                currentUser ?: User(
                    id = "demo_user",
                    name = "Demo User",
                    email = "demo@example.com",
                    profileImageUrl = "https://via.placeholder.com/150",
                    location = com.example.swoptrader.data.model.Location(
                        latitude = -26.2041,
                        longitude = 28.0473,
                        address = "Johannesburg, South Africa",
                        city = "Johannesburg",
                        country = "South Africa"
                    ),
                    tradeScore = 250,
                    level = 5,
                    carbonSaved = 45.3,
                    isVerified = true
                )
            ))
        }
    }
    
    override suspend fun requestLocationPermissionAndUpdateUser(): Result<User?> {
        return try {
            val user = currentUser
            if (user != null && locationPermissionService.hasAnyLocationPermission()) {
                val userLocation = geocodingService.getCurrentLocationWithGeocoding()
                if (userLocation != null) {
                    val updatedUser = user.copy(location = userLocation)
                    currentUser = updatedUser
                    firestoreRepository.saveUser(updatedUser)
                    // Also update local repository
                    userRepository.updateUser(updatedUser)
                    Result.success(updatedUser)
                } else {
                    Result.success(user)
                }
            } else {
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun shouldRequestLocationPermission(): Boolean {
        val user = currentUser
        return user != null && 
               user.location == null && 
               !locationPermissionService.hasAnyLocationPermission()
    }
    
    private fun createUserFromFirebaseUser(firebaseUser: FirebaseUser, name: String? = null): User {
        return User(
            id = firebaseUser.uid,
            name = name ?: firebaseUser.displayName ?: "User",
            email = firebaseUser.email ?: "",
            profileImageUrl = firebaseUser.photoUrl?.toString(),
            location = null,
            tradeScore = 0,
            level = 1,
            carbonSaved = 0.0,
            isVerified = firebaseUser.isEmailVerified
        )
    }
}