package com.enlightenment.ai.data.remote.model

import com.google.gson.annotations.SerializedName
/**
 * API响应模型 - ImageRecognition
 * 
 * 数据结构：
 * 封装ImageRecognition相关的API响应数据。
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
 * ImageRecognitionResponse - ImageRecognition响应
 * 
 * API响应数据模型，用于解析服务端返回的JSON数据
 * 
 * 数据结构遵循后端API规范，确保前后端数据一致性
 * 
 * @自版本 1.0.0
 */
data class ImageRecognitionResponse(
    @SerializedName("object_name")
    val objectName: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("confidence")
    val confidence: Float,
    @SerializedName("educational_content")
    val educationalContent: String?,
    @SerializedName("fun_facts")
    val funFacts: List<String>?,
    @SerializedName("related_topics")
    val relatedTopics: List<String>?,
    @SerializedName("age_appropriate")
    val ageAppropriate: Boolean
)