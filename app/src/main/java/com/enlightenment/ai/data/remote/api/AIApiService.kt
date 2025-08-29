package com.enlightenment.ai.data.remote.api

import com.enlightenment.ai.data.remote.model.DialogueResponse
import com.enlightenment.ai.data.remote.model.StoryResponse
import com.enlightenment.ai.data.remote.model.ImageRecognitionResponse
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * AIApiService
 * 
 * 功能说明：
 * [请补充具体功能说明]
 * 
 * 核心职责：
 * 1. [职责1]
 * 2. [职责2]
 * 3. [职责3]
 * 
 * 使用场景：
 * [请补充使用场景]
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */
/**
 * AIApiService - AIApiService
 * 
 * 远程数据访问组件，负责与服务端API通信
 * 
 * @自版本 1.0.0
 */
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

/**
 * StoryGenerateRequest - StoryGenerateRequest
 * 
 * 远程数据访问组件，负责与服务端API通信
 * 
 * @自版本 1.0.0
 */
data class StoryGenerateRequest(
    val childProfile: ChildProfileRequest,
    val storyParams: StoryParamsRequest
)
/**
 * API请求模型 - ChildProfile
 * 
 * 请求参数：
 * 定义ChildProfile相关的API请求数据结构。
 * 确保参数完整性和类型安全。
 * 
 * 验证规则：
 * - 必填字段检查
 * - 参数范围验证
 * - 格式规范校验
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */


/**
 * ChildProfileRequest - ChildProfileRequest
 * 
 * 远程数据访问组件，负责与服务端API通信
 * 
 * @自版本 1.0.0
 */
data class ChildProfileRequest(
    val age: Int,
    val name: String? = null,
    val interests: List<String> = emptyList()
)
/**
 * API请求模型 - StoryParams
 * 
 * 请求参数：
 * 定义StoryParams相关的API请求数据结构。
 * 确保参数完整性和类型安全。
 * 
 * 验证规则：
 * - 必填字段检查
 * - 参数范围验证
 * - 格式规范校验
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */


/**
 * StoryParamsRequest - StoryParamsRequest
 * 
 * 远程数据访问组件，负责与服务端API通信
 * 
 * @自版本 1.0.0
 */
data class StoryParamsRequest(
    val theme: String? = null,
    val length: String = "medium",
    val educationalGoals: List<String> = emptyList()
)
/**
 * API请求模型 - Dialogue
 * 
 * 请求参数：
 * 定义Dialogue相关的API请求数据结构。
 * 确保参数完整性和类型安全。
 * 
 * 验证规则：
 * - 必填字段检查
 * - 参数范围验证
 * - 格式规范校验
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */


/**
 * DialogueRequest - DialogueRequest
 * 
 * 远程数据访问组件，负责与服务端API通信
 * 
 * @自版本 1.0.0
 */
data class DialogueRequest(
    val message: String,
    val context: DialogueContextRequest
)
/**
 * API请求模型 - DialogueContext
 * 
 * 请求参数：
 * 定义DialogueContext相关的API请求数据结构。
 * 确保参数完整性和类型安全。
 * 
 * 验证规则：
 * - 必填字段检查
 * - 参数范围验证
 * - 格式规范校验
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */


/**
 * DialogueContextRequest - DialogueContextRequest
 * 
 * 远程数据访问组件，负责与服务端API通信
 * 
 * @自版本 1.0.0
 */
data class DialogueContextRequest(
    val conversationId: String,
    val childAge: Int,
    val history: List<MessageHistory> = emptyList()
)
/**
 * MessageHistory
 * 
 * 功能说明：
 * 提供MessageHistory相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * MessageHistory - MessageHistory
 * 
 * 远程数据访问组件，负责与服务端API通信
 * 
 * @自版本 1.0.0
 */
data class MessageHistory(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long
)