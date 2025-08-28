package com.enlightenment.ai.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentLoginViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow<ParentLoginUiState>(ParentLoginUiState.Idle)
    val uiState: StateFlow<ParentLoginUiState> = _uiState.asStateFlow()
    
    // Simple math challenge: 12 + 7 = 19
    private val correctAnswer = "19"
    
    fun verifyPassword(answer: String) {
        viewModelScope.launch {
            _uiState.value = ParentLoginUiState.Loading
            
            // Simulate verification delay
            delay(1000)
            
            if (answer == correctAnswer) {
                _uiState.value = ParentLoginUiState.Success
            } else {
                _uiState.value = ParentLoginUiState.Error("答案不正确，请重试")
            }
        }
    }
}

sealed class ParentLoginUiState {
    object Idle : ParentLoginUiState()
    object Loading : ParentLoginUiState()
    object Success : ParentLoginUiState()
    data class Error(val message: String) : ParentLoginUiState()
}