package com.example.swoptrader.ui.screens.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swoptrader.data.test.SimpleFirestoreTest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirestoreTestViewModel @Inject constructor(
    private val simpleFirestoreTest: SimpleFirestoreTest
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FirestoreTestUiState())
    val uiState: StateFlow<FirestoreTestUiState> = _uiState.asStateFlow()
    
    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                testResult = null
            )
            
            try {
                val result = simpleFirestoreTest.testConnection()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    testResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    testResult = SimpleFirestoreTest.TestResult(
                        success = false,
                        message = "Test failed with exception: ${e.message}",
                        details = listOf("❌ Exception: ${e.javaClass.simpleName}")
                    )
                )
            }
        }
    }
    
    data class FirestoreTestUiState(
        val isLoading: Boolean = false,
        val testResult: SimpleFirestoreTest.TestResult? = null
    )
}
