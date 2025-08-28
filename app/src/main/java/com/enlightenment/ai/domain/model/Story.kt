package com.enlightenment.ai.domain.model

import java.util.UUID

data class Story(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val duration: Int, // in seconds
    val questions: List<Question> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val childAge: Int? = null,
    val personalizedElements: PersonalizedElements? = null
)

data class Question(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null
)

data class PersonalizedElements(
    val childName: String? = null,
    val interests: List<String> = emptyList(),
    val favoriteColor: String? = null,
    val familyMembers: List<String> = emptyList()
)