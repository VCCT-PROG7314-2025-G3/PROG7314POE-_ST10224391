package com.example.swoptrader.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.swoptrader.service.TranslationManager
import com.example.swoptrader.service.TranslationService
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

@Composable
fun rememberTranslationServices(): Pair<TranslationManager, TranslationService> {
    val context = LocalContext.current
    return remember {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            TranslationServicesEntryPoint::class.java
        )
        Pair(entryPoint.translationManager(), entryPoint.translationService())
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface TranslationServicesEntryPoint {
    fun translationManager(): TranslationManager
    fun translationService(): TranslationService
}