package com.example.swoptrader.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swoptrader.R
import com.example.swoptrader.service.GoogleSignInService
import com.example.swoptrader.service.GoogleSignInServiceEntryPoint
import com.example.swoptrader.ui.components.AutoTranslatedText
import com.example.swoptrader.ui.components.AutoTranslatedTextBold
import com.example.swoptrader.ui.components.LocationPermissionDialog
import com.example.swoptrader.ui.components.AutoTranslatedTextSmall
import com.example.swoptrader.ui.components.AutoTranslatedTextTitle
import com.example.swoptrader.ui.theme.*
import dagger.hilt.android.EntryPointAccessors
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Get GoogleSignInService from Hilt
    val googleSignInService = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            GoogleSignInServiceEntryPoint::class.java
        ).googleSignInService()
    }
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            // Successfully got the account
            println("Google sign-in successful: ${account?.email}")
            viewModel.handleGoogleSignInResult(account)
        } catch (e: ApiException) {
            println("Google sign-in ApiException: ${e.statusCode} - ${e.message}")
            // Check if it's actually a cancellation or an error
            when (e.statusCode) {
                com.google.android.gms.common.api.CommonStatusCodes.CANCELED -> {
                    // User actually cancelled
                    println("Google sign-in was cancelled by user")
                    viewModel.handleGoogleSignInResult(null)
                }
                com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> {
                    // Network error
                    println("Google sign-in network error")
                    viewModel.handleGoogleSignInResult(null)
                }
                else -> {
                    // Other error occurred - still try to process if possible
                    println("Google sign-in other error, attempting fallback")
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        if (task.isSuccessful) {
                            val account = task.result
                            println("Google sign-in fallback successful: ${account?.email}")
                            viewModel.handleGoogleSignInResult(account)
                        } else {
                            println("Google sign-in fallback failed")
                            viewModel.handleGoogleSignInResult(null)
                        }
                    } catch (ex: Exception) {
                        println("Google sign-in fallback exception: ${ex.message}")
                        viewModel.handleGoogleSignInResult(null)
                    }
                }
            }
        } catch (e: Exception) {
            // General exception
            println("Google sign-in general exception: ${e.message}")
            viewModel.handleGoogleSignInResult(null)
        }
    }
    
    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }
    
    // Handle Google Sign-In request
    LaunchedEffect(uiState.showGoogleSignIn) {
        if (uiState.showGoogleSignIn) {
            // Launch actual Google sign-in flow
            googleSignInLauncher.launch(googleSignInService.getSignInIntent())
        }
    }
    
    // Handle login success
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // SwopTrader Logo
            SwopTraderLogo()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AutoTranslatedTextTitle(
                        text = "Welcome to SwopTrader"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    AutoTranslatedText(
                        text = "Trade items, save the planet"
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Email/Password Login Form
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        label = { AutoTranslatedText("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.emailError.isNotEmpty()
                    )
                    
                    if (uiState.emailError.isNotEmpty()) {
                        Text(
                            text = uiState.emailError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    
                    // Name field for signup
                    if (uiState.isSignUp) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = viewModel::updateName,
                            label = { AutoTranslatedText("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.nameError.isNotEmpty()
                        )
                        
                        if (uiState.nameError.isNotEmpty()) {
                            Text(
                                text = uiState.nameError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = { AutoTranslatedText("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = uiState.passwordError.isNotEmpty()
                    )
                    
                    if (uiState.passwordError.isNotEmpty()) {
                        Text(
                            text = uiState.passwordError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Login/Signup Button
                    Button(
                        onClick = { 
                            if (uiState.isSignUp) {
                                viewModel.signUp()
                            } else {
                                viewModel.login()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            AutoTranslatedTextBold(
                                text = if (uiState.isSignUp) "Sign Up" else "Sign In"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Toggle between Login and Signup
                    TextButton(
                        onClick = { viewModel.toggleSignUp() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AutoTranslatedText(
                            text = if (uiState.isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up"
                        )
                    }
                    
                    if (uiState.errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline
                        )
                        AutoTranslatedText(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        androidx.compose.material3.Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { viewModel.signInWithGoogle() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        AutoTranslatedTextBold(
                            text = "Sign In with Google"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { viewModel.signUpWithGoogle() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        AutoTranslatedTextBold(
                            text = "Sign Up with Google"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Biometric Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = { viewModel.toggleBiometricSetting() },
                            enabled = uiState.biometricAvailable,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            AutoTranslatedText(
                                text = "Enable biometric authentication"
                            )
                            AutoTranslatedText(
                                text = if (uiState.biometricAvailable) {
                                    "Use fingerprint or face recognition for quick login"
                                } else {
                                    "Biometric authentication not available on this device"
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Error Message
            if (uiState.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SwopTraderLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // SwopTrader Logo with black circle background
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    color = androidx.compose.ui.graphics.Color.Black,
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.swoptrader),
                contentDescription = "SwopTrader Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "SwopTrader",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        
        Text(
            text = "Sustainable Trading Platform",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

