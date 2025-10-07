package com.example.swoptrader.data.test

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleFirestoreTest @Inject constructor() {
    
    companion object {
        private const val TAG = "SimpleFirestoreTest"
    }
    
    suspend fun testConnection(): TestResult {
        return try {
            Log.d(TAG, "Starting simple Firestore connection test...")
            
            val firestore = FirebaseFirestore.getInstance()
            
            // Test 1: Basic connection
            Log.d(TAG, "Testing basic connection...")
            if (firestore != null) {
                Log.d(TAG, "✅ Basic connection successful")
            } else {
                return TestResult(
                    success = false,
                    message = "Failed to get Firestore instance"
                )
            }
            
            // Test 2: Write a simple document
            Log.d(TAG, "Testing write operation...")
            val testData = mapOf(
                "test" to "connection",
                "timestamp" to System.currentTimeMillis(),
                "message" to "Hello from SwopTrader!"
            )
            
            firestore.collection("connection_test")
                .document("test_${System.currentTimeMillis()}")
                .set(testData)
                .await()
            
            Log.d(TAG, "✅ Write operation successful")
            
            // Test 3: Read a document
            Log.d(TAG, "Testing read operation...")
            val snapshot = firestore.collection("connection_test")
                .limit(1)
                .get()
                .await()
            
            if (snapshot.documents.isNotEmpty()) {
                val data = snapshot.documents.first().data
                Log.d(TAG, "✅ Read operation successful - Found: $data")
            } else {
                Log.d(TAG, "✅ Read operation successful - No documents found")
            }
            
            Log.d(TAG, "All Firestore tests passed! ✅")
            TestResult(
                success = true,
                message = "Firestore connection successful! All tests passed.",
                details = listOf(
                    "✅ Basic connection established",
                    "✅ Write operation successful",
                    "✅ Read operation successful"
                )
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Firestore test failed: ${e.message}", e)
            TestResult(
                success = false,
                message = "Firestore connection failed: ${e.message}",
                details = listOf("❌ Error: ${e.javaClass.simpleName}")
            )
        }
    }
    
    data class TestResult(
        val success: Boolean,
        val message: String,
        val details: List<String> = emptyList()
    )
}

