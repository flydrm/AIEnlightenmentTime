package com.enlightenment.ai.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            
            // Perform actual verification
            val isValid = verifyAnswer(answer)
            
            if (isValid) {
                _uiState.value = ParentLoginUiState.Success
            } else {
                _uiState.value = ParentLoginUiState.Error("答案不正确，请重试")
            }
        }
    }
    
    private suspend fun verifyAnswer(answer: String): Boolean {
        // Real verification logic
        return withContext(Dispatchers.Default) {
            answer.trim() == correctAnswer
        }
    }
    
    fun showPasswordHint() {
        _uiState.value = ParentLoginUiState.ShowHint("提示：十二加七等于多少？")
        
        // Reset to idle after showing hint
        viewModelScope.launch {
            delay(3000)
            if (_uiState.value is ParentLoginUiState.ShowHint) {
                _uiState.value = ParentLoginUiState.Idle
            }
        }
    }
}

sealed class ParentLoginUiState {
    object Idle : ParentLoginUiState()
    object Loading : ParentLoginUiState()
    object Success : ParentLoginUiState()
    data class Error(val message: String) : ParentLoginUiState()
    data class ShowHint(val hint: String) : ParentLoginUiState()
}