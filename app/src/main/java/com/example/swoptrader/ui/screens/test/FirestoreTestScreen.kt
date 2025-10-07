package com.example.swoptrader.ui.screens.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swoptrader.data.test.SimpleFirestoreTest
import com.example.swoptrader.ui.components.GlobalAutoTranslatedText
import com.example.swoptrader.ui.components.GlobalAutoTranslatedTextBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirestoreTestScreen(
    onNavigateBack: () -> Unit,
    viewModel: FirestoreTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { GlobalAutoTranslatedTextBold(text = "Firestore Connection Test") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Test Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        uiState.isLoading -> MaterialTheme.colorScheme.primaryContainer
                        uiState.testResult?.success == true -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        uiState.testResult?.success == false -> Color(0xFFF44336).copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                GlobalAutoTranslatedTextBold(
                                    text = "Testing Connection..."
                                )
                            }
                            uiState.testResult?.success == true -> {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF4CAF50)
                                )
                                GlobalAutoTranslatedTextBold(
                                    text = "Connection Successful! ✅"
                                )
                            }
                            uiState.testResult?.success == false -> {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFF44336)
                                )
                                GlobalAutoTranslatedTextBold(
                                    text = "Connection Failed ❌"
                                )
                            }
                            else -> {
                                GlobalAutoTranslatedTextBold(
                                    text = "Ready to Test"
                                )
                            }
                        }
                    }
                    
                    uiState.testResult?.message?.let { message ->
                        GlobalAutoTranslatedText(
                            text = message
                        )
                    }
                }
            }
            
            // Test Button
            Button(
                onClick = { viewModel.testConnection() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                GlobalAutoTranslatedTextBold(
                    text = if (uiState.isLoading) "Testing..." else "Test Firestore Connection"
                )
            }
            
            // Test Details
            uiState.testResult?.details?.let { details ->
                if (details.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            GlobalAutoTranslatedTextBold(
                                text = "Test Details:"
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(details) { detail ->
                                    GlobalAutoTranslatedText(
                                        text = detail
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    GlobalAutoTranslatedTextBold(
                        text = "What This Test Does:"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val instructions = listOf(
                        "1. Tests basic Firestore connection",
                        "2. Attempts to write a test document",
                        "3. Reads data from Firestore",
                        "4. Deletes the test document",
                        "5. Verifies all operations work correctly"
                    )
                    
                    instructions.forEach { instruction ->
                        GlobalAutoTranslatedText(
                            text = instruction
                        )
                    }
                }
            }
            
            // Troubleshooting
            if (uiState.testResult?.success == false) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        GlobalAutoTranslatedTextBold(
                            text = "Troubleshooting:"
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val troubleshooting = listOf(
                            "• Check if Firestore is enabled in Firebase Console",
                            "• Verify your google-services.json is up to date",
                            "• Ensure you have internet connection",
                            "• Check Firebase project permissions",
                            "• Verify Firestore security rules allow read/write"
                        )
                        
                        troubleshooting.forEach { tip ->
                            GlobalAutoTranslatedText(
                                text = tip
                            )
                        }
                    }
                }
            }
        }
    }
}
