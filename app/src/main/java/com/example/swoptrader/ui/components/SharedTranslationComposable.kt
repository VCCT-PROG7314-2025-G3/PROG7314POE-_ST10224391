package com.example.swoptrader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swoptrader.service.TranslationStateManager
import com.example.swoptrader.service.TranslationService
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun SharedAutoTranslatedText(
    text: String,
    modifier: Modifier = Modifier,
    translationStateManager: TranslationStateManager,
    translationService: TranslationService
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val translationEnabled by translationStateManager.translationEnabled.collectAsState()
    val selectedLanguageCode by translationStateManager.selectedLanguageCode.collectAsState()
    
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
    translationStateManager: TranslationStateManager,
    translationService: TranslationService
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val translationEnabled by translationStateManager.translationEnabled.collectAsState()
    val selectedLanguageCode by translationStateManager.selectedLanguageCode.collectAsState()
    
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
    translationStateManager: TranslationStateManager,
    translationService: TranslationService
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    
    val translationEnabled by translationStateManager.translationEnabled.collectAsState()
    val selectedLanguageCode by translationStateManager.selectedLanguageCode.collectAsState()
    
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

