package com.example.swoptrader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.example.swoptrader.service.TranslationStateManager
import com.example.swoptrader.service.TranslationService
import com.example.swoptrader.service.TranslationManager
import com.example.swoptrader.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun TranslationButton(
    text: String,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var showTranslation by remember { mutableStateOf(false) }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val settingsState by settingsViewModel.uiState.collectAsState()
    val lifecycleScope = LocalLifecycleOwner.current.lifecycleScope
    
    // Debug logging
    println("TranslationButton: translationEnabled=${settingsState.translationEnabled}, selectedLanguageCode=${settingsState.selectedLanguageCode}")
    
    // Only show translation button if translation is enabled and language is not English
    if (settingsState.translationEnabled && settingsState.selectedLanguageCode != "en") {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Original text
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Translation button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Translate to ${settingsState.selectedLanguage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                    
                    Button(
                        onClick = {
                            if (!showTranslation && !isTranslating) {
                                isTranslating = true
                                // Translate the text using coroutine
                                lifecycleScope.launch {
                                    settingsViewModel.translateText(text).fold(
                                        onSuccess = { translation ->
                                            translatedText = translation
                                            showTranslation = true
                                            isTranslating = false
                                        },
                                        onFailure = { error ->
                                            translatedText = "Translation failed: ${error.message}"
                                            showTranslation = true
                                            isTranslating = false
                                        }
                                    )
                                }
                            } else {
                                showTranslation = !showTranslation
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translate",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Show translated text if available
                if (showTranslation && translatedText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = translatedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AutoTranslatedText(
    text: String,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val settingsState by settingsViewModel.uiState.collectAsState()
    
    // Debug logging
    println("AutoTranslatedText: text='$text', translationEnabled=${settingsState.translationEnabled}, selectedLanguageCode=${settingsState.selectedLanguageCode}")
    
    // Auto-translate if translation is enabled and language is not English
    LaunchedEffect(text, settingsState.translationEnabled, settingsState.selectedLanguageCode) {
        if (settingsState.translationEnabled && 
            settingsState.selectedLanguageCode != "en" && 
            text.isNotEmpty()) {
            println("AutoTranslatedText: Starting translation for '$text'")
            isTranslating = true
            settingsViewModel.translateText(text).fold(
                onSuccess = { translation ->
                    println("AutoTranslatedText: Translation successful: '$translation'")
                    translatedText = translation
                    isTranslating = false
                },
                onFailure = { error ->
                    println("AutoTranslatedText: Translation failed: ${error.message}")
                    translatedText = text // Fallback to original text
                    isTranslating = false
                }
            )
        } else {
            println("AutoTranslatedText: Skipping translation - enabled=${settingsState.translationEnabled}, code=${settingsState.selectedLanguageCode}, text='$text'")
            translatedText = text
        }
    }
    
    if (isTranslating) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Translating...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text(
            text = translatedText.ifEmpty { text },
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
        )
    }
}

@Composable
fun AutoTranslatedTextBold(
    text: String,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val settingsState by settingsViewModel.uiState.collectAsState()
    
    LaunchedEffect(text, settingsState.translationEnabled, settingsState.selectedLanguageCode) {
        if (settingsState.translationEnabled && 
            settingsState.selectedLanguageCode != "en" && 
            text.isNotEmpty()) {
            isTranslating = true
            settingsViewModel.translateText(text).fold(
                onSuccess = { translation ->
                    translatedText = translation
                    isTranslating = false
                },
                onFailure = { error ->
                    translatedText = text
                    isTranslating = false
                }
            )
        } else {
            translatedText = text
        }
    }
    
    if (isTranslating) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Translating...",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text(
            text = translatedText.ifEmpty { text },
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            modifier = modifier
        )
    }
}

@Composable
fun AutoTranslatedTextSmall(
    text: String,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val settingsState by settingsViewModel.uiState.collectAsState()
    
    LaunchedEffect(text, settingsState.translationEnabled, settingsState.selectedLanguageCode) {
        if (settingsState.translationEnabled && 
            settingsState.selectedLanguageCode != "en" && 
            text.isNotEmpty()) {
            isTranslating = true
            settingsViewModel.translateText(text).fold(
                onSuccess = { translation ->
                    translatedText = translation
                    isTranslating = false
                },
                onFailure = { error ->
                    translatedText = text
                    isTranslating = false
                }
            )
        } else {
            translatedText = text
        }
    }
    
    if (isTranslating) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Translating...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text(
            text = translatedText.ifEmpty { text },
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier
        )
    }
}

@Composable
fun AutoTranslatedTextTitle(
    text: String,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val settingsState by settingsViewModel.uiState.collectAsState()
    
    LaunchedEffect(text, settingsState.translationEnabled, settingsState.selectedLanguageCode) {
        if (settingsState.translationEnabled && 
            settingsState.selectedLanguageCode != "en" && 
            text.isNotEmpty()) {
            isTranslating = true
            settingsViewModel.translateText(text).fold(
                onSuccess = { translation ->
                    translatedText = translation
                    isTranslating = false
                },
                onFailure = { error ->
                    translatedText = text
                    isTranslating = false
                }
            )
        } else {
            translatedText = text
        }
    }
    
    if (isTranslating) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Translating...",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text(
            text = translatedText.ifEmpty { text },
            style = MaterialTheme.typography.headlineSmall,
            modifier = modifier
        )
    }
}

// New shared composables that use the TranslationManager singleton
@Composable
fun SharedAutoTranslatedText(
    text: String,
    modifier: Modifier = Modifier,
    translationManager: TranslationManager,
    translationService: TranslationService
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val translationEnabled by translationManager.translationEnabled.collectAsState()
    val selectedLanguageCode by translationManager.selectedLanguageCode.collectAsState()
    
    // Debug logging
    println("SharedAutoTranslatedText: text='$text', translationEnabled=$translationEnabled, selectedLanguageCode=$selectedLanguageCode")
    
    // Auto-translate if translation is enabled and language is not English
    LaunchedEffect(text, translationEnabled, selectedLanguageCode) {
        if (translationEnabled && 
            selectedLanguageCode != "en" && 
            text.isNotEmpty()) {
            println("SharedAutoTranslatedText: Starting translation for '$text'")
            isTranslating = true
            translationService.translateText(
                text = text,
                targetLanguage = selectedLanguageCode,
                sourceLanguage = "auto"
            ).fold(
                onSuccess = { translation ->
                    println("SharedAutoTranslatedText: Translation successful: '$translation'")
                    translatedText = translation
                    isTranslating = false
                },
                onFailure = { error ->
                    println("SharedAutoTranslatedText: Translation failed: ${error.message}")
                    translatedText = text // Fallback to original text
                    isTranslating = false
                }
            )
        } else {
            println("SharedAutoTranslatedText: Skipping translation - enabled=$translationEnabled, code=$selectedLanguageCode, text='$text'")
            translatedText = text
        }
    }
    
    if (isTranslating) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Translating...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text(
            text = translatedText.ifEmpty { text },
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SharedAutoTranslatedTextBold(
    text: String,
    modifier: Modifier = Modifier,
    translationManager: TranslationManager,
    translationService: TranslationService
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val translationEnabled by translationManager.translationEnabled.collectAsState()
    val selectedLanguageCode by translationManager.selectedLanguageCode.collectAsState()
    
    // Auto-translate if translation is enabled and language is not English
    LaunchedEffect(text, translationEnabled, selectedLanguageCode) {
        if (translationEnabled && 
            selectedLanguageCode != "en" && 
            text.isNotEmpty()) {
            isTranslating = true
            translationService.translateText(
                text = text,
                targetLanguage = selectedLanguageCode,
                sourceLanguage = "auto"
            ).fold(
                onSuccess = { translation ->
                    translatedText = translation
                    isTranslating = false
                },
                onFailure = { error ->
                    translatedText = text
                    isTranslating = false
                }
            )
        } else {
            translatedText = text
        }
    }
    
    if (isTranslating) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Translating...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text(
            text = translatedText.ifEmpty { text },
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@Composable
fun SharedAutoTranslatedTextTitle(
    text: String,
    modifier: Modifier = Modifier,
    translationManager: TranslationManager,
    translationService: TranslationService
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val translationEnabled by translationManager.translationEnabled.collectAsState()
    val selectedLanguageCode by translationManager.selectedLanguageCode.collectAsState()
    
    // Auto-translate if translation is enabled and language is not English
    LaunchedEffect(text, translationEnabled, selectedLanguageCode) {
        if (translationEnabled && 
            selectedLanguageCode != "en" && 
            text.isNotEmpty()) {
            isTranslating = true
            translationService.translateText(
                text = text,
                targetLanguage = selectedLanguageCode,
                sourceLanguage = "auto"
            ).fold(
                onSuccess = { translation ->
                    translatedText = translation
                    isTranslating = false
                },
                onFailure = { error ->
                    translatedText = text
                    isTranslating = false
                }
            )
        } else {
            translatedText = text
        }
    }
    
    if (isTranslating) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Translating...",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text(
            text = translatedText.ifEmpty { text },
            modifier = modifier,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
