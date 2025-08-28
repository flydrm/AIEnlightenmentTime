package com.enlightenment.ai.data.remote.api

import com.enlightenment.ai.data.remote.model.DialogueResponse
import com.enlightenment.ai.data.remote.model.StoryResponse
import com.enlightenment.ai.data.remote.model.ImageRecognitionResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface AIApiService {
    
    @POST("story/generate")
    @Headers("Content-Type: application/json")
    suspend fun generateStory(
        @Header("Authorization") apiKey: String = "Bearer ${AIModelConfig.GEMINI_API_KEY}",
        @Body request: StoryGenerateRequest
    ): StoryResponse
    
    @POST("dialogue/chat")
    @Headers("Content-Type: application/json")
    suspend fun sendDialogueMessage(
        @Header("Authorization") apiKey: String = "Bearer ${AIModelConfig.GPT_API_KEY}",
        @Body request: DialogueRequest
    ): DialogueResponse
    
    @Multipart
    @POST("image/recognize")
    suspend fun recognizeImage(
        @Header("Authorization") apiKey: String = "Bearer ${AIModelConfig.GEMINI_API_KEY}",
        @Part image: MultipartBody.Part
    ): ImageRecognitionResponse
}

data class StoryGenerateRequest(
    val childProfile: ChildProfileRequest,
    val storyParams: StoryParamsRequest
)

data class ChildProfileRequest(
    val age: Int,
    val name: String? = null,
    val interests: List<String> = emptyList()
)

data class StoryParamsRequest(
    val theme: String? = null,
    val length: String = "medium",
    val educationalGoals: List<String> = emptyList()
)

data class DialogueRequest(
    val message: String,
    val context: DialogueContextRequest
)

data class DialogueContextRequest(
    val conversationId: String,
    val childAge: Int,
    val history: List<MessageHistory> = emptyList()
)

data class MessageHistory(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long
)