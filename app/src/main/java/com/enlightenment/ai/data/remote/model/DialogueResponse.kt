package com.enlightenment.ai.data.remote.model

import com.google.gson.annotations.SerializedName
/**
 * API响应模型 - Dialogue
 * 
 * 数据结构：
 * 封装Dialogue相关的API响应数据。
 * 与后端接口契约保持一致。
 * 
 * 字段映射：
 * - 使用@SerializedName处理命名差异
 * - 支持可空类型处理缺失字段
 * - 默认值保证数据完整性
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */


/**
 * DialogueResponse - Dialogue响应
 * 
 * API响应数据模型，用于解析服务端返回的JSON数据
 * 
 * 数据结构遵循后端API规范，确保前后端数据一致性
 * 
 * @since 1.0.0
 */
data class DialogueResponse(
    @SerializedName("reply")
    val reply: ReplyResponse,
    @SerializedName("metadata")
    val metadata: DialogueMetadataResponse?
)
/**
 * API响应模型 - Reply
 * 
 * 数据结构：
 * 封装Reply相关的API响应数据。
 * 与后端接口契约保持一致。
 * 
 * 字段映射：
 * - 使用@SerializedName处理命名差异
 * - 支持可空类型处理缺失字段
 * - 默认值保证数据完整性
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */


/**
 * ReplyResponse - Reply响应
 * 
 * API响应数据模型，用于解析服务端返回的JSON数据
 * 
 * 数据结构遵循后端API规范，确保前后端数据一致性
 * 
 * @since 1.0.0
 */
data class ReplyResponse(
    @SerializedName("text")
    val text: String,
    @SerializedName("emotion")
    val emotion: String?,
    @SerializedName("suggestions")
    val suggestions: List<String>?
)
/**
 * API响应模型 - DialogueMetadata
 * 
 * 数据结构：
 * 封装DialogueMetadata相关的API响应数据。
 * 与后端接口契约保持一致。
 * 
 * 字段映射：
 * - 使用@SerializedName处理命名差异
 * - 支持可空类型处理缺失字段
 * - 默认值保证数据完整性
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */


/**
 * DialogueMetadataResponse - DialogueMetadata响应
 * 
 * API响应数据模型，用于解析服务端返回的JSON数据
 * 
 * 数据结构遵循后端API规范，确保前后端数据一致性
 * 
 * @自版本 1.0.0
 */
data class DialogueMetadataResponse(
    @SerializedName("intent")
    val intent: String?,
    @SerializedName("confidence")
    val confidence: Float?
)