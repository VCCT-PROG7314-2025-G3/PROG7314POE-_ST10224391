package com.example.swoptrader.service

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInService @Inject constructor(
    private val context: Context
) {
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken("485701460355-mk6skq9np7rkn4128rin47jdo15eccu6.apps.googleusercontent.com")
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }
    
    fun getSignInIntent() = googleSignInClient.signInIntent
    
    fun handleSignInResult(task: Task<GoogleSignInAccount>): Result<GoogleSignInAccount> {
        return try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Result.success(account)
            } else {
                Result.failure(Exception("Google sign-in failed: No account returned"))
            }
        } catch (e: ApiException) {
            Result.failure(Exception("Google sign-in failed: ${e.statusCode}"))
        }
    }
    
    fun signOut() {
        googleSignInClient.signOut()
    }
    
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}


