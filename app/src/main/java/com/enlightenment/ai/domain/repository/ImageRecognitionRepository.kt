package com.enlightenment.ai.domain.repository

import com.enlightenment.ai.domain.usecase.RecognitionResult
import java.io.File

interface ImageRecognitionRepository {
    suspend fun recognizeImage(imageFile: File): Result<RecognitionResult>
    suspend fun getCachedRecognitions(): List<RecognitionResult>
}