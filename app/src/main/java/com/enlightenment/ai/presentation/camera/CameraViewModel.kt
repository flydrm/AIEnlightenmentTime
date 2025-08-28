package com.enlightenment.ai.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.usecase.RecognizeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val recognizeImageUseCase: RecognizeImageUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Preview)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    fun analyzeImage(imageUri: String) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Analyzing
            
            try {
                // Convert URI to File
                val imageFile = File(imageUri.removePrefix("file://"))
                
                // Use real image recognition
                recognizeImageUseCase(imageFile)
                    .onSuccess { recognitionResult ->
                        val displayResult = RecognitionResult(
                            objectName = recognitionResult.objectName,
                            description = recognitionResult.description,
                            funFact = recognitionResult.funFact,
                            confidence = recognitionResult.confidence
                        )
                        _uiState.value = CameraUiState.Result(displayResult)
                    }
                    .onFailure { error ->
                        // Handle error - show default educational content
                        val fallbackResult = RecognitionResult(
                            objectName = "神秘物品",
                            description = "哎呀，我没有认出这是什么。但是每个物品都有它独特的故事！",
                            funFact = "继续探索，你会发现更多有趣的东西！",
                            confidence = 0.5f
                        )
                        _uiState.value = CameraUiState.Result(fallbackResult)
                    }
            } catch (e: Exception) {
                // Handle file access error
                _uiState.value = CameraUiState.Preview
            }
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