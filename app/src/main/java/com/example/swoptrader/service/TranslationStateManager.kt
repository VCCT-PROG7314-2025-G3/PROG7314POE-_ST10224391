package com.example.swoptrader.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationStateManager @Inject constructor() {
    
    private val _translationEnabled = MutableStateFlow(true)
    val translationEnabled: StateFlow<Boolean> = _translationEnabled.asStateFlow()
    
    private val _selectedLanguageCode = MutableStateFlow("en")
    val selectedLanguageCode: StateFlow<String> = _selectedLanguageCode.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    fun setTranslationEnabled(enabled: Boolean) {
        _translationEnabled.value = enabled
    }
    
    fun setSelectedLanguage(language: String, code: String) {
        _selectedLanguage.value = language
        _selectedLanguageCode.value = code
    }
    
    fun getCurrentLanguageCode(): String = _selectedLanguageCode.value
    fun isTranslationEnabled(): Boolean = _translationEnabled.value
}

