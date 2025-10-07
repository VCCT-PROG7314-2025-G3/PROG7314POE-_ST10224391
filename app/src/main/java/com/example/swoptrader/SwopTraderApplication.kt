package com.example.swoptrader

/**
 * SwopTraderApplication - Application Class with Dependency Injection
 * 
 * This class serves as the application entry point and demonstrates proper
 * integration of dependency injection with Hilt framework. It follows the
 * Application class pattern for Android applications and implements proper
 * initialization of third-party services.
 * 
 * Key Concepts Demonstrated:
 * - Dependency Injection with Hilt (Google, 2022)
 * - Application Lifecycle Management (Android Developers, 2023)
 * - Third-party Service Integration (Google, 2023)
 * - Singleton Pattern Implementation (Gamma et al., 1994)
 * - Service Initialization Patterns (Android Developers, 2023)
 * 
 * Architecture Benefits:
 * - Centralized dependency management
 * - Proper service initialization
 * - Application-wide configuration
 * - Memory management optimization
 * 
 * References:
 * - Google. (2022). Dependency Injection with Hilt. Android Developers Guide.
 * - Android Developers. (2023). Application Class. Android Developers Documentation.
 * - Google. (2023). Firebase Initialization. Firebase Documentation.
 * - Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). Design Patterns: Elements of Reusable Object-Oriented Software.
 * - Android Developers. (2023). Service Initialization Best Practices. Android Developers Guide.
 */

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt Android Application annotation enables dependency injection throughout the app (Google, 2022)
 */
@HiltAndroidApp
class SwopTraderApplication : Application() {
    
    /**
     * Application initialization following Android lifecycle patterns (Android Developers, 2023)
     */
    override fun onCreate() {
        super.onCreate()
        
        // Firebase initialization for cloud services integration (Google, 2023)
        FirebaseApp.initializeApp(this)
        
    }
}
