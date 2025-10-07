package com.example.swoptrader.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationService @Inject constructor(
    private val context: Context
) {
    
    private val httpClient = OkHttpClient()
    
    // Real translation service using Google Translate API via HTTP
    
    suspend fun translateText(
        text: String,
        targetLanguage: String,
        sourceLanguage: String = "auto"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            println("TranslationService: Attempting to translate '$text' to $targetLanguage")
            
            // If target language is English, return original text
            if (targetLanguage == "en") {
                return@withContext Result.success(text)
            }
            
            val apiKey = context.getString(com.example.swoptrader.R.string.google_maps_key)
            println("TranslationService: Using API key: ${apiKey.take(10)}...")
            
            // Check if API key is valid
            if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
                println("TranslationService: Invalid or missing API key")
                val fallbackTranslation = getFallbackTranslation(text, targetLanguage)
                return@withContext Result.success(fallbackTranslation)
            }
            
            // Use Google Translate API via HTTP
            val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"
            println("TranslationService: Request URL: $url")
            
            val requestBody = JSONObject().apply {
                put("q", text)
                put("target", targetLanguage)
                if (sourceLanguage != "auto") {
                    put("source", sourceLanguage)
                }
            }.toString()
            
            println("TranslationService: Request body: $requestBody")
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("User-Agent", "SwopTrader-Android/1.0")
                .addHeader("X-Android-Package", "com.example.swoptrader")
                .addHeader("X-Android-Cert", "41:63:3A:FB:E7:1B:82:34:F8:10:71:1B:CE:48:7E:02:A7:75:59:92")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            println("TranslationService: Response code: ${response.code}")
            println("TranslationService: Response message: ${response.message}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                println("TranslationService: Response body: $responseBody")
                
                if (responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val translations = jsonResponse.getJSONObject("data").getJSONArray("translations")
                        val translation = translations.getJSONObject(0).getString("translatedText")
                        
                        println("TranslationService: Translation successful: '$translation'")
                        return@withContext Result.success(translation)
                    } catch (e: Exception) {
                        println("TranslationService: JSON parsing error: ${e.message}")
                        println("TranslationService: Response body was: $responseBody")
                    }
                }
            } else {
                val errorBody = response.body?.string()
                println("TranslationService: API call failed with code ${response.code}")
                println("TranslationService: Error response: $errorBody")
            }
            
            println("TranslationService: API call failed, using fallback")
            val fallbackTranslation = getFallbackTranslation(text, targetLanguage)
            return@withContext Result.success(fallbackTranslation)
            
        } catch (e: Exception) {
            println("TranslationService: Translation error: ${e.message}")
            // Fallback to mock translation if anything fails
            val fallbackTranslation = getFallbackTranslation(text, targetLanguage)
            println("TranslationService: Using fallback translation: '$fallbackTranslation'")
            Result.success(fallbackTranslation)
        }
    }
    
    suspend fun detectLanguage(text: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = context.getString(com.example.swoptrader.R.string.google_maps_key)
            
            // Use Google Translate API for language detection
            val url = "https://translation.googleapis.com/language/translate/v2/detect?key=$apiKey"
            
            val requestBody = JSONObject().apply {
                put("q", text)
            }.toString()
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val detections = jsonResponse.getJSONObject("data").getJSONArray("detections")
                    val detection = detections.getJSONArray(0).getJSONObject(0)
                    val detectedLanguage = detection.getString("language")
                    
                    println("TranslationService: Language detected: $detectedLanguage")
                    return@withContext Result.success(detectedLanguage)
                }
            }
            
            println("TranslationService: Language detection failed, using fallback")
            val fallbackLanguage = getFallbackLanguageDetection(text)
            return@withContext Result.success(fallbackLanguage)
            
        } catch (e: Exception) {
            println("Language detection error: ${e.message}")
            // Fallback to simple detection
            val fallbackLanguage = getFallbackLanguageDetection(text)
            Result.success(fallbackLanguage)
        }
    }
    
    private fun getFallbackTranslation(text: String, targetLanguage: String): String {
        // Fallback translations for when API is not available
        val fallbackTranslations = mapOf(
            "en" to text, // English - return original
            "af" to "[Afrikaans] $text",
            "zu" to "[Zulu] $text",
            "xh" to "[Xhosa] $text",
            "st" to "[Sesotho] $text",
            "tn" to "[Setswana] $text",
            "fr" to "[French] $text",
            "de" to "[German] $text",
            "es" to "[Spanish] $text",
            "pt" to "[Portuguese] $text"
        )
        return fallbackTranslations[targetLanguage] ?: "[$targetLanguage] $text"
    }
    
    private fun getFallbackLanguageDetection(text: String): String {
        // Simple language detection based on text patterns
        return when {
            text.contains("Hello") || text.contains("the") -> "en"
            text.contains("Hallo") -> "af"
            text.contains("Sawubona") -> "zu"
            text.contains("Molo") -> "xh"
            text.contains("Dumela") -> "st"
            text.contains("Bonjour") -> "fr"
            text.contains("Hallo") -> "de"
            text.contains("Hola") -> "es"
            text.contains("Olá") -> "pt"
            else -> "en" // Default to English
        }
    }
    
    fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language("en", "English"),
            Language("af", "Afrikaans"),
            Language("zu", "Zulu"),
            Language("xh", "Xhosa"),
            Language("st", "Sesotho"),
            Language("tn", "Setswana"),
            Language("ss", "Swati"),
            Language("ve", "Venda"),
            Language("ts", "Tsonga"),
            Language("nr", "Ndebele"),
            Language("fr", "French"),
            Language("de", "German"),
            Language("es", "Spanish"),
            Language("pt", "Portuguese"),
            Language("it", "Italian"),
            Language("ru", "Russian"),
            Language("ja", "Japanese"),
            Language("ko", "Korean"),
            Language("zh", "Chinese"),
            Language("ar", "Arabic"),
            Language("hi", "Hindi")
        )
    }
}

data class Language(
    val code: String,
    val name: String
)
