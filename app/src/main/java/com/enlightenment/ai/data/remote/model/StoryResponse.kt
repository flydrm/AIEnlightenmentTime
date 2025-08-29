package com.enlightenment.ai.data.remote.model

import com.google.gson.annotations.SerializedName
/**
 * API响应模型 - Story
 * 
 * 数据结构：
 * 封装Story相关的API响应数据。
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
 * StoryResponse - Story响应
 * 
 * API响应数据模型，用于解析服务端返回的JSON数据
 * 
 * 数据结构遵循后端API规范，确保前后端数据一致性
 * 
 * @since 1.0.0
 */
data class StoryResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("questions")
    val questions: List<QuestionResponse>?,
    @SerializedName("metadata")
    val metadata: MetadataResponse?
)
/**
 * API响应模型 - Question
 * 
 * 数据结构：
 * 封装Question相关的API响应数据。
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
 * QuestionResponse - Question响应
 * 
 * API响应数据模型，用于解析服务端返回的JSON数据
 * 
 * 数据结构遵循后端API规范，确保前后端数据一致性
 * 
 * @since 1.0.0
 */
data class QuestionResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("options")
    val options: List<String>,
    @SerializedName("correct_answer_index")
    val correctAnswerIndex: Int,
    @SerializedName("explanation")
    val explanation: String?
)
/**
 * API响应模型 - Metadata
 * 
 * 数据结构：
 * 封装Metadata相关的API响应数据。
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
 * MetadataResponse - Metadata响应
 * 
 * API响应数据模型，用于解析服务端返回的JSON数据
 * 
 * 数据结构遵循后端API规范，确保前后端数据一致性
 * 
 * @since 1.0.0
 */
data class MetadataResponse(
    @SerializedName("generated_by")
    val generatedBy: String?,
    @SerializedName("quality_score")
    val qualityScore: Float?
)