package com.example.swoptrader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GlobalAutoTranslatedText(
    text: String,
    modifier: Modifier = Modifier
) {
    val (translationManager, translationService) = rememberTranslationServices()
    
    SharedAutoTranslatedText(
        text = text,
        modifier = modifier,
        translationManager = translationManager,
        translationService = translationService
    )
}

@Composable
fun GlobalAutoTranslatedTextBold(
    text: String,
    modifier: Modifier = Modifier
) {
    val (translationManager, translationService) = rememberTranslationServices()
    
    SharedAutoTranslatedTextBold(
        text = text,
        modifier = modifier,
        translationManager = translationManager,
        translationService = translationService
    )
}

@Composable
fun GlobalAutoTranslatedTextTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val (translationManager, translationService) = rememberTranslationServices()
    
    SharedAutoTranslatedTextTitle(
        text = text,
        modifier = modifier,
        translationManager = translationManager,
        translationService = translationService
    )
}

