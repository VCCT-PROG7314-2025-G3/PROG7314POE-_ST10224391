package com.example.swoptrader.service

import android.content.Context
import android.content.SharedPreferences
import com.example.swoptrader.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("swoptrader_session", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_REMEMBER_USER = "remember_user"
    }
    
    fun saveUserSession(user: User, rememberUser: Boolean = true) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_DATA, gson.toJson(user))
            putBoolean(KEY_REMEMBER_USER, rememberUser)
            apply()
        }
    }
    
    fun getCurrentUser(): User? {
        val userJson = prefs.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    fun isLoggedIn(): Boolean {
        // Check both local session and Firebase Auth state
        val localLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val firebaseUser = firebaseAuth.currentUser
        
        // If Firebase user exists, we're logged in
        if (firebaseUser != null) {
            // Update local session if Firebase user exists but local session is false
            if (!localLoggedIn) {
                prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
            }
            return true
        } else {
            // If Firebase user is null, clear local session
            if (localLoggedIn) {
                clearSession()
            }
            return false
        }
    }
    
    fun shouldRememberUser(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_USER, false)
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    fun clearSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putString(KEY_USER_DATA, null)
            putBoolean(KEY_REMEMBER_USER, false)
            putBoolean(KEY_BIOMETRIC_ENABLED, false)
            apply()
        }
    }
    
    fun logout() {
        // Sign out from Firebase Auth
        firebaseAuth.signOut()
        // Always clear the session completely when user explicitly logs out
        clearSession()
    }
}
