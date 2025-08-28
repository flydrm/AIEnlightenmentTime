package com.enlightenment.ai.presentation.camera

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
class CameraViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Preview)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    fun analyzeImage(imageUri: String) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Analyzing
            
            // Simulate image analysis (in real app, call image recognition API)
            delay(2000)
            
            // Mock result
            val result = RecognitionResult(
                objectName = "玩具熊",
                description = "这是一只可爱的棕色玩具熊！它有圆圆的耳朵和黑色的眼睛。",
                funFact = "你知道吗？世界上第一只泰迪熊是在1902年制作的！",
                confidence = 0.95f
            )
            
            _uiState.value = CameraUiState.Result(result)
        }
    }
    
    fun resetToPreview() {
        _uiState.value = CameraUiState.Preview
    }
}

sealed class CameraUiState {
    object Preview : CameraUiState()
    object Analyzing : CameraUiState()
    data class Result(val result: RecognitionResult) : CameraUiState()
}

data class RecognitionResult(
    val objectName: String,
    val description: String,
    val funFact: String? = null,
    val confidence: Float
)