package com.enlightenment.ai.domain.usecase

import com.enlightenment.ai.domain.repository.ImageRecognitionRepository
import java.io.File
import javax.inject.Inject

class RecognizeImageUseCase @Inject constructor(
    private val imageRecognitionRepository: ImageRecognitionRepository
) {
    suspend operator fun invoke(imageFile: File): Result<RecognitionResult> {
        return imageRecognitionRepository.recognizeImage(imageFile)
    }
}

data class RecognitionResult(
    val objectName: String,
    val description: String,
    val funFact: String? = null,
    val confidence: Float,
    val educationalContent: String? = null,
    val relatedTopics: List<String> = emptyList()
)