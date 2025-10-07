package com.example.swoptrader.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.repository.AuthRepository
import com.example.swoptrader.service.GoogleSignInService
import com.example.swoptrader.service.SessionManager
import com.example.swoptrader.service.BiometricAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInService: GoogleSignInService,
    private val sessionManager: SessionManager,
    private val biometricAuthService: BiometricAuthService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(isLoggedIn = true)
        }
        
        // Load biometric setting and check availability
        val biometricAvailable = biometricAuthService.isBiometricAvailable()
        val biometricEnabled = sessionManager.isBiometricEnabled() && biometricAvailable
        
        _uiState.value = _uiState.value.copy(
            biometricEnabled = biometricEnabled,
            biometricAvailable = biometricAvailable
        )
    }
    
    fun signInWithGoogle() {
        viewModelScope.launch {
            // Sign out first to force account selection
            googleSignInService.signOut()
            
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = "",
                showGoogleSignIn = true,
                isSignUp = false
            )
        }
    }
    
    fun signUpWithGoogle() {
        viewModelScope.launch {
            // Sign out first to force account selection
            googleSignInService.signOut()
            
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = "",
                showGoogleSignIn = true,
                isSignUp = true
            )
        }
    }
    
    fun handleGoogleSignInResult(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showGoogleSignIn = false)
            
            if (account != null) {
                try {
                    val idToken = account.idToken
                    if (idToken.isNullOrEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Google authentication failed: No token received. Please try again.",
                            isSignUp = false
                        )
                        return@launch
                    }
                    
                    // Log account details for debugging (remove in production)
                    println("Google account details:")
                    println("ID: ${account.id}")
                    println("Email: ${account.email}")
                    println("Display Name: ${account.displayName}")
                    println("Has ID Token: ${!idToken.isNullOrEmpty()}")
                    println("Is Sign Up: ${_uiState.value.isSignUp}")
                    
                    val result = if (_uiState.value.isSignUp) {
                        // Handle Google sign-up
                        authRepository.signUpWithGoogle(idToken, account)
                    } else {
                        // Handle Google sign-in
                        authRepository.loginWithGoogle(idToken, account)
                    }
                    
                    result.fold(
                        onSuccess = { loggedInUser ->
                            // Save user session
                            sessionManager.saveUserSession(loggedInUser, _uiState.value.biometricEnabled)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isSignUp = false
                        )
                        // Check for location permission after successful login
                        checkLocationPermissionAfterLogin()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = error.message ?: if (_uiState.value.isSignUp) "Google sign-up failed" else "Google sign-in failed",
                                isSignUp = false
                            )
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: if (_uiState.value.isSignUp) "Google sign-up failed" else "Google sign-in failed",
                        isSignUp = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (_uiState.value.isSignUp) "Google sign-up was cancelled" else "Google sign-in was cancelled",
                    isSignUp = false
                )
            }
        }
    }
    
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = ""
        )
    }
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = ""
        )
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = ""
        )
    }
    
    fun login() {
        val currentState = _uiState.value
        
        // Validate inputs
        var hasError = false
        var emailError = ""
        var passwordError = ""
        
        if (currentState.email.isBlank()) {
            emailError = "Email is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            emailError = "Please enter a valid email address"
            hasError = true
        }
        
        if (currentState.password.isBlank()) {
            passwordError = "Password is required"
            hasError = true
        }
        
        if (hasError) {
            _uiState.value = currentState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            
            try {
                // Use Firebase Auth for email/password login
                val result = authRepository.loginWithEmail(currentState.email, currentState.password)
                result.fold(
                    onSuccess = { user ->
                        // Save user session
                        sessionManager.saveUserSession(user, _uiState.value.biometricEnabled)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true
                        )
                        // Check for location permission after successful login
                        checkLocationPermissionAfterLogin()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Login failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Login failed"
                )
            }
        }
    }
    
    fun signUp() {
        val currentState = _uiState.value
        
        // Validate inputs
        var hasError = false
        var emailError = ""
        var nameError = ""
        var passwordError = ""
        
        if (currentState.email.isBlank()) {
            emailError = "Email is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            emailError = "Please enter a valid email address"
            hasError = true
        }
        
        if (currentState.name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        } else if (currentState.name.length < 2) {
            nameError = "Name must be at least 2 characters"
            hasError = true
        }
        
        if (currentState.password.isBlank()) {
            passwordError = "Password is required"
            hasError = true
        } else if (currentState.password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            hasError = true
        }
        
        if (hasError) {
            _uiState.value = currentState.copy(
                emailError = emailError,
                nameError = nameError,
                passwordError = passwordError
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            
            try {
                // Use Firebase Auth for email/password registration
                val result = authRepository.register(currentState.email, currentState.password, currentState.name)
                result.fold(
                    onSuccess = { user ->
                        // Save user session
                        sessionManager.saveUserSession(user, _uiState.value.biometricEnabled)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true
                        )
                        // Check for location permission after successful signup
                        checkLocationPermissionAfterLogin()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Sign up failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Sign up failed"
                )
            }
        }
    }
    
    fun toggleBiometric(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
        sessionManager.setBiometricEnabled(enabled)
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                // Clear session
                sessionManager.logout()
                _uiState.value = _uiState.value.copy(isLoggedIn = false)
            } catch (e: Exception) {
                // Handle any errors silently
            }
        }
    }
    
    fun showSignUp() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "Sign up functionality will be available soon"
        )
    }
    
    fun checkLocationPermissionAfterLogin() {
        viewModelScope.launch {
            if (authRepository.shouldRequestLocationPermission()) {
                _uiState.value = _uiState.value.copy(showLocationPermissionDialog = true)
            }
        }
    }
    
    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showLocationPermissionDialog = false)
            authRepository.requestLocationPermissionAndUpdateUser()
        }
    }
    
    fun onLocationPermissionDenied() {
        _uiState.value = _uiState.value.copy(showLocationPermissionDialog = false)
    }
    
    fun toggleSignUp() {
        _uiState.value = _uiState.value.copy(
            isSignUp = !_uiState.value.isSignUp,
            errorMessage = "",
            emailError = "",
            nameError = "",
            passwordError = ""
        )
    }
    
    fun toggleBiometricSetting() {
        val currentEnabled = _uiState.value.biometricEnabled
        val newEnabled = !currentEnabled
        
        if (newEnabled && !_uiState.value.biometricAvailable) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Biometric authentication is not available on this device"
            )
            return
        }
        
        sessionManager.setBiometricEnabled(newEnabled)
        _uiState.value = _uiState.value.copy(biometricEnabled = newEnabled)
    }
    
    fun getBiometricStatus(): com.example.swoptrader.service.BiometricStatus {
        return biometricAuthService.getBiometricStatus()
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val showGoogleSignIn: Boolean = false,
    val isSignUp: Boolean = false,
    val errorMessage: String = "",
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val emailError: String = "",
    val nameError: String = "",
    val passwordError: String = "",
    val showLocationPermissionDialog: Boolean = false
)
