package com.enlightenment.ai.data.remote.api

import com.enlightenment.ai.data.remote.model.DialogueResponse
import com.enlightenment.ai.data.remote.model.StoryResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AIApiService {
    
    @POST("api/v1/story/generate")
    suspend fun generateStory(
        @Body request: StoryGenerateRequest
    ): StoryResponse
    
    @POST("api/v1/dialogue/chat")
    suspend fun sendDialogueMessage(
        @Body request: DialogueRequest
    ): DialogueResponse
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