package com.enlightenment.ai.data.remote.model

import com.google.gson.annotations.SerializedName

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