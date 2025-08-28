package com.enlightenment.ai.data.remote.model

import com.google.gson.annotations.SerializedName

data class DialogueResponse(
    @SerializedName("reply")
    val reply: ReplyResponse,
    @SerializedName("metadata")
    val metadata: DialogueMetadataResponse?
)

data class ReplyResponse(
    @SerializedName("text")
    val text: String,
    @SerializedName("emotion")
    val emotion: String?,
    @SerializedName("suggestions")
    val suggestions: List<String>?
)

data class DialogueMetadataResponse(
    @SerializedName("intent")
    val intent: String?,
    @SerializedName("confidence")
    val confidence: Float?
)