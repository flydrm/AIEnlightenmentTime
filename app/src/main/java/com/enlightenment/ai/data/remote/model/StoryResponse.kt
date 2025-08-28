package com.enlightenment.ai.data.remote.model

import com.google.gson.annotations.SerializedName

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

data class MetadataResponse(
    @SerializedName("generated_by")
    val generatedBy: String?,
    @SerializedName("quality_score")
    val qualityScore: Float?
)