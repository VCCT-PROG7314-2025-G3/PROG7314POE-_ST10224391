package com.example.swoptrader.service

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GoogleSignInServiceEntryPoint {
    fun googleSignInService(): GoogleSignInService
}


